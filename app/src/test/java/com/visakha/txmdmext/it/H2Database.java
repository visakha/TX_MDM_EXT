package com.visakha.txmdmext.it;

import com.visakha.txmdmext.config.AppProperties;
import com.visakha.txmdmext.config.TestConfig;
import com.visakha.txmdmext.event.EventParser;
import com.visakha.txmdmext.event.Fixtures;
import com.visakha.txmdmext.event.StoredEvent;
import com.visakha.txmdmext.history.HistoryPlanner;
import com.visakha.txmdmext.job.EventProcessor;
import com.visakha.txmdmext.job.HistoryWriter;
import com.visakha.txmdmext.model.zlvguest.GuestChildRepository;
import com.visakha.txmdmext.model.zlvguest.GuestMasterRepository;
import com.visakha.txmdmext.model.zlvguest.ZlvGuestModel;
import com.visakha.txmdmext.store.DbTime;
import com.visakha.txmdmext.store.EventStoreRepository;
import com.visakha.txmdmext.store.FieldChangeRepository;
import com.visakha.txmdmext.store.LoadedEventRepository;
import com.visakha.txmdmext.store.SourceHistoryRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

/**
 * A fresh in-memory H2 (Oracle mode) per test class, with the REAL V001 DDL applied as-is and the
 * real production repositories and EventProcessor wired by hand. Schema is created only here.
 */
final class H2Database {

  static final Path V001 = Path.of("..", "ddl", "V001__history_tables.sql");
  static final Path V002 = Path.of("..", "ddl", "V002__zlv_guest_tables.sql");
  static final Path V003 = Path.of("..", "ddl", "V003__zlv_guest_phone_num.sql");

  final DataSource dataSource;
  final JdbcClient jdbc;
  final AppProperties props = TestConfig.example();

  H2Database() {
    JdbcDataSource ds = new JdbcDataSource();
    ds.setURL("jdbc:h2:mem:it_" + UUID.randomUUID() + ";MODE=Oracle;DB_CLOSE_DELAY=-1");
    this.dataSource = ds;
    this.jdbc = JdbcClient.create(ds);
  }

  H2Database withHistorySchema() {
    run(read(V001));
    run(read(V003));
    return this;
  }

  H2Database withEventStore() {
    run(resource("/db/zlv_mdm_events_store.sql"));
    return this;
  }

  /** Inserts the 44 real rows of extract_20261127_005948.zip. */
  H2Database withRealEvents() {
    Fixtures.rows().forEach(this::insertEvent);
    return this;
  }

  void insertEvent(StoredEvent row) {
    jdbc.sql("INSERT INTO ZLV_MDM_EVENTS_STORE (id01, evt_dtm, payload) VALUES (?, ?, ?)")
        .params(row.id01(), row.evtDtm(), row.payload())
        .update();
  }

  EventProcessor processor() {
    DbTime time = new DbTime(props);
    ZlvGuestModel model = TestConfig.MODEL;
    GuestMasterRepository masters = new GuestMasterRepository(jdbc, time);
    GuestChildRepository children = new GuestChildRepository(jdbc, time);
    SourceHistoryRepository sources = new SourceHistoryRepository(jdbc, time, model);
    FieldChangeRepository fields = new FieldChangeRepository(jdbc, time, model);
    return new EventProcessor(
        new EventStoreRepository(jdbc),
        model,
        masters,
        children,
        sources,
        new LoadedEventRepository(jdbc, model),
        new HistoryPlanner(TestConfig.exampleRowHash(), model),
        new HistoryWriter(masters, children, sources, fields, time),
        new EventParser(),
        new DataSourceTransactionManager(dataSource),
        time,
        props);
  }

  /** One row in the OLD unprefixed master table, to prove V002 leaves it alone. */
  void insertOldMasterRow() {
    jdbc.sql(
            "INSERT INTO EVT_MASTER_HIST (business_id, id01, valid_from, is_current)"
                + " VALUES ('old-1', 'old-id01', CURRENT_TIMESTAMP, 1)")
        .update();
  }

  long count(String sql, Object... params) {
    return jdbc.sql(sql).params(Arrays.asList(params)).query(Long.class).single();
  }

  List<String> strings(String sql, Object... params) {
    return jdbc.sql(sql).params(Arrays.asList(params)).query(String.class).list();
  }

  /** Runs a script: comment lines skipped, split on ';'. */
  void run(String script) {
    String noComments =
        script.lines().filter(l -> !l.trim().startsWith("--")).collect(Collectors.joining("\n"));
    Arrays.stream(noComments.split(";"))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .forEach(s -> jdbc.sql(s).update());
  }

  static String read(Path p) {
    try {
      return Files.readString(p, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException("cannot read " + p.toAbsolutePath(), e);
    }
  }

  static String resource(String name) {
    try (InputStream in = H2Database.class.getResourceAsStream(name)) {
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
