package com.visakha.txmdmext.store;

import com.visakha.txmdmext.model.EntityModel;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Idempotency: has any history table of the model already got a row from this ID01? With row_md5
 * CDC an event may write no master row, so every table is checked.
 */
@Repository
public class LoadedEventRepository {

  static final String COUNT_OF_TABLE = "(SELECT COUNT(*) FROM %s WHERE id01 = :id01)";

  static final String COUNT_OF_MEMBERS =
      "(SELECT COUNT(*) FROM %s WHERE id01_opened = :id01 OR id01_closed = :id01)";

  static final String COUNT_ROWS_OF_ID01 = "SELECT %s AS row_count FROM DUAL";

  private final JdbcClient jdbc;
  private final String countRowsOfId01;

  public LoadedEventRepository(JdbcClient jdbc, EntityModel model) {
    this.jdbc = jdbc;
    // Built once at startup from the model's table names (constants, validated), not from input.
    Stream<String> byId01 =
        Stream.concat(
                Stream.concat(
                    Stream.of(model.masterTable()),
                    model.childGroups().stream().map(g -> g.table())),
                Stream.of(model.fieldChangeTable(), model.xrefTable()))
            .map(t -> COUNT_OF_TABLE.formatted(SqlNames.table(t)));
    Stream<String> members =
        Stream.of(COUNT_OF_MEMBERS.formatted(SqlNames.table(model.memberTable())));
    this.countRowsOfId01 =
        COUNT_ROWS_OF_ID01.formatted(
            Stream.concat(byId01, members).collect(Collectors.joining("\n     + ")));
  }

  /** The SQL as built, for tests and logs. */
  public String sql() {
    return countRowsOfId01;
  }

  public boolean alreadyLoaded(String id01) {
    Number count = jdbc.sql(countRowsOfId01).param("id01", id01).query(Number.class).single();
    return count != null && count.longValue() > 0;
  }
}
