package com.visakha.txmdmext.event;

/** One raw row of ZLV_MDM_EVENTS_STORE. evtDtm is epoch ms (when IDMC wrote the event). */
public record StoredEvent(String id01, Long evtDtm, String payload) {

  public boolean hasPayload() {
    return payload != null && !payload.isBlank();
  }
}
