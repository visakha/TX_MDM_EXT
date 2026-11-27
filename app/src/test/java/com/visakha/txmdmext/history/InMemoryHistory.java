package com.visakha.txmdmext.history;

import com.visakha.txmdmext.event.BusinessEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Applies HistoryPlans the way HistoryWriter does, but in memory, so the history logic can be
 * checked against the real events without Oracle.
 */
final class InMemoryHistory {

  record ChildEvent(String businessId, String id01, ChildVersion version) {}

  final Map<String, JsonNode> currentPayload = new HashMap<>();
  final Map<String, List<MasterVersion>> masterVersions = new HashMap<>();
  final Map<String, Set<MemberKey>> openMembers = new HashMap<>();
  final List<ChildEvent> childEvents = new ArrayList<>();
  final List<FieldChangeRow> fieldChanges = new ArrayList<>();
  final List<XrefChangeRow> xrefChanges = new ArrayList<>();
  final List<HistoryPlan> plans = new ArrayList<>();

  void replay(List<BusinessEvent> ordered) {
    ordered.stream().filter(HistoryPlanner::isUsable).forEach(this::apply);
  }

  void apply(BusinessEvent e) {
    String bid = e.businessId();
    PriorState prior =
        new PriorState(
            currentPayload.get(bid), Set.copyOf(openMembers.getOrDefault(bid, Set.of())));
    HistoryPlan plan = HistoryPlanner.plan(e, prior);
    plans.add(plan);
    masterVersions.computeIfAbsent(bid, k -> new ArrayList<>()).add(plan.master());
    if (e.hasPayload()) {
      currentPayload.put(bid, e.payload());
    }
    plan.childVersions().forEach(v -> childEvents.add(new ChildEvent(bid, e.id01(), v)));
    Set<MemberKey> members = openMembers.computeIfAbsent(bid, k -> new LinkedHashSet<>());
    members.removeAll(plan.membersToClose());
    members.addAll(plan.membersToOpen());
    fieldChanges.addAll(plan.fieldChanges());
    xrefChanges.addAll(plan.xrefChanges());
  }

  List<ChildEvent> childEventsOf(String businessId, String childId) {
    return childEvents.stream()
        .filter(
            c -> c.businessId().equals(businessId) && c.version().row().childId().equals(childId))
        .toList();
  }

  /** Open child rows = last version per child id that is not a delete. */
  long openChildren(String businessId, FieldGroup group) {
    Map<String, ChildVersion> last = new HashMap<>();
    childEvents.stream()
        .filter(c -> c.businessId().equals(businessId) && c.version().row().group() == group)
        .forEach(c -> last.put(c.version().row().childId(), c.version()));
    return last.values().stream().filter(v -> !v.deleted()).count();
  }
}
