package com.visakha.txmdmext.job;

import com.visakha.txmdmext.history.ChildVersion;
import com.visakha.txmdmext.history.HistoryPlan;
import com.visakha.txmdmext.history.MemberKey;
import com.visakha.txmdmext.history.MemberVersion;
import com.visakha.txmdmext.store.ChildHistoryRepository;
import com.visakha.txmdmext.store.DbTime;
import com.visakha.txmdmext.store.FieldChangeRepository;
import com.visakha.txmdmext.store.MasterHistoryRepository;
import com.visakha.txmdmext.store.SourceHistoryRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

/** Writes one HistoryPlan. Close first, then append. Called inside the event's transaction. */
@Component
public class HistoryWriter {

  private final MasterHistoryRepository masters;
  private final ChildHistoryRepository children;
  private final SourceHistoryRepository sources;
  private final FieldChangeRepository fieldChanges;
  private final DbTime time;

  public HistoryWriter(
      MasterHistoryRepository masters,
      ChildHistoryRepository children,
      SourceHistoryRepository sources,
      FieldChangeRepository fieldChanges,
      DbTime time) {
    this.masters = masters;
    this.children = children;
    this.sources = sources;
    this.fieldChanges = fieldChanges;
    this.time = time;
  }

  public void write(HistoryPlan plan) {
    LocalDateTime at = time.of(plan.validFrom());
    String bid = plan.businessId();
    if (plan.master() != null) {
      masters.closeCurrent(bid, at);
      masters.insert(plan.master());
    }
    for (ChildVersion v : plan.childVersions()) {
      children.closeCurrent(v.row().group(), bid, v.row().childId(), at);
      children.insert(bid, plan.id01(), v, at);
    }
    for (MemberKey m : plan.membersToClose()) {
      sources.closeMember(bid, m, plan.id01(), at);
    }
    for (MemberVersion m : plan.membersToOpen()) {
      sources.openMember(bid, m, plan.id01(), at);
    }
    plan.fieldChanges().forEach(fieldChanges::insert);
    plan.xrefChanges().forEach(sources::insertXrefChange);
  }
}
