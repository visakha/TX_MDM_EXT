package com.visakha.txmdmext.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.visakha.txmdmext.config.TestConfig;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class RowHashTest {

  private static Map<String, List<String>> config(String phoneColumns) {
    Map<String, List<String>> c = new LinkedHashMap<>(TestConfig.example().md5());
    c.put("EVT_MASTER_PHONE_HIST", Arrays.asList(phoneColumns.split(",")));
    return c;
  }

  private static Map<String, Object> phone(Long phNum, String typCd) {
    Map<String, Object> v = new HashMap<>();
    v.put("cntry_cd", 1L);
    v.put("ph_num", phNum);
    v.put("ph_typ_cd", typCd);
    v.put("is_deleted", false);
    return v;
  }

  @Test
  void sameValuesGiveTheSameHashEveryTime() {
    RowHash h = RowHash.fromConfig(config("cntry_cd,ph_num,ph_typ_cd"));
    String first = h.md5(HistTable.EVT_MASTER_PHONE_HIST, phone(5125550701L, "Mobile"));
    String second = h.md5(HistTable.EVT_MASTER_PHONE_HIST, phone(5125550701L, "Mobile"));
    assertEquals(first, second);
    assertEquals(32, first.length());
    assertTrue(first.chars().allMatch(c -> Character.isDigit(c) || (c >= 'a' && c <= 'f')));
  }

  @Test
  void knownInputGivesKnownHash() {
    RowHash h = RowHash.fromConfig(config("cntry_cd,ph_num,ph_typ_cd"));
    Map<String, Object> v = phone(5125550701L, "Mobile");
    assertEquals("1|5125550701|Mobile", h.canonical(HistTable.EVT_MASTER_PHONE_HIST, v));
    // md5("1|5125550701|Mobile"), checked with: printf '1|5125550701|Mobile' | md5sum
    assertEquals("8bcd6984786290f2a2d764950e61585b", h.md5(HistTable.EVT_MASTER_PHONE_HIST, v));
  }

  @Test
  void columnOrderChangesTheHash() {
    Map<String, Object> v = phone(5125550701L, "Mobile");
    String a =
        RowHash.fromConfig(config("cntry_cd,ph_num,ph_typ_cd"))
            .md5(HistTable.EVT_MASTER_PHONE_HIST, v);
    String b =
        RowHash.fromConfig(config("ph_typ_cd,ph_num,cntry_cd"))
            .md5(HistTable.EVT_MASTER_PHONE_HIST, v);
    assertNotEquals(a, b);
  }

  @Test
  void onlyListedColumnsCount() {
    RowHash h = RowHash.fromConfig(config("cntry_cd,ph_num"));
    assertEquals(
        h.md5(HistTable.EVT_MASTER_PHONE_HIST, phone(5125550701L, "Mobile")),
        h.md5(HistTable.EVT_MASTER_PHONE_HIST, phone(5125550701L, "Fax")));
  }

  @Test
  void nullAndEmptyTextHashDifferently() {
    RowHash h = RowHash.fromConfig(config("ph_typ_cd"));
    assertEquals("\\N", h.canonical(HistTable.EVT_MASTER_PHONE_HIST, phone(1L, null)));
    assertNotEquals(
        h.md5(HistTable.EVT_MASTER_PHONE_HIST, phone(1L, null)),
        h.md5(HistTable.EVT_MASTER_PHONE_HIST, phone(1L, "")));
  }

  @Test
  void textIsTrimmedAndDelimiterEscaped() {
    assertEquals("Mobile", RowHash.normalize("  Mobile "));
    assertEquals("a\\|b", RowHash.normalize("a|b"));
    assertNotEquals(
        RowHash.normalize("a|b"), RowHash.normalize("a") + "|" + RowHash.normalize("b"));
  }

  @Test
  void valuesAreNormalizedByType() {
    assertEquals("1", RowHash.normalize(Boolean.TRUE));
    assertEquals("0", RowHash.normalize(Boolean.FALSE));
    assertEquals("1.5", RowHash.normalize(1.50d));
    assertEquals("12334455001", RowHash.normalize(12334455001L));
    assertEquals("2026-11-27", RowHash.normalize(LocalDate.of(2026, 11, 27)));
    assertEquals(
        "2026-11-27T06:19:51.827Z", RowHash.normalize(Instant.ofEpochMilli(1795760391827L)));
  }

  @Test
  void exampleFileFormulasAreValid() {
    RowHash h = TestConfig.exampleRowHash();
    assertEquals(HistTable.values().length, h.tables().size());
    assertEquals(
        List.of("cntry_cd", "ph_num", "ph_typ_cd", "ph_typ_nm", "no_mrkt_calls", "is_deleted"),
        h.columns(HistTable.EVT_MASTER_PHONE_HIST));
  }

  @Test
  void unknownColumnFailsFast() {
    IllegalStateException e =
        assertThrows(
            IllegalStateException.class, () -> RowHash.fromConfig(config("cntry_cd,phone_nmbr")));
    assertTrue(e.getMessage().contains("unknown column 'phone_nmbr'"));
  }

  @Test
  void missingTableEntryFailsFast() {
    Map<String, List<String>> c = new LinkedHashMap<>(TestConfig.example().md5());
    c.keySet().removeIf(k -> k.toUpperCase().contains("ALT"));
    IllegalStateException e =
        assertThrows(IllegalStateException.class, () -> RowHash.fromConfig(c));
    assertTrue(e.getMessage().contains("EVT_MASTER_ALT_ID_HIST is missing"));
  }

  @Test
  void unknownTableAndEmptyListFailFast() {
    Map<String, List<String>> c = new LinkedHashMap<>(TestConfig.example().md5());
    c.put("EVT_NOT_A_TABLE", List.of("x"));
    c.put("EVT_MEMBER_HIST", List.of());
    IllegalStateException e =
        assertThrows(IllegalStateException.class, () -> RowHash.fromConfig(c));
    assertTrue(e.getMessage().contains("not a Type 2 history table"));
    assertTrue(e.getMessage().contains("column list is empty"));
  }

  @Test
  void everyKnownColumnHasAValue() {
    // Guards HistTable against HashValues drifting apart.
    MasterVersion m =
        HistoryPlanner.masterRow(
            com.visakha.txmdmext.event.Fixtures.orderedEvents().getFirst(), Instant.EPOCH);
    assertEquals(
        HistTable.EVT_MASTER_HIST.knownColumns(), List.copyOf(HashValues.master(m).keySet()));
    ChildRow.Phone p = new ChildRow.Phone("x", 1L, 2L, null, null);
    assertEquals(
        HistTable.EVT_MASTER_PHONE_HIST.knownColumns(),
        List.copyOf(HashValues.child("b", p, false).keySet()));
    ChildRow.SrcAddress s =
        new ChildRow.SrcAddress(
            "x", null, null, null, null, null, null, null, null, null, null, null, null, null);
    assertEquals(
        HistTable.EVT_MASTER_SRC_ADDR_HIST.knownColumns(),
        List.copyOf(HashValues.child("b", s, false).keySet()));
    ChildRow.Address a =
        new ChildRow.Address(
            "x", null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, null, null, null, null);
    assertEquals(
        HistTable.EVT_MASTER_ADDR_HIST.knownColumns(),
        List.copyOf(HashValues.child("b", a, false).keySet()));
    ChildRow.AltId alt = new ChildRow.AltId("x", null, null, null, null, null, null, null);
    assertEquals(
        HistTable.EVT_MASTER_ALT_ID_HIST.knownColumns(),
        List.copyOf(HashValues.child("b", alt, false).keySet()));
  }
}
