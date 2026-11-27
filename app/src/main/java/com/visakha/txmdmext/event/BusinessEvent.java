package com.visakha.txmdmext.event;

import java.util.List;
import tools.jackson.databind.JsonNode;

/**
 * One parsed zlv_guest business event. payload is the master record AFTER the change; it is sparse
 * (null fields are left out) and may be null.
 */
public record BusinessEvent(
    String id01,
    Long evtDtm,
    String eventTypeName,
    EventType eventType,
    ImpliedOperation impliedOperation,
    String businessId,
    String activeBusinessId,
    String assetId,
    Long dateOfChange,
    String correlationId,
    String userName,
    String sourceSystem,
    States states,
    List<String> changedFields,
    List<String> mergedBusinessIds,
    List<Member> members,
    List<XrefInsight> affectedXrefs,
    List<FieldChange> dataDiff,
    JsonNode payload) {

  /** When the change happened: dateOfChange, or EVT_DTM when IDMC left dateOfChange null. */
  public long effectiveTime() {
    if (dateOfChange != null) {
      return dateOfChange;
    }
    return evtDtm == null ? 0L : evtDtm;
  }

  public boolean hasPayload() {
    return payload != null && payload.isObject();
  }
}
