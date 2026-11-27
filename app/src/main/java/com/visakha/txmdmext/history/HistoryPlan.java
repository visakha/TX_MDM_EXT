package com.visakha.txmdmext.history;

import java.time.Instant;
import java.util.List;

/**
 * Everything one event writes. master is null when its row_md5 equals the current row (no content
 * change). Built by HistoryPlanner, written in one transaction.
 */
public record HistoryPlan(
    String id01,
    String businessId,
    Instant validFrom,
    MasterVersion master,
    List<ChildVersion> childVersions,
    List<MemberVersion> membersToOpen,
    List<MemberKey> membersToClose,
    List<FieldChangeRow> fieldChanges,
    List<XrefChangeRow> xrefChanges) {

  public boolean writesNothing() {
    return master == null
        && childVersions.isEmpty()
        && membersToOpen.isEmpty()
        && membersToClose.isEmpty()
        && fieldChanges.isEmpty()
        && xrefChanges.isEmpty();
  }
}
