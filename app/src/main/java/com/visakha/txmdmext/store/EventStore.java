package com.visakha.txmdmext.store;

import com.visakha.txmdmext.event.StoredEvent;
import java.time.LocalDateTime;
import java.util.List;

/** The model's event store table. Read and stamp only, never deletes. Implemented per model. */
public interface EventStore {

  List<StoredEvent> findUnprocessed(int limit);

  /** Stamps processed_dtm on every unprocessed row of this ID01. Returns the rows stamped. */
  int markProcessed(String id01, LocalDateTime processedDtm);
}
