package com.visakha.txmdmext.job;

import com.visakha.txmdmext.config.AppProperties;
import com.visakha.txmdmext.event.BusinessEvent;
import com.visakha.txmdmext.event.EventBatch;
import com.visakha.txmdmext.event.EventParser;
import com.visakha.txmdmext.event.StoredEvent;
import com.visakha.txmdmext.history.HistoryPlan;
import com.visakha.txmdmext.history.HistoryPlanner;
import com.visakha.txmdmext.history.PriorState;
import com.visakha.txmdmext.store.DbTime;
import com.visakha.txmdmext.store.EventStoreRepository;
import com.visakha.txmdmext.store.MasterHistoryRepository;
import com.visakha.txmdmext.store.SourceHistoryRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * One pass over ZLV_MDM_EVENTS_STORE: read unprocessed rows, dedupe on ID01, order, and load each
 * event in its own transaction together with its processed_dtm stamp.
 */
@Service
public class EventProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(EventProcessor.class);

  private final EventStoreRepository store;
  private final MasterHistoryRepository masters;
  private final SourceHistoryRepository sources;
  private final HistoryWriter writer;
  private final EventParser parser;
  private final TransactionTemplate tx;
  private final DbTime time;
  private final int batchSize;

  public EventProcessor(
      EventStoreRepository store,
      MasterHistoryRepository masters,
      SourceHistoryRepository sources,
      HistoryWriter writer,
      EventParser parser,
      TransactionTemplate tx,
      DbTime time,
      AppProperties props) {
    this.store = store;
    this.masters = masters;
    this.sources = sources;
    this.writer = writer;
    this.parser = parser;
    this.tx = tx;
    this.time = time;
    this.batchSize = props.batchSize();
  }

  public BatchResult runOnce() {
    List<StoredEvent> rows = store.findUnprocessed(batchSize);
    List<StoredEvent> distinct = EventBatch.dedupe(rows);
    List<BusinessEvent> events = new ArrayList<>();
    int failed = 0;
    for (StoredEvent row : distinct) {
      try {
        events.add(parser.parse(row));
      } catch (RuntimeException e) {
        failed++;
        LOG.error(
            "ID01 {}: payload could not be parsed, left unprocessed: {}",
            row.id01(),
            e.getMessage());
      }
    }
    int loaded = 0;
    int already = 0;
    int skipped = 0;
    // After a failure, later events of the same master wait for the next pass to keep order.
    Set<String> blocked = new HashSet<>();
    for (BusinessEvent e : EventBatch.ordered(events)) {
      if (!HistoryPlanner.isUsable(e) || blocked.contains(e.businessId())) {
        skipped++;
        continue;
      }
      try {
        boolean wasNew = Boolean.TRUE.equals(tx.execute(status -> loadOne(e)));
        if (wasNew) {
          loaded++;
        } else {
          already++;
        }
      } catch (RuntimeException ex) {
        failed++;
        blocked.add(e.businessId());
        LOG.error(
            "ID01 {} (business id {}): load failed, rolled back: {}",
            e.id01(),
            e.businessId(),
            ex.getMessage());
      }
    }
    return new BatchResult(rows.size(), distinct.size(), loaded, already, failed, skipped);
  }

  /** Returns true when history rows were written, false when the ID01 was already loaded. */
  private boolean loadOne(BusinessEvent e) {
    boolean already = masters.eventAlreadyLoaded(e.id01());
    if (!already) {
      HistoryPlan plan = HistoryPlanner.plan(e, prior(e.businessId()));
      writer.write(plan);
    }
    store.markProcessed(e.id01(), time.now());
    return !already;
  }

  private PriorState prior(String businessId) {
    return new PriorState(
        masters.currentPayload(businessId).map(parser::readTree).orElse(null),
        sources.openMembers(businessId));
  }
}
