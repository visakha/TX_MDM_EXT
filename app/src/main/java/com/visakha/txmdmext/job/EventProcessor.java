package com.visakha.txmdmext.job;

import com.visakha.txmdmext.config.AppProperties;
import com.visakha.txmdmext.event.BusinessEvent;
import com.visakha.txmdmext.event.EventBatch;
import com.visakha.txmdmext.event.EventParser;
import com.visakha.txmdmext.event.StoredEvent;
import com.visakha.txmdmext.history.HistoryPlan;
import com.visakha.txmdmext.history.HistoryPlanner;
import com.visakha.txmdmext.history.OpenChild;
import com.visakha.txmdmext.history.PriorState;
import com.visakha.txmdmext.model.ChildGroup;
import com.visakha.txmdmext.model.EntityModel;
import com.visakha.txmdmext.store.ChildStore;
import com.visakha.txmdmext.store.DbTime;
import com.visakha.txmdmext.store.EventStoreRepository;
import com.visakha.txmdmext.store.LoadedEventRepository;
import com.visakha.txmdmext.store.MasterStore;
import com.visakha.txmdmext.store.SourceHistoryRepository;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * One pass over ZLV_MDM_EVENTS_STORE: read unprocessed rows, dedupe on ID01, order, and load each
 * event in its own transaction together with its processed_dtm stamp.
 */
@Service
public class EventProcessor {

  private static final Logger LOG = LoggerFactory.getLogger(EventProcessor.class);

  private final EventStoreRepository store;
  private final EntityModel model;
  private final MasterStore masters;
  private final ChildStore children;
  private final SourceHistoryRepository sources;
  private final LoadedEventRepository loaded;
  private final HistoryPlanner planner;
  private final HistoryWriter writer;
  private final EventParser parser;
  private final TransactionTemplate tx;
  private final DbTime time;
  private final int batchSize;

  public EventProcessor(
      EventStoreRepository store,
      EntityModel model,
      MasterStore masters,
      ChildStore children,
      SourceHistoryRepository sources,
      LoadedEventRepository loaded,
      HistoryPlanner planner,
      HistoryWriter writer,
      EventParser parser,
      PlatformTransactionManager txManager,
      DbTime time,
      AppProperties props) {
    this.store = store;
    this.model = model;
    this.masters = masters;
    this.children = children;
    this.sources = sources;
    this.loaded = loaded;
    this.planner = planner;
    this.writer = writer;
    this.parser = parser;
    this.tx = new TransactionTemplate(txManager);
    this.time = time;
    this.batchSize = props.batchSize();
  }

  public BatchResult runOnce() {
    List<StoredEvent> rows = store.findUnprocessed(batchSize);
    List<StoredEvent> distinct = EventBatch.dedupe(rows);
    List<BusinessEvent> events = new ArrayList<>();
    List<String> failedIds = new ArrayList<>();
    for (StoredEvent row : distinct) {
      try {
        events.add(parser.parse(row));
      } catch (RuntimeException e) {
        failedIds.add(row.id01());
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
        failedIds.add(e.id01());
        blocked.add(e.businessId());
        LOG.error(
            "ID01 {} (business id {}): load failed, rolled back: {}",
            e.id01(),
            e.businessId(),
            ex.getMessage());
      }
    }
    return new BatchResult(rows.size(), distinct.size(), loaded, already, failedIds, skipped);
  }

  /** Returns true when history rows were written, false when the ID01 was already loaded. */
  private boolean loadOne(BusinessEvent e) {
    boolean already = loaded.alreadyLoaded(e.id01());
    if (!already) {
      HistoryPlan plan = planner.plan(e, prior(e.businessId()));
      writer.write(plan);
    }
    store.markProcessed(e.id01(), time.now());
    return !already;
  }

  private PriorState prior(String businessId) {
    Map<ChildGroup, Map<String, OpenChild>> open = new HashMap<>();
    model.childGroups().forEach(g -> open.put(g, children.openRows(g, businessId)));
    return new PriorState(
        masters.currentMd5(businessId).orElse(null), open, sources.openMembers(businessId));
  }
}
