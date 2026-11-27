package com.visakha.txmdmext.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.visakha.txmdmext.event.BusinessEvent;
import com.visakha.txmdmext.event.Fixtures;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HistoryPlannerTest {

  private static final MemberKey TTF_12A_1 = new MemberKey("zlv_ttf", "EVT-UC12A-TTF-1");
  private static final MemberKey TTF_12A_2 = new MemberKey("zlv_ttf", "EVT-UC12A-TTF-2");
  private static final MemberKey ECI_12B_1 = new MemberKey("zlv_eci", "EVT-UC12B-ECI-1");
  private static final MemberKey ECI_12B_2 = new MemberKey("zlv_eci", "EVT-UC12B-ECI-2");

  private List<BusinessEvent> events;
  private InMemoryHistory history;

  @BeforeEach
  void replayAllRealEvents() {
    events = Fixtures.orderedEvents();
    history = new InMemoryHistory();
    history.replay(events);
  }

  @Test
  void everyEventGivesOneMasterVersion() {
    assertEquals(36, history.plans.size());
    long versions = history.masterVersions.values().stream().mapToLong(List::size).sum();
    assertEquals(36, versions);
  }

  @Test
  void createWithPhoneOpensMasterMemberAndPhone() {
    BusinessEvent create = Fixtures.byId01Prefix(events, "6b092107", "MASTER_CREATED");
    HistoryPlan plan = HistoryPlanner.plan(create, PriorState.NONE);
    assertEquals("100000000000172", plan.businessId());
    assertEquals(List.of(new MemberKey("zlv_ttf", "EVT-UC7-TTF-1")), plan.membersToOpen());
    assertEquals(1, plan.childVersions().size());
    ChildRow.Phone phone = (ChildRow.Phone) plan.childVersions().getFirst().row();
    assertEquals(FieldGroup.X_PHONE, phone.group());
    assertTrue(phone.phTyp() != null && phone.phTyp().code() != null);
    assertEquals(9, plan.fieldChanges().size());
    assertEquals(1, plan.fieldChanges().getFirst().seqNo());
    assertEquals("EvtTest", plan.master().root().firstName());
  }

  @Test
  void removedPhoneGetsDeletedVersion() {
    List<InMemoryHistory.ChildEvent> phone =
        history.childEventsOf("100000000000172", "2dca242eedb34fa98eacbaa17dd87212");
    assertEquals(2, phone.size());
    assertFalse(phone.get(0).version().deleted());
    assertTrue(phone.get(1).version().deleted());
    assertTrue(phone.get(1).id01().startsWith("6b092171"));
    assertEquals(1, history.openChildren("100000000000172", FieldGroup.X_PHONE));
  }

  @Test
  void removedSourceAddressGetsDeletedVersion() {
    List<InMemoryHistory.ChildEvent> addr =
        history.childEventsOf("100000000000192", "d96a50d6c077405b893085920789e242");
    assertEquals(2, addr.size());
    assertTrue(addr.get(1).version().deleted());
    assertEquals(0, history.openChildren("100000000000192", FieldGroup.X_SRC_ADDRESS));
  }

  @Test
  void lookupChangeMakesNewPhoneVersionWithCodeAndName() {
    List<InMemoryHistory.ChildEvent> phone =
        history.childEventsOf("100000000000192", "7ddf5b15d8e24f468ecbc53c60c5f812");
    ChildRow.Phone last = (ChildRow.Phone) phone.getLast().version().row();
    assertEquals(new Lookup("Fax", "Fax"), last.phTyp());
  }

  @Test
  void altIdRowIsTyped() {
    long altIds = history.openChildren("100000000000172", FieldGroup.X_ALT_ID);
    assertEquals(1, altIds);
    ChildRow.AltId alt =
        (ChildRow.AltId)
            history
                .childEventsOf("100000000000172", "d1fa3e1963554a8b85bb451fec0c4fa3")
                .getFirst()
                .version()
                .row();
    assertEquals(Boolean.TRUE, alt.activeInd());
  }

  @Test
  void clearedRootFieldIsFieldChangeWithNullNewValue() {
    FieldChangeRow cleared =
        history.fieldChanges.stream()
            .filter(c -> c.id01().startsWith("6b092182") && c.fieldPath().equals("last_name"))
            .findFirst()
            .orElseThrow();
    assertEquals("DELETED", cleared.changeType());
    assertNull(cleared.newValue());
    BusinessEvent e = Fixtures.byId01Prefix(events, "6b092182", "MASTER_UPDATED");
    assertNull(PayloadReader.root(e.payload()).lastName());
  }

  @Test
  void mergeMovesMemberToSurvivorAndClosesLoser() {
    assertEquals(Set.of(TTF_12A_1, TTF_12A_2), history.openMembers.get("100000000000162"));
    assertTrue(history.openMembers.get("100000000000173").isEmpty());
    MasterVersion loserLast = history.masterVersions.get("100000000000173").getLast();
    assertEquals("MASTER_MERGED", loserLast.eventType());
    assertEquals("100000000000162", loserLast.activeBusinessId());
  }

  @Test
  void unmergeGivesMemberBackToRebornMaster() {
    assertEquals(Set.of(ECI_12B_1), history.openMembers.get("100000000000193"));
    assertEquals(Set.of(ECI_12B_2), history.openMembers.get("100000000000163"));
    MasterVersion reborn = history.masterVersions.get("100000000000163").getLast();
    assertEquals("MASTER_UNMERGED", reborn.eventType());
    assertEquals(
        Set.of(new MemberKey("zlv_ttf", "EVT-UC12C-TTF-1")),
        history.openMembers.get("100000000000184"));
    assertEquals(
        Set.of(new MemberKey("zlv_eci", "EVT-UC12C-ECI-1")),
        history.openMembers.get("100000000000164"));
  }

  @Test
  void xrefChangeIsKeyedBySourceAndStampedWithTheEventMaster() {
    // UC26: the xref's own businessId (191) differs from the master (180).
    XrefChangeRow x =
        history.xrefChanges.stream()
            .filter(r -> r.id01().startsWith("6b0908bf"))
            .findFirst()
            .orElseThrow();
    assertEquals("100000000000180", x.businessId());
    assertEquals("c360.default.system", x.sourceSystem());
    assertEquals("ex6b08748a90cd870a2269f441", x.sourcePkey());
  }

  @Test
  void noTwoXrefRowsShareTheUniqueKey() {
    long distinct =
        history.xrefChanges.stream()
            .map(x -> x.id01() + "|" + x.sourceSystem() + "|" + x.sourcePkey())
            .distinct()
            .count();
    assertEquals(history.xrefChanges.size(), distinct);
  }
}
