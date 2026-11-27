package com.visakha.txmdmext.history;

import com.visakha.txmdmext.config.TestConfig;
import com.visakha.txmdmext.event.BusinessEvent;
import com.visakha.txmdmext.model.ChildGroup;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Applies HistoryPlans the way HistoryWriter does, but in memory, so the history logic can be
 * checked against the real events without a database. Uses the formulas of the example file.
 */
final class InMemoryHistory {

  record ChildEvent(String businessId, String id01, ChildVersion version) {}

  final HistoryPlanner planner = new HistoryPlanner(TestConfig.exampleRowHash(), TestConfig.MODEL);
  final Map<String, String> masterMd5 = new HashMap<>();
  final Map<String, List<MasterVersion>> masterVersions = new HashMap<>();
  final Map<String, Map<ChildGroup, Map<String, OpenChild>>> openChildren = new HashMap<>();
  final Map<String, Set<MemberKey>> openMembers = new HashMap<>();
  final List<ChildEvent> childEvents = new ArrayList<>();
  final List<FieldChangeRow> fieldChanges = new ArrayList<>();
  final List<XrefChangeRow> xrefChanges = new ArrayList<>();
  final Map<String, HistoryPlan> plans = new LinkedHashMap<>();

  void replay(List<BusinessEvent> ordered) {
    ordered.stream().filter(HistoryPlanner::isUsable).forEach(this::apply);
  }

  PriorState prior(String bid) {
    return new PriorState(
        masterMd5.get(bid),
        openChildren.getOrDefault(bid, Map.of()),
        Set.copyOf(openMembers.getOrDefault(bid, Set.of())));
  }

  HistoryPlan apply(BusinessEvent e) {
    String bid = e.businessId();
    HistoryPlan plan = planner.plan(e, prior(bid));
    plans.put(e.id01(), plan);
    if (plan.master() != null) {
      masterVersions.computeIfAbsent(bid, k -> new ArrayList<>()).add(plan.master());
      masterMd5.put(bid, plan.master().rowMd5());
    }
    Map<ChildGroup, Map<String, OpenChild>> open =
        openChildren.computeIfAbsent(bid, k -> new HashMap<>());
    for (ChildVersion v : plan.childVersions()) {
      childEvents.add(new ChildEvent(bid, e.id01(), v));
      Map<String, OpenChild> group =
          open.computeIfAbsent(v.row().group(), k -> new LinkedHashMap<>());
      if (v.deleted()) {
        group.remove(v.row().childId());
      } else {
        group.put(v.row().childId(), new OpenChild(v.row(), v.rowMd5()));
      }
    }
    Set<MemberKey> members = openMembers.computeIfAbsent(bid, k -> new LinkedHashSet<>());
    members.removeAll(plan.membersToClose());
    plan.membersToOpen().forEach(m -> members.add(m.key()));
    fieldChanges.addAll(plan.fieldChanges());
    xrefChanges.addAll(plan.xrefChanges());
    return plan;
  }

  HistoryPlan planOf(String id01Prefix, String businessId) {
    return plans.entrySet().stream()
        .filter(
            x -> x.getKey().startsWith(id01Prefix) && x.getValue().businessId().equals(businessId))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElseThrow();
  }

  List<ChildEvent> childEventsOf(String businessId, String childId) {
    return childEvents.stream()
        .filter(
            c -> c.businessId().equals(businessId) && c.version().row().childId().equals(childId))
        .toList();
  }

  long openChildren(String businessId, ChildGroup group) {
    return openChildren.getOrDefault(businessId, Map.of()).getOrDefault(group, Map.of()).size();
  }

  MasterVersion currentMaster(String businessId) {
    return masterVersions.get(businessId).getLast();
  }
}
