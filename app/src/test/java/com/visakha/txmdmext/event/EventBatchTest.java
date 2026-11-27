package com.visakha.txmdmext.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class EventBatchTest {

  @Test
  void dedupeKeepsOneRowPerId01() {
    List<StoredEvent> rows = Fixtures.rows();
    assertEquals(44, rows.size());
    List<StoredEvent> distinct = EventBatch.dedupe(rows);
    assertEquals(36, distinct.size());
    assertEquals(36, distinct.stream().map(StoredEvent::id01).distinct().count());
  }

  @Test
  void orderUsesDateOfChangeThenEvtDtmThenId01() {
    List<BusinessEvent> events = Fixtures.orderedEvents();
    for (int i = 1; i < events.size(); i++) {
      assertTrue(EventBatch.ORDER.compare(events.get(i - 1), events.get(i)) <= 0);
    }
    List<BusinessEvent> noDate = events.stream().filter(e -> e.dateOfChange() == null).toList();
    assertEquals(2, noDate.size());
    noDate.forEach(e -> assertEquals(e.evtDtm(), e.effectiveTime()));
  }

  @Test
  void createComesBeforeItsUpdatesAndMergeBeforeUnmerge() {
    List<BusinessEvent> events = Fixtures.orderedEvents();
    int create172 = indexOf(events, "100000000000172", EventType.MASTER_CREATED);
    int update172 = indexOf(events, "100000000000172", EventType.MASTER_UPDATED);
    assertTrue(create172 < update172);
    int merged163 = indexOf(events, "100000000000163", EventType.MASTER_MERGED);
    int reborn163 = indexOf(events, "100000000000163", EventType.MASTER_UNMERGED);
    assertTrue(merged163 < reborn163);
  }

  @Test
  void eventWithoutDateOfChangeFallsBackToEvtDtm() {
    BusinessEvent e =
        Fixtures.orderedEvents().stream()
            .filter(x -> x.dateOfChange() == null)
            .findFirst()
            .orElseThrow();
    assertNull(e.dateOfChange());
    assertEquals(e.evtDtm().longValue(), e.effectiveTime());
  }

  private static int indexOf(List<BusinessEvent> events, String businessId, EventType type) {
    for (int i = 0; i < events.size(); i++) {
      if (events.get(i).businessId().equals(businessId) && events.get(i).eventType() == type) {
        return i;
      }
    }
    throw new AssertionError("not found: " + businessId + " " + type);
  }

  @Test
  void duplicateId01WithIdenticalPayloadIsKeptOnce() {
    List<StoredEvent> rows = Fixtures.rows();
    StoredEvent dup =
        rows.stream()
            .filter(r -> rows.stream().filter(o -> o.id01().equals(r.id01())).count() > 1)
            .findFirst()
            .orElseThrow();
    List<StoredEvent> same = rows.stream().filter(r -> r.id01().equals(dup.id01())).toList();
    assertTrue(same.stream().allMatch(r -> r.payload().equals(dup.payload())));
    assertEquals(1, EventBatch.dedupe(same).size());
  }

  @Test
  void copyWithPayloadWinsOverEmptyCopy() {
    StoredEvent empty = new StoredEvent("x", 1L, null);
    StoredEvent full = new StoredEvent("x", 1L, "{}");
    assertEquals(List.of(full), EventBatch.dedupe(List.of(empty, full)));
  }
}
