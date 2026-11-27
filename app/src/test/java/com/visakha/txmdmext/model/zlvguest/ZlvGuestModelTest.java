package com.visakha.txmdmext.model.zlvguest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.visakha.txmdmext.config.TestConfig;
import com.visakha.txmdmext.event.Fixtures;
import com.visakha.txmdmext.history.HistoryPlanner;
import com.visakha.txmdmext.history.MasterVersion;
import com.visakha.txmdmext.store.LoadedEventRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/** The zlv_guest model: table names, hashable columns, and that the engine stays guest-free. */
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
    assertEquals(34, GuestTables.MASTER_SRC_ADDR.length());
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
    // Guards GuestTables against GuestHashValues drifting apart.
    MasterVersion m =
        new HistoryPlanner(TestConfig.exampleRowHash(), model)
            .masterRow(Fixtures.orderedEvents().getFirst(), Instant.EPOCH);
    Map<String, List<String>> known = model.hashableColumns();
    assertEquals(known.get(GuestTables.MASTER), List.copyOf(GuestHashValues.master(m).keySet()));
    assertChild(known, new GuestChild.Phone("x", 1L, 2L, null, null));
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

  @Test
  void idempotencyCheckCoversEveryWrittenTable() {
    String sql = new LoadedEventRepository(null, model).sql();
    Stream.of(
            GuestTables.MASTER,
            GuestTables.MASTER_PHONE,
            GuestTables.MASTER_SRC_ADDR,
            GuestTables.MASTER_ADDR,
            GuestTables.MASTER_ALT_ID,
            GuestTables.FIELD_CHANGE,
            GuestTables.XREF_CHANGE,
            GuestTables.MEMBER)
        .forEach(t -> assertTrue(sql.contains("FROM " + t + " WHERE"), t));
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
