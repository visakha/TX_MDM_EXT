package com.visakha.txmdmext.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.visakha.txmdmext.event.Fixtures;
import com.visakha.txmdmext.model.zlvguest.FieldGroup;
import com.visakha.txmdmext.model.zlvguest.GuestChild;
import com.visakha.txmdmext.model.zlvguest.GuestRoot;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** One test per real use case of internal-docs/USE_CASES.md, on the 36 real events. */
class UseCaseTest {

  private static final String B172 = "100000000000172";
  private static final String B192 = "100000000000192";
  private static final MemberKey TTF_7 = new MemberKey("zlv_ttf", "EVT-UC7-TTF-1");
  private static final MemberKey ECI_7 = new MemberKey("zlv_eci", "EVT-UC7-ECI-1");

  private static InMemoryHistory h;

  @BeforeAll
  static void replay() {
    h = new InMemoryHistory();
    h.replay(Fixtures.orderedEvents());
  }

  private static XrefChangeRow xref(String id01Prefix, String businessId) {
    List<XrefChangeRow> rows = h.planOf(id01Prefix, businessId).xrefChanges();
    assertEquals(1, rows.size());
    return rows.getFirst();
  }

  private static FieldChangeRow change(String id01Prefix, String businessId, String path) {
    return h.planOf(id01Prefix, businessId).fieldChanges().stream()
        .filter(c -> c.fieldPath().equals(path))
        .findFirst()
        .orElseThrow();
  }

  private static ChildVersion onlyChild(String id01Prefix, String businessId) {
    List<ChildVersion> v = h.planOf(id01Prefix, businessId).childVersions();
    assertEquals(1, v.size());
    return v.getFirst();
  }

  private static Set<MemberKey> members(String bid) {
    return h.openMembers.get(bid);
  }

  @Test
  void uc6UiCreateViaC360DefaultSystem() {
    HistoryPlan p = h.planOf("6b092127", "100000000000183");
    assertNotNull(p.master());
    assertEquals("UC6ui", ((GuestRoot) p.master().root()).lastName());
    assertTrue(p.childVersions().isEmpty());
    XrefChangeRow x = xref("6b092127", "100000000000183");
    assertEquals("c360.default.system", x.sourceSystem());
    assertEquals("EVT-UC6-UI-1", x.sourcePkey());
    // Real event says DATA (USE_CASES.md guessed DATA; the older UI edits are PATCH).
    assertEquals("DATA", x.xrefType());
    assertEquals(
        Set.of(new MemberKey("c360.default.system", "EVT-UC6-UI-1")), members("100000000000183"));
  }

  @Test
  void uc7TtfCreateWithPhone() {
    HistoryPlan p = h.planOf("6b092107", B172);
    assertEquals("MASTER_CREATED", p.master().eventType());
    XrefChangeRow x = xref("6b092107", B172);
    assertEquals("zlv_ttf", x.sourceSystem());
    assertEquals("DATA", x.xrefType());
    assertEquals("CREATED", x.xrefChange());
    assertEquals("ADDED", x.membershipChange());
    GuestChild.Phone ph = (GuestChild.Phone) onlyChild("6b092107", B172).row();
    assertEquals("479cb3c697d943699bf266ca9a23a89e", ph.childId());
    assertEquals("5125550701", ph.phoneNum());
    assertEquals("Mobile", ph.phTyp().code());
    assertEquals(List.of(TTF_7), p.membersToOpen().stream().map(MemberVersion::key).toList());
  }

  @Test
  void uc7EciCreateWithPhone() {
    XrefChangeRow x = xref("6b092124", B192);
    assertEquals("zlv_eci", x.sourceSystem());
    assertEquals("EVT-UC7-ECI-1", x.sourcePkey());
    assertEquals("DATA", x.xrefType());
    GuestChild.Phone ph = (GuestChild.Phone) onlyChild("6b092124", B192).row();
    assertEquals("7ddf5b15d8e24f468ecbc53c60c5f812", ph.childId());
    assertEquals("5125550702", ph.phoneNum());
    assertEquals(Set.of(ECI_7), members(B192));
  }

  @Test
  void uc8TtfRootUpdate() {
    HistoryPlan p = h.planOf("6b092144", B172);
    assertEquals("UC7ttfUpd", ((GuestRoot) p.master().root()).lastName());
    assertEquals(2, p.fieldChanges().size());
    FieldChangeRow c = change("6b092144", B172, "last_name");
    assertEquals("UC7ttf", c.oldValue());
    assertEquals("UC7ttfUpd", c.newValue());
    assertEquals("UPDATED", xref("6b092144", B172).xrefChange());
    assertTrue(p.membersToOpen().isEmpty() && p.membersToClose().isEmpty());
  }

  @Test
  void uc8EciRootUpdate() {
    HistoryPlan p = h.planOf("6b09214e", B192);
    assertEquals("UC7eciUpd", ((GuestRoot) p.master().root()).lastName());
    assertEquals("UC7eci", change("6b09214e", B192, "last_name").oldValue());
    assertEquals("zlv_eci", xref("6b09214e", B192).sourceSystem());
  }

  @Test
  void uc5TtfAddSourceAddress() {
    ChildVersion v = onlyChild("6b092152", B172);
    GuestChild.SrcAddress a = (GuestChild.SrcAddress) v.row();
    assertFalse(v.deleted());
    assertEquals("7da173a5742d4f689b6b08acccbc26e2", a.childId());
    assertEquals("100 Test Ln", a.addressLine1());
    assertEquals("Austin", a.city());
    assertEquals("AZ", a.stateCd().code());
    assertEquals("US", a.countryCd().code());
    assertEquals("AM2", a.addressTyp().code());
  }

  @Test
  void uc5EciAddSourceAddress() {
    GuestChild.SrcAddress a = (GuestChild.SrcAddress) onlyChild("6b092155", B192).row();
    assertEquals("d96a50d6c077405b893085920789e242", a.childId());
    assertEquals("200 Test Ln", a.addressLine1());
  }

  @Test
  void uc17TtfAddSecondPhoneLeavesFirstUntouched() {
    ChildVersion v = onlyChild("6b092158", B172);
    assertEquals("2dca242eedb34fa98eacbaa17dd87212", v.row().childId());
    assertEquals("5125550711", ((GuestChild.Phone) v.row()).phoneNum());
    assertEquals(1, h.childEventsOf(B172, "479cb3c697d943699bf266ca9a23a89e").size());
  }

  @Test
  void uc16TtfRemovePhone() {
    ChildVersion v = onlyChild("6b092171", B172);
    assertEquals("2dca242eedb34fa98eacbaa17dd87212", v.row().childId());
    assertTrue(v.deleted());
    FieldChangeRow c = change("6b092171", B172, "x_phone[2dca242eedb34fa98eacbaa17dd87212].phone_num");
    assertEquals("DELETED", c.changeType());
    assertNull(c.newValue());
  }

  @Test
  void uc16aEciRemoveSourceAddress() {
    ChildVersion v = onlyChild("6b092175", B192);
    assertEquals("d96a50d6c077405b893085920789e242", v.row().childId());
    assertTrue(v.deleted());
    assertEquals(0, h.openChildren(B192, FieldGroup.X_SRC_ADDRESS));
  }

  @Test
  void uc19TtfAddAltIdTin() {
    GuestChild.AltId a = (GuestChild.AltId) onlyChild("6b092178", B172).row();
    assertEquals("d1fa3e1963554a8b85bb451fec0c4fa3", a.childId());
    assertEquals("9999", a.altIdVal());
    assertEquals("TIN", a.altIdTyp().code());
    assertEquals(Boolean.TRUE, a.activeInd());
    assertEquals(LocalDate.of(2026, 11, 27), a.startDate());
    assertEquals("UHC", a.priSegmentTyp().code());
    assertEquals("Income", a.secSegmentTyp().code());
  }

  @Test
  void uc19FinalStateOf172() {
    assertEquals(1, h.openChildren(B172, FieldGroup.X_PHONE));
    assertEquals(1, h.openChildren(B172, FieldGroup.X_SRC_ADDRESS));
    assertEquals(1, h.openChildren(B172, FieldGroup.X_ALT_ID));
    assertEquals("UC7ttfUpd", ((GuestRoot) h.currentMaster(B172).root()).lastName());
  }

  @Test
  void uc20EciLookupMobileToFax() {
    ChildVersion v = onlyChild("6b09217c", B192);
    GuestChild.Phone ph = (GuestChild.Phone) v.row();
    assertEquals("7ddf5b15d8e24f468ecbc53c60c5f812", ph.childId());
    assertEquals(new Lookup("Fax", "Fax"), ph.phTyp());
    FieldChangeRow c =
        change("6b09217c", B192, "x_phone[7ddf5b15d8e24f468ecbc53c60c5f812].ph_typ.Code");
    assertEquals("Mobile", c.oldValue());
    assertEquals("Fax", c.newValue());
    assertEquals("ph_typ.Code", c.fieldName());
    assertEquals("x_phone", c.fieldGroup());
  }

  @Test
  void uc22EciBooleanTrueAndNumberCreated() {
    FieldChangeRow dnc = change("6b09217f", B192, "do_not_contact_ind");
    FieldChangeRow incr = change("6b09217f", B192, "touch_incr");
    assertEquals("CREATED", dnc.changeType());
    assertEquals("true", dnc.newValue());
    assertEquals("CREATED", incr.changeType());
    assertEquals("1.5", incr.newValue());
    GuestRoot r = (GuestRoot) h.planOf("6b09217f", B192).master().root();
    assertEquals(Boolean.TRUE, r.doNotContactInd());
    assertEquals(1.5d, r.touchIncr());
  }

  @Test
  void uc21EciClearLastName() {
    FieldChangeRow c = change("6b092182", B192, "last_name");
    assertEquals("DELETED", c.changeType());
    assertEquals("UC7eciUpd", c.oldValue());
    assertNull(c.newValue());
    assertNull(((GuestRoot) h.planOf("6b092182", B192).master().root()).lastName());
  }

  @Test
  void uc21FinalStateOf192() {
    GuestRoot r = (GuestRoot) h.currentMaster(B192).root();
    assertNull(r.lastName());
    assertEquals(Boolean.TRUE, r.doNotContactInd());
    assertEquals(1.5d, r.touchIncr());
    assertEquals(1, h.openChildren(B192, FieldGroup.X_PHONE));
    assertEquals(0, h.openChildren(B192, FieldGroup.X_SRC_ADDRESS));
  }

  @Test
  void uc12aMergeTtfTtf() {
    HistoryPlan survivor = h.planOf("6b09219c", "100000000000162");
    HistoryPlan loser = h.planOf("6b09219c", "100000000000173");
    MemberKey ttf2 = new MemberKey("zlv_ttf", "EVT-UC12A-TTF-2");
    assertEquals(List.of(ttf2), survivor.membersToOpen().stream().map(MemberVersion::key).toList());
    assertEquals(List.of(ttf2), loser.membersToClose());
    assertEquals("100000000000162", loser.master().activeBusinessId());
    assertEquals("MASTER_MERGED", loser.master().eventType());
    assertEquals(
        Set.of(new MemberKey("zlv_ttf", "EVT-UC12A-TTF-1"), ttf2), members("100000000000162"));
    assertTrue(members("100000000000173").isEmpty());
  }

  @Test
  void uc12bMergeEciEci() {
    HistoryPlan survivor = h.planOf("6b0921aa", "100000000000193");
    HistoryPlan loser = h.planOf("6b0921aa", "100000000000163");
    MemberKey eci2 = new MemberKey("zlv_eci", "EVT-UC12B-ECI-2");
    assertEquals(List.of(eci2), survivor.membersToOpen().stream().map(MemberVersion::key).toList());
    assertEquals(List.of(eci2), loser.membersToClose());
    assertEquals("100000000000193", loser.master().activeBusinessId());
  }

  @Test
  void uc12cMergeTtfEci() {
    HistoryPlan survivor = h.planOf("6b0921ae", "100000000000184");
    HistoryPlan loser = h.planOf("6b0921ae", "100000000000164");
    MemberKey eci = new MemberKey("zlv_eci", "EVT-UC12C-ECI-1");
    assertEquals(List.of(eci), survivor.membersToOpen().stream().map(MemberVersion::key).toList());
    assertEquals(List.of(eci), loser.membersToClose());
    assertEquals("100000000000184", loser.master().activeBusinessId());
  }

  @Test
  void uc26CrossSourceUpdateOfMergedMember() {
    HistoryPlan p = h.planOf("6b0921bc", "100000000000184");
    XrefChangeRow x = xref("6b0921bc", "100000000000184");
    assertEquals("100000000000184", x.businessId());
    assertEquals("zlv_eci", x.sourceSystem());
    assertEquals("EVT-UC12C-ECI-1", x.sourcePkey());
    assertTrue(p.fieldChanges().isEmpty());
    assertNull(p.master(), "empty diff, same content: row_md5 unchanged, no new master version");
    assertTrue(p.membersToOpen().isEmpty() && p.membersToClose().isEmpty());
  }

  @Test
  void uc13EciUnmerge() {
    HistoryPlan survivor = h.planOf("6b0921c0", "100000000000193");
    HistoryPlan reborn = h.planOf("6b0921c0", "100000000000163");
    MemberKey eci2 = new MemberKey("zlv_eci", "EVT-UC12B-ECI-2");
    assertEquals(List.of(eci2), survivor.membersToClose());
    assertEquals("REMOVED", xref("6b0921c0", "100000000000193").membershipChange());
    assertEquals(List.of(eci2), reborn.membersToOpen().stream().map(MemberVersion::key).toList());
    assertEquals("MASTER_UNMERGED", reborn.master().eventType());
    assertEquals("100000000000163", reborn.master().activeBusinessId());
    assertEquals(Set.of(new MemberKey("zlv_eci", "EVT-UC12B-ECI-1")), members("100000000000193"));
    assertEquals(Set.of(eci2), members("100000000000163"));
  }

  @Test
  void uc13TtfUnmerge() {
    HistoryPlan survivor = h.planOf("6b0921c6", "100000000000184");
    HistoryPlan reborn = h.planOf("6b0921c6", "100000000000164");
    MemberKey eci = new MemberKey("zlv_eci", "EVT-UC12C-ECI-1");
    assertEquals(List.of(eci), survivor.membersToClose());
    XrefChangeRow x = xref("6b0921c6", "100000000000184");
    assertEquals("EVT-UC12C-ECI-1", x.sourcePkey());
    assertEquals("REMOVED", x.membershipChange());
    assertEquals(Set.of(new MemberKey("zlv_ttf", "EVT-UC12C-TTF-1")), members("100000000000184"));
    assertEquals(List.of(eci), reborn.membersToOpen().stream().map(MemberVersion::key).toList());
    assertEquals(Set.of(eci), members("100000000000164"));
  }

  @Test
  void olderSampleJackT180() {
    HistoryPlan p = h.planOf("6b0908bf", "100000000000180");
    assertEquals("JackT", ((GuestRoot) p.master().root()).firstName());
    XrefChangeRow x = xref("6b0908bf", "100000000000180");
    assertEquals("PATCH", x.xrefType());
    // UC26 shape: the xref says 191, history stays on the event master 180.
    assertEquals("100000000000180", x.businessId());
    assertEquals(2, members("100000000000180").size());
  }

  @Test
  void olderSampleHenry190() {
    assertEquals(
        "Henry T3",
        ((GuestRoot) h.planOf("6b09106d", "100000000000190").master().root()).firstName());
    assertEquals("CREATED", xref("6b09106d", "100000000000190").xrefChange());
  }

  @Test
  void olderSamples171BothLand() {
    HistoryPlan first = h.planOf("6b0917c8", "100000000000171");
    HistoryPlan second = h.planOf("6b0919d4", "100000000000171");
    assertEquals("y this kolavaridhi", ((GuestRoot) first.master().root()).clnsFirstName());
    GuestChild.Phone ph = (GuestChild.Phone) second.childVersions().getFirst().row();
    assertEquals("d7be8f20468b4238b13ccf42765a5a87", ph.childId());
    assertEquals("12334455001", ph.phoneNum());
  }

  @Test
  void olderSample160SourceAddressLands() {
    GuestChild.SrcAddress a =
        (GuestChild.SrcAddress) onlyChild("6b091a40", "100000000000160").row();
    assertEquals("8ca983a25f7a4d5db9728a8b28debccc", a.childId());
    assertEquals("1200 Main Rd", a.addressLine1());
    assertEquals(13, h.planOf("6b091a40", "100000000000160").fieldChanges().size());
  }
}
