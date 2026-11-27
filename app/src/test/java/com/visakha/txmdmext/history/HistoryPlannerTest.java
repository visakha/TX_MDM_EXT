package com.visakha.txmdmext.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.visakha.txmdmext.config.TestConfig;
import com.visakha.txmdmext.event.BusinessEvent;
import com.visakha.txmdmext.event.Fixtures;
import com.visakha.txmdmext.model.zlvguest.GuestChild;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** General history rules, replaying all 36 real events in memory. */
class HistoryPlannerTest {

  private List<BusinessEvent> events;
  private InMemoryHistory history;

  @BeforeEach
  void replayAllRealEvents() {
    events = Fixtures.orderedEvents();
    history = new InMemoryHistory();
    history.replay(events);
  }

  @Test
  void everyEventIsPlanned() {
    assertEquals(36, history.plans.size());
  }

  @Test
  void createStartsFromNothing() {
    BusinessEvent create = Fixtures.byId01Prefix(events, "6b092107", "MASTER_CREATED");
    HistoryPlan plan =
        new HistoryPlanner(TestConfig.exampleRowHash(), TestConfig.MODEL)
            .plan(create, PriorState.NONE);
    assertNotNull(plan.master());
    assertEquals(32, plan.master().rowMd5().length());
    assertEquals(1, plan.membersToOpen().size());
    assertTrue(plan.membersToClose().isEmpty());
    assertEquals(1, plan.childVersions().size());
  }

  @Test
  void unchangedContentGivesNoNewVersion() {
    BusinessEvent create = Fixtures.byId01Prefix(events, "6b092107", "MASTER_CREATED");
    InMemoryHistory h = new InMemoryHistory();
    h.apply(create);
    HistoryPlan again = h.planner.plan(create, h.prior(create.businessId()));
    assertNull(again.master());
    assertTrue(again.childVersions().isEmpty());
    assertTrue(again.membersToOpen().isEmpty());
    assertTrue(again.membersToClose().isEmpty());
  }

  @Test
  void eachMasterHasOneCurrentMd5AndVersionsDiffer() {
    history.masterVersions.forEach((bid, versions) -> {
      for (int i = 1; i < versions.size(); i++) {
        assertFalse(
            versions.get(i).rowMd5().equals(versions.get(i - 1).rowMd5()),
            "two equal consecutive versions for " + bid);
      }
      assertEquals(versions.getLast().rowMd5(), history.masterMd5.get(bid));
    });
  }

  @Test
  void fieldChangeRowsMatchDataDiffTotals() {
    int diffTotal = events.stream().mapToInt(e -> e.dataDiff().size()).sum();
    assertEquals(diffTotal, history.fieldChanges.size());
  }

  @Test
  void everyTouchNotesChangeIsItsOwnFieldChangeRow() {
    long eventsWithNote =
        events.stream()
            .filter(e -> e.dataDiff().stream().anyMatch(d -> d.fieldPath().equals("touch_notes")))
            .count();
    long noteRows =
        history.fieldChanges.stream().filter(c -> "touch_notes".equals(c.fieldName())).count();
    assertTrue(eventsWithNote > 0);
    assertEquals(eventsWithNote, noteRows);
  }

  @Test
  void fieldChangeSeqNoRunsOneToNPerEvent() {
    history
        .plans
        .values()
        .forEach(p -> {
          for (int i = 0; i < p.fieldChanges().size(); i++) {
            assertEquals(i + 1, p.fieldChanges().get(i).seqNo());
          }
        });
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

  @Test
  void removedChildGetsDeletedVersionWithItsLastValues() {
    InMemoryHistory.ChildEvent last =
        history.childEventsOf("100000000000172", "2dca242eedb34fa98eacbaa17dd87212").getLast();
    assertTrue(last.version().deleted());
    assertEquals(5125550711L, ((GuestChild.Phone) last.version().row()).phNum());
  }
}
