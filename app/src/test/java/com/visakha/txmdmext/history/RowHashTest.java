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

  private static final String PHONE = "EVT_ZLV_GUEST_MASTER_PHONE_HIST";

  private static RowHash fromConfig(Map<String, List<String>> c) {
    return RowHash.fromConfig(c, TestConfig.MODEL.hashableColumns());
  }

  @Test
  void configKeysMatchIgnoringCaseAndUnderscores() {
    Map<String, List<String>> c = new LinkedHashMap<>(TestConfig.example().md5());
    List<String> phone = c.remove(PHONE);
    c.put("evtzlvguestmasterphonehist", phone);
    assertEquals(phone, fromConfig(c).columns(PHONE));
  }

  private static Map<String, List<String>> config(String phoneColumns) {
    Map<String, List<String>> c = new LinkedHashMap<>(TestConfig.example().md5());
    c.put(PHONE, Arrays.asList(phoneColumns.split(",")));
    return c;
  }

  private static Map<String, Object> phone(String phoneNum, String typCd) {
    Map<String, Object> v = new HashMap<>();
    v.put("cntry_cd", 1L);
    v.put("phone_num", phoneNum);
    v.put("ph_typ_cd", typCd);
    v.put("is_deleted", false);
    return v;
  }

  @Test
  void sameValuesGiveTheSameHashEveryTime() {
    RowHash h = fromConfig(config("cntry_cd,phone_num,ph_typ_cd"));
    String first = h.md5(PHONE, phone("5125550701", "Mobile"));
    String second = h.md5(PHONE, phone("5125550701", "Mobile"));
    assertEquals(first, second);
    assertEquals(32, first.length());
    assertTrue(first.chars().allMatch(c -> Character.isDigit(c) || (c >= 'a' && c <= 'f')));
  }

  @Test
  void knownInputGivesKnownHash() {
    RowHash h = fromConfig(config("cntry_cd,phone_num,ph_typ_cd"));
    Map<String, Object> v = phone("5125550701", "Mobile");
    assertEquals("1|5125550701|Mobile", h.canonical(PHONE, v));
    // md5("1|5125550701|Mobile"), checked with: printf '1|5125550701|Mobile' | md5sum
    assertEquals("8bcd6984786290f2a2d764950e61585b", h.md5(PHONE, v));
  }

  @Test
  void columnOrderChangesTheHash() {
    Map<String, Object> v = phone("5125550701", "Mobile");
    String a = fromConfig(config("cntry_cd,phone_num,ph_typ_cd")).md5(PHONE, v);
    String b = fromConfig(config("ph_typ_cd,phone_num,cntry_cd")).md5(PHONE, v);
    assertNotEquals(a, b);
  }

  @Test
  void onlyListedColumnsCount() {
    RowHash h = fromConfig(config("cntry_cd,phone_num"));
    assertEquals(
        h.md5(PHONE, phone("5125550701", "Mobile")), h.md5(PHONE, phone("5125550701", "Fax")));
  }

  @Test
  void nullAndEmptyTextHashDifferently() {
    RowHash h = fromConfig(config("ph_typ_cd"));
    assertEquals("\\N", h.canonical(PHONE, phone("1", null)));
    assertNotEquals(h.md5(PHONE, phone("1", null)), h.md5(PHONE, phone("1", "")));
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
    assertEquals(TestConfig.MODEL.hashableColumns().keySet(), h.tables());
    assertEquals(
        List.of("cntry_cd", "phone_num", "ph_typ_cd", "ph_typ_nm", "no_mrkt_calls", "is_deleted"),
        h.columns(PHONE));
  }

  @Test
  void unknownColumnFailsFast() {
    IllegalStateException e =
        assertThrows(IllegalStateException.class, () -> fromConfig(config("cntry_cd,phone_nmbr")));
    assertTrue(e.getMessage().contains("unknown column 'phone_nmbr'"));
  }

  @Test
  void missingTableEntryFailsFast() {
    Map<String, List<String>> c = new LinkedHashMap<>(TestConfig.example().md5());
    c.keySet().removeIf(k -> k.toUpperCase().contains("ALT"));
    IllegalStateException e = assertThrows(IllegalStateException.class, () -> fromConfig(c));
    assertTrue(e.getMessage().contains("EVT_ZLV_GUEST_MASTER_ALT_ID_HIST is missing"));
  }

  @Test
  void unknownTableAndEmptyListFailFast() {
    Map<String, List<String>> c = new LinkedHashMap<>(TestConfig.example().md5());
    c.put("EVT_NOT_A_TABLE", List.of("x"));
    c.put("EVT_ZLV_GUEST_MEMBER_HIST", List.of());
    IllegalStateException e = assertThrows(IllegalStateException.class, () -> fromConfig(c));
    assertTrue(e.getMessage().contains("not a Type 2 history table"));
    assertTrue(e.getMessage().contains("column list is empty"));
  }
}
