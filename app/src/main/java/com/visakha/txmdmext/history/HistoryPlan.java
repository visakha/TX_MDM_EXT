package com.visakha.txmdmext.history;

import java.time.Instant;
import java.util.List;

/** Everything one event writes. Built by HistoryPlanner, written in one transaction. */
public record HistoryPlan(
    String id01,
    String businessId,
    Instant validFrom,
    MasterVersion master,
    List<ChildVersion> childVersions,
    List<MemberKey> membersToOpen,
    List<MemberKey> membersToClose,
    List<FieldChangeRow> fieldChanges,
    List<XrefChangeRow> xrefChanges) {}
