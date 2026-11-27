package com.visakha.txmdmext.model.zlvguest.store;

import com.visakha.txmdmext.event.StoredEvent;
import com.visakha.txmdmext.model.zlvguest.repository.ZlvMdmEventRepository;
import com.visakha.txmdmext.store.EventStore;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Component;

/** ZLV_MDM_EVENTS_STORE through ZlvMdmEventRepository. Reads and stamps, never deletes. */
@Component
public class GuestEventStore implements EventStore {

  private final ZlvMdmEventRepository events;

  public GuestEventStore(ZlvMdmEventRepository events) {
    this.events = events;
  }

  @Override
  public List<StoredEvent> findUnprocessed(int limit) {
    return events.findByProcessedDtmIsNullOrderByEvtDtmAscId01Asc(Limit.of(limit)).stream()
        .map(row -> new StoredEvent(row.id01(), row.evtDtm(), row.payload()))
        .toList();
  }

  @Override
  public int markProcessed(String id01, LocalDateTime processedDtm) {
    return events.markProcessed(id01, processedDtm);
  }
}
