package com.visakha.txmdmext.it;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.visakha.txmdmext.event.BusinessEvent;
import com.visakha.txmdmext.event.Fixtures;
import com.visakha.txmdmext.job.BatchResult;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

/**
 * End to end on H2 (Oracle mode): 44 real rows in, the real EventProcessor and SQL run, then the
 * actual rows of every history table are checked.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class H2HistoryIT {

  static final List<String> TABLES =
      List.of(
          "EVT_ZLV_GUEST_MASTER_HIST",
          "EVT_ZLV_GUEST_MASTER_PHONE_HIST",
          "EVT_ZLV_GUEST_MASTER_SRC_ADDR_HIST",
          "EVT_ZLV_GUEST_MASTER_ADDR_HIST",
          "EVT_ZLV_GUEST_MASTER_ALT_ID_HIST",
          "EVT_ZLV_GUEST_FIELD_CHANGE",
          "EVT_ZLV_GUEST_MEMBER_HIST",
          "EVT_ZLV_GUEST_XREF_CHANGE",
          "EVT_ZLV_GUEST_SOURCE_HIST");

  private H2Database db;
  private BatchResult result;
  private final List<BusinessEvent> events = Fixtures.orderedEvents();

  @BeforeAll
  void loadRealEvents() {
    db = new H2Database().withHistorySchema().withEventStore().withRealEvents();
    result = db.processor().runOnce();
    TABLES.forEach(
        t -> System.out.println("H2 rows " + t + " = " + db.count("SELECT COUNT(*) FROM " + t)));
  }

  private Set<String> currentMembers(String bid) {
    return Set.copyOf(
        db.strings(
            "SELECT source_system || ':' || source_pkey FROM EVT_ZLV_GUEST_MEMBER_HIST"
                + " WHERE business_id = ? AND is_current = 1",
            bid));
  }

  @Test
  void dedupesTo36EventsAndLoadsThemAll() {
    assertEquals(44, result.rowsRead());
    assertEquals(36, result.distinctEvents());
    assertEquals(36, result.loaded());
    assertEquals(0, result.failed());
  }

  @Test
  void processedDtmIsStampedOnAll44Rows() {
    assertEquals(
        0, db.count("SELECT COUNT(*) FROM ZLV_MDM_EVENTS_STORE WHERE processed_dtm IS NULL"));
    assertEquals(44, db.count("SELECT COUNT(*) FROM ZLV_MDM_EVENTS_STORE"));
  }

  @Test
  void everyMasterHasExactlyOneCurrentRow() {
    assertEquals(
        0,
        db.count(
            "SELECT COUNT(*) FROM (SELECT business_id FROM EVT_ZLV_GUEST_MASTER_HIST"
                + " GROUP BY business_id HAVING SUM(is_current) <> 1)"));
    // 9 use-case masters (162 163 164 172 173 183 184 192 193) + 4 older samples.
    assertEquals(13, db.count("SELECT COUNT(DISTINCT business_id) FROM EVT_ZLV_GUEST_MASTER_HIST"));
  }

  @Test
  void closedMasterRowsHaveValidTo() {
    assertEquals(
        0,
        db.count(
            "SELECT COUNT(*) FROM EVT_ZLV_GUEST_MASTER_HIST WHERE is_current = 0 AND valid_to IS"
                + " NULL"));
    assertEquals(
        0,
        db.count(
            "SELECT COUNT(*) FROM EVT_ZLV_GUEST_MASTER_HIST WHERE is_current = 1 AND valid_to IS"
                + " NOT NULL"));
  }

  @Test
  void uc12aMergeMembership() {
    assertEquals(
        Set.of("zlv_ttf:EVT-UC12A-TTF-1", "zlv_ttf:EVT-UC12A-TTF-2"),
        currentMembers("100000000000162"));
    assertEquals(Set.of(), currentMembers("100000000000173"));
  }

  @Test
  void uc12bThenUc13EciMembership() {
    assertEquals(Set.of("zlv_eci:EVT-UC12B-ECI-1"), currentMembers("100000000000193"));
    assertEquals(Set.of("zlv_eci:EVT-UC12B-ECI-2"), currentMembers("100000000000163"));
    assertEquals(
        2,
        db.count(
            "SELECT COUNT(*) FROM EVT_ZLV_GUEST_MEMBER_HIST WHERE source_pkey = 'EVT-UC12B-ECI-2'"
                + " AND is_current = 0 AND id01_closed IS NOT NULL"));
  }

  @Test
  void uc12cThenUc13TtfMembership() {
    assertEquals(Set.of("zlv_ttf:EVT-UC12C-TTF-1"), currentMembers("100000000000184"));
    assertEquals(Set.of("zlv_eci:EVT-UC12C-ECI-1"), currentMembers("100000000000164"));
    assertEquals(
        "REMOVED",
        db.strings(
                "SELECT membership_change FROM EVT_ZLV_GUEST_XREF_CHANGE WHERE business_id = ?"
                    + " AND source_pkey = 'EVT-UC12C-ECI-1' AND membership_change = 'REMOVED'",
                "100000000000184")
            .getFirst());
    assertTrue(
        db.strings(
                "SELECT id01_closed FROM EVT_ZLV_GUEST_MEMBER_HIST WHERE business_id ="
                    + " '100000000000184' AND source_pkey = 'EVT-UC12C-ECI-1'")
            .getFirst()
            .startsWith("6b0921c6"));
  }

  @Test
  void uc16PhoneClosedThenDeletedVersion() {
    List<String> rows =
        db.strings(
            "SELECT is_current || ',' || is_deleted || ',' || CASE WHEN valid_to IS NULL THEN"
                + " 'open' ELSE 'closed' END FROM EVT_ZLV_GUEST_MASTER_PHONE_HIST WHERE business_id"
                + " = ? AND child_id = ? ORDER BY hist_id",
            "100000000000172",
            "2dca242eedb34fa98eacbaa17dd87212");
    assertEquals(List.of("0,0,closed", "1,1,open"), rows);
  }

  @Test
  void uc16aSourceAddressClosedThenDeletedVersion() {
    List<String> rows =
        db.strings(
            "SELECT is_current || ',' || is_deleted FROM EVT_ZLV_GUEST_MASTER_SRC_ADDR_HIST"
                + " WHERE business_id = ? AND child_id = ? ORDER BY hist_id",
            "100000000000192",
            "d96a50d6c077405b893085920789e242");
    assertEquals(List.of("0,0", "1,1"), rows);
  }

  @Test
  void uc20LookupStoredAsCodeAndName() {
    List<String> rows =
        db.strings(
            "SELECT ph_typ_cd || '/' || ph_typ_nm || '/' || is_current FROM"
                + " EVT_ZLV_GUEST_MASTER_PHONE_HIST WHERE business_id = ? AND child_id = ? ORDER BY"
                + " hist_id",
            "100000000000192",
            "7ddf5b15d8e24f468ecbc53c60c5f812");
    assertEquals(List.of("Mobile/Mobile/0", "Fax/Fax/1"), rows);
  }

  @Test
  void fieldChangeRowsMatchDataDiffTotals() {
    long diffTotal = events.stream().mapToLong(e -> e.dataDiff().size()).sum();
    assertEquals(diffTotal, db.count("SELECT COUNT(*) FROM EVT_ZLV_GUEST_FIELD_CHANGE"));
    assertEquals(
        1,
        db.count(
            "SELECT COUNT(*) FROM EVT_ZLV_GUEST_FIELD_CHANGE WHERE field_name = 'last_name' AND"
                + " change_type = 'DELETED' AND new_value IS NULL AND old_value = 'UC7eciUpd'"));
  }

  @Test
  void xrefChangesKeyedBySourceAndEventMaster() {
    long xrefTotal = events.stream().mapToLong(e -> e.affectedXrefs().size()).sum();
    assertEquals(xrefTotal, db.count("SELECT COUNT(*) FROM EVT_ZLV_GUEST_XREF_CHANGE"));
    // UC26: xref businessId in the event is 164, the row carries the event master 184.
    assertEquals(
        "100000000000184",
        db.strings("SELECT business_id FROM EVT_ZLV_GUEST_XREF_CHANGE WHERE id01 LIKE '6b0921bc%'")
            .getFirst());
  }

  @Test
  void rowMd5IsFilledOnEveryTypeTwoRow() {
    for (String t :
        List.of(
            "EVT_ZLV_GUEST_MASTER_HIST",
            "EVT_ZLV_GUEST_MASTER_PHONE_HIST",
            "EVT_ZLV_GUEST_MASTER_SRC_ADDR_HIST",
            "EVT_ZLV_GUEST_MASTER_ALT_ID_HIST",
            "EVT_ZLV_GUEST_MEMBER_HIST")) {
      assertEquals(0, db.count("SELECT COUNT(*) FROM " + t + " WHERE row_md5 IS NULL"), t);
      assertTrue(db.count("SELECT COUNT(*) FROM " + t) > 0, t);
    }
  }

  @Test
  void unchangedContentWritesNoMasterVersion() {
    // UC26 event 6b0921bc: empty dataDiff, same content -> same row_md5 -> no master row.
    assertEquals(
        0, db.count("SELECT COUNT(*) FROM EVT_ZLV_GUEST_MASTER_HIST WHERE id01 LIKE '6b0921bc%'"));
    // Two consecutive versions of one master never share a hash (A-B-A is allowed: 163, 193).
    assertEquals(
        0,
        db.count(
            "SELECT COUNT(*) FROM EVT_ZLV_GUEST_MASTER_HIST a JOIN EVT_ZLV_GUEST_MASTER_HIST b"
                + " ON a.business_id = b.business_id AND a.valid_to = b.valid_from"
                + " AND a.is_current = 0 WHERE a.row_md5 = b.row_md5"));
  }

  @Test
  void eventSourceHistIsNotFilledYet() {
    assertEquals(0, db.count("SELECT COUNT(*) FROM EVT_ZLV_GUEST_SOURCE_HIST"));
  }
}
