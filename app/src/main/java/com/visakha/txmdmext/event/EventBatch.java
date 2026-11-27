package com.visakha.txmdmext.event;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/** Dedupe and ordering rules agreed with Vamsi (NOTES.md, VERIFICATION.md). */
public final class EventBatch {

  /** Order by dateOfChange, EVT_DTM when it is null, then ID01. */
  public static final Comparator<BusinessEvent> ORDER =
      Comparator.comparingLong(BusinessEvent::effectiveTime).thenComparing(BusinessEvent::id01);

  private EventBatch() {}

  /**
   * The same event can land more than once when publish windows overlap: keep one row per ID01. A
   * copy with a payload wins over an empty one.
   */
  public static List<StoredEvent> dedupe(List<StoredEvent> rows) {
    return rows.stream()
        .collect(
            Collectors.toMap(
                StoredEvent::id01,
                row -> row,
                (kept, other) -> kept.hasPayload() ? kept : other,
                LinkedHashMap::new))
        .values()
        .stream()
        .toList();
  }

  public static List<BusinessEvent> ordered(List<BusinessEvent> events) {
    return events.stream().sorted(ORDER).toList();
  }
}
