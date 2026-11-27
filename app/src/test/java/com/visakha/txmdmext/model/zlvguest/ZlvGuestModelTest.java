package com.visakha.txmdmext.model.zlvguest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.visakha.txmdmext.config.TestConfig;
import com.visakha.txmdmext.event.Fixtures;
import com.visakha.txmdmext.history.HistoryPlanner;
import com.visakha.txmdmext.history.MasterVersion;
import com.visakha.txmdmext.model.zlvguest.entity.GuestFieldChange;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterAddrHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterAltIdHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterPhoneHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterSrcAddrHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMemberHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestSourceHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestXrefChange;
import com.visakha.txmdmext.model.zlvguest.entity.ZlvMdmEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * The zlv_guest model: entities match the DDL, hashable columns, and the engine stays guest-free.
 */
class ZlvGuestModelTest {

  private final ZlvGuestModel model = new ZlvGuestModel();

  @Test
  void everyTableCarriesTheDomainPrefix() {
    List<String> tables =
        Stream.concat(
                Stream.of(
                    model.masterTable(),
                    model.fieldChangeTable(),
                    model.memberTable(),
                    model.xrefTable(),
                    model.sourceTable()),
                model.childGroups().stream().map(g -> g.table()))
            .toList();
    assertEquals(9, tables.size());
    assertTrue(tables.stream().allMatch(t -> t.startsWith("EVT_ZLV_GUEST_")));
    assertEquals("EVT_ZLV_GUEST_MEMBER_HIST", model.memberTable());
    assertEquals(34, GuestMasterSrcAddrHist.TABLE.length());
    assertTrue(tables.stream().allMatch(t -> t.length() <= 128));
  }

  @Test
  void everyTableIsInTheDdl() throws IOException {
    String v001 = Files.readString(Path.of("..", "ddl", "V001__history_tables.sql"));
    String v002 = Files.readString(Path.of("..", "ddl", "V002__zlv_guest_tables.sql"));
    model
        .hashableColumns()
        .keySet()
        .forEach(t -> {
          assertTrue(v001.contains("CREATE TABLE " + t + " ("), t);
          assertTrue(v002.contains("CREATE TABLE " + t + " ("), t);
        });
  }

  @Test
  void hashValuesCoverExactlyTheHashableColumns() {
    // Guards GuestHashColumns against GuestHashValues drifting apart.
    MasterVersion m =
        new HistoryPlanner(TestConfig.exampleRowHash(), model)
            .masterRow(Fixtures.orderedEvents().getFirst(), Instant.EPOCH);
    Map<String, List<String>> known = model.hashableColumns();
    assertEquals(known.get(GuestMasterHist.TABLE), List.copyOf(GuestHashValues.master(m).keySet()));
    assertChild(known, new GuestChild.Phone("x", 1L, "2", null, null));
    assertChild(
        known,
        new GuestChild.SrcAddress(
            "x", null, null, null, null, null, null, null, null, null, null, null, null, null));
    assertChild(
        known,
        new GuestChild.Address(
            "x", null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null));
    assertChild(known, new GuestChild.AltId("x", null, null, null, null, null, null, null));
  }

  private static void assertChild(Map<String, List<String>> known, GuestChild row) {
    assertEquals(
        known.get(row.group().table()),
        List.copyOf(GuestHashValues.child("b", row, false).keySet()));
  }

  static final List<Class<? extends Record>> ENTITIES =
      List.of(
          GuestMasterHist.class,
          GuestMasterPhoneHist.class,
          GuestMasterSrcAddrHist.class,
          GuestMasterAddrHist.class,
          GuestMasterAltIdHist.class,
          GuestFieldChange.class,
          GuestMemberHist.class,
          GuestXrefChange.class,
          GuestSourceHist.class);

  @Test
  void everyEntityMapsItsV002TableColumnForColumn() throws IOException {
    // V002 creates the tables, V003 adds phone_num. Unmapped on purpose: legacy ph_num only.
    String ddl =
        Files.readString(Path.of("..", "ddl", "V002__zlv_guest_tables.sql"))
            + Files.readString(Path.of("..", "ddl", "V003__zlv_guest_phone_num.sql"));
    for (Class<? extends Record> entity : ENTITIES) {
      String table = entity.getAnnotation(Table.class).value();
      List<String> mapped = columns(entity);
      List<String> inDdl = ddlColumns(ddl, table);
      List<String> missing = inDdl.stream().filter(c -> !mapped.contains(c)).toList();
      List<String> unknown = mapped.stream().filter(c -> !inDdl.contains(c)).toList();
      assertEquals(List.of(), unknown, table + " entity columns not in the DDL");
      assertEquals(
          table.equals(GuestMasterPhoneHist.TABLE) ? List.of("ph_num") : List.of(),
          missing,
          table + " DDL columns not in the entity");
    }
    assertEquals(9, ENTITIES.size());
    assertEquals("ZLV_MDM_EVENTS_STORE", ZlvMdmEvent.class.getAnnotation(Table.class).value());
    assertEquals(
        Set.of("id01", "evt_dtm", "evt_dtm_tmz", "payload", "processed_dtm"),
        Set.copyOf(columns(ZlvMdmEvent.class)));
  }

  /** The @Column names of an entity record (record component annotations land on its fields). */
  private static List<String> columns(Class<? extends Record> entity) {
    return Arrays.stream(entity.getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(Column.class))
        .map(f -> f.getAnnotation(Column.class).value())
        .toList();
  }

  /** Column names of one CREATE TABLE (or ALTER TABLE ... ADD) in the DDL text, lower case. */
  private static List<String> ddlColumns(String ddl, String table) {
    int start = ddl.indexOf("CREATE TABLE " + table + " (");
    String body = ddl.substring(start, ddl.indexOf(");", start));
    Stream<String> created =
        body.lines()
            .skip(1)
            .map(String::trim)
            .filter(l -> !l.isEmpty() && !l.startsWith("CONSTRAINT"))
            .map(l -> l.split("\\s+")[0]);
    Stream<String> added =
        ddl.lines()
            .filter(l -> l.startsWith("ALTER TABLE " + table + " ADD "))
            .map(l -> l.substring(("ALTER TABLE " + table + " ADD ").length()).split("\\s+")[0]);
    return Stream.concat(created, added).toList();
  }

  @Test
  void genericEngineNamesNoGuestFieldOrTable() throws IOException {
    Path main = Path.of("src", "main", "java", "com", "visakha", "txmdmext");
    Path guest = main.resolve(Path.of("model", "zlvguest"));
    try (Stream<Path> files = Files.walk(main)) {
      List<String> offenders =
          files
              .filter(p -> p.toString().endsWith(".java") && !p.startsWith(guest))
              .filter(p -> mentionsGuest(p))
              .map(Path::toString)
              .toList();
      assertEquals(List.of(), offenders);
    }
  }

  private static boolean mentionsGuest(Path p) {
    try {
      String s = Files.readString(p);
      // Imports of the guest package, guest table names, or guest field names in SQL / code.
      return s.contains("import com.visakha.txmdmext.model.zlvguest")
          || s.contains("EVT_ZLV_GUEST_")
          || s.contains("\"first_name\"")
          || s.contains("first_name,")
          || s.contains("\"x_phone\"");
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }
}
