package com.visakha.txmdmext.event;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * The 44 real rows (36 distinct ID01) from extract_20261127_005948.zip. The file is
 * ID01|EVT_DTM|EVT_DTM_TMZ|PAYLOAD|PROCESSED_DTM; the payload may contain '|', so split by index.
 */
public final class Fixtures {

  private Fixtures() {}

  public static List<StoredEvent> rows() {
    try (InputStream in =
        Objects.requireNonNull(
            Fixtures.class.getResourceAsStream("/events/ZLV_MDM_EVENTS_STORE.dsv"))) {
      String text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      return text.lines()
          .filter(line -> !line.isBlank())
          .filter(line -> !line.startsWith("ID01|"))
          .map(Fixtures::row)
          .toList();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static List<BusinessEvent> orderedEvents() {
    return EventBatch.ordered(new EventParser().parseAll(EventBatch.dedupe(rows())));
  }

  public static BusinessEvent byId01Prefix(List<BusinessEvent> events, String prefix, String type) {
    return events.stream()
        .filter(e -> e.id01().startsWith(prefix) && e.eventTypeName().equals(type))
        .findFirst()
        .orElseThrow();
  }

  private static StoredEvent row(String line) {
    int p1 = line.indexOf('|');
    int p2 = line.indexOf('|', p1 + 1);
    int p3 = line.indexOf('|', p2 + 1);
    int p4 = line.lastIndexOf('|');
    String evtDtm = line.substring(p1 + 1, p2);
    String payload = line.substring(p3 + 1, p4);
    return new StoredEvent(
        line.substring(0, p1),
        evtDtm.isEmpty() ? null : Long.valueOf(evtDtm),
        payload.isEmpty() ? null : payload);
  }
}
