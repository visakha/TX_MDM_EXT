package com.visakha.txmdmext.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class EventParserTest {

  private final List<BusinessEvent> events = Fixtures.orderedEvents();

  @Test
  void parsesAll36RealEventsWithKnownTypes() {
    assertEquals(36, events.size());
    Map<EventType, Long> byType =
        events.stream()
            .collect(Collectors.groupingBy(BusinessEvent::eventType, Collectors.counting()));
    assertEquals(9L, byType.get(EventType.MASTER_CREATED));
    assertEquals(3L, byType.get(EventType.MASTER_MERGED));
    assertEquals(2L, byType.get(EventType.MASTER_UNMERGED));
    assertNull(byType.get(EventType.OTHER));
    events.forEach(e -> assertNotEquals(ImpliedOperation.UNKNOWN, e.impliedOperation()));
  }

  @Test
  void mergedLoserPointsAtSurvivorAndHasNoDiff() {
    BusinessEvent loser = Fixtures.byId01Prefix(events, "6b09219c", "MASTER_MERGED");
    assertEquals("100000000000173", loser.businessId());
    assertEquals("100000000000162", loser.activeBusinessId());
    assertEquals(ImpliedOperation.MERGE, loser.impliedOperation());
    assertTrue(loser.dataDiff().isEmpty());
    assertTrue(loser.affectedXrefs().isEmpty());
  }

  @Test
  void unmergeSurvivorReportsRemovedMember() {
    BusinessEvent e = Fixtures.byId01Prefix(events, "6b0921c0", "MASTER_UPDATED");
    XrefInsight x = e.affectedXrefs().getFirst();
    assertEquals(ImpliedOperation.UNMERGE, e.impliedOperation());
    assertEquals(MembershipChange.REMOVED, x.membershipChange());
    assertEquals("EVT-UC12B-ECI-2", x.sourcePKey());
  }

  @Test
  void deletedDiffEntryHasNullNewValue() {
    BusinessEvent e = Fixtures.byId01Prefix(events, "6b092182", "MASTER_UPDATED");
    Map<String, FieldChange> diff =
        e.dataDiff().stream()
            .collect(Collectors.toMap(FieldChange::fieldPath, Function.identity()));
    assertEquals(ChangeType.DELETED, diff.get("last_name").changeType());
    assertNull(diff.get("last_name").newValue());
    assertEquals("UC7eciUpd", diff.get("last_name").oldValue());
  }

  @Test
  void typedDiffValuesAreKeptAsText() {
    BusinessEvent e = Fixtures.byId01Prefix(events, "6b09217f", "MASTER_UPDATED");
    Map<String, FieldChange> diff =
        e.dataDiff().stream()
            .collect(Collectors.toMap(FieldChange::fieldPath, Function.identity()));
    assertEquals("true", diff.get("do_not_contact_ind").newValue());
    assertEquals("1.5", diff.get("touch_incr").newValue());
  }

  @Test
  void fieldPathSplitsGroupChildAndField() {
    FieldPath lookup = FieldPath.parse("x_phone[7ddf5b15d8e24f468ecbc53c60c5f812].ph_typ.Code");
    assertEquals("x_phone", lookup.group());
    assertEquals("7ddf5b15d8e24f468ecbc53c60c5f812", lookup.childId());
    assertEquals("ph_typ.Code", lookup.fieldName());
    FieldPath root = FieldPath.parse("last_name");
    assertNull(root.group());
    assertNull(root.childId());
    assertEquals("last_name", root.fieldName());
  }
}
