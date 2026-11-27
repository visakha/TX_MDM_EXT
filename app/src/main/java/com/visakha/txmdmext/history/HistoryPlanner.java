package com.visakha.txmdmext.history;

import com.visakha.txmdmext.event.BusinessEvent;
import com.visakha.txmdmext.event.EventType;
import com.visakha.txmdmext.event.FieldChange;
import com.visakha.txmdmext.event.FieldPath;
import com.visakha.txmdmext.event.XrefInsight;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/** Pure Java: turns one event plus what history already holds into the rows to write. */
public final class HistoryPlanner {

  private static final int MAX_TEXT = 4000;

  private HistoryPlanner() {}

  public static HistoryPlan plan(BusinessEvent e, PriorState prior) {
    Instant validFrom = Instant.ofEpochMilli(e.effectiveTime());
    return new HistoryPlan(
        e.id01(),
        e.businessId(),
        validFrom,
        master(e, validFrom),
        childVersions(e, prior),
        membersToOpen(e, prior),
        membersToClose(e, prior),
        fieldChanges(e, validFrom),
        xrefChanges(e, validFrom));
  }

  static MasterVersion master(BusinessEvent e, Instant validFrom) {
    boolean deleted =
        switch (e.impliedOperation()) {
          case DELETE, PURGE -> true;
          default -> false;
        };
    return new MasterVersion(
        e.businessId(),
        e.activeBusinessId(),
        e.id01(),
        e.eventTypeName(),
        e.impliedOperation().name(),
        e.correlationId(),
        e.dateOfChange(),
        e.userName(),
        e.sourceSystem(),
        e.mergedBusinessIds().isEmpty() ? null : String.join(",", e.mergedBusinessIds()),
        e.states().base(),
        e.states().validation(),
        e.states().createMode(),
        e.hasPayload()
            ? PayloadReader.meta(e.payload())
            : new MetaInfo(null, null, null, null, null),
        e.hasPayload()
            ? PayloadReader.root(e.payload())
            : new GuestRoot(
                null, null, null, null, null, null, null, null, null, null, null, null, null),
        deleted,
        e.hasPayload() ? e.payload().toString() : null,
        validFrom);
  }

  /**
   * Snapshot diff per group: new or changed rows get a new version, rows that left the payload get
   * a deleted version. No payload means we cannot tell, so nothing changes.
   */
  static List<ChildVersion> childVersions(BusinessEvent e, PriorState prior) {
    if (!e.hasPayload()) {
      return List.of();
    }
    return Arrays.stream(FieldGroup.values()).flatMap(group -> diffGroup(e, prior, group)).toList();
  }

  private static Stream<ChildVersion> diffGroup(BusinessEvent e, PriorState prior, FieldGroup g) {
    Map<String, ChildRow> before =
        prior.currentPayload() == null
            ? Map.of()
            : byId(PayloadReader.children(prior.currentPayload(), g));
    Map<String, ChildRow> after = byId(PayloadReader.children(e.payload(), g));
    Stream<ChildVersion> changed =
        after.values().stream()
            .filter(row -> !row.equals(before.get(row.childId())))
            .map(row -> new ChildVersion(row, false));
    Stream<ChildVersion> removed =
        before.values().stream()
            .filter(row -> !after.containsKey(row.childId()))
            .map(row -> new ChildVersion(row, true));
    return Stream.concat(changed, removed);
  }

  private static Map<String, ChildRow> byId(List<ChildRow> rows) {
    return rows.stream()
        .filter(row -> row.childId() != null)
        .collect(
            Collectors.toMap(
                ChildRow::childId, Function.identity(), (a, b) -> a, java.util.LinkedHashMap::new));
  }

  /** members[] is the full membership after the event. A merged-away master keeps none. */
  static Set<MemberKey> membersAfter(BusinessEvent e) {
    if (e.eventType() == EventType.MASTER_MERGED) {
      return Set.of();
    }
    return e.members().stream()
        .map(m -> new MemberKey(m.sourceSystemId(), m.sourcePKey()))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  static List<MemberKey> membersToOpen(BusinessEvent e, PriorState prior) {
    return membersAfter(e).stream().filter(m -> !prior.openMembers().contains(m)).toList();
  }

  static List<MemberKey> membersToClose(BusinessEvent e, PriorState prior) {
    Set<MemberKey> after = membersAfter(e);
    return prior.openMembers().stream().filter(m -> !after.contains(m)).toList();
  }

  static List<FieldChangeRow> fieldChanges(BusinessEvent e, Instant changeTime) {
    List<FieldChange> diff = e.dataDiff();
    return IntStream.range(0, diff.size())
        .mapToObj(i -> fieldChange(e, diff.get(i), i + 1, changeTime))
        .toList();
  }

  private static FieldChangeRow fieldChange(
      BusinessEvent e, FieldChange c, int seqNo, Instant changeTime) {
    FieldPath path = FieldPath.parse(c.fieldPath());
    return new FieldChangeRow(
        e.id01(),
        seqNo,
        e.businessId(),
        c.fieldPath(),
        path.group(),
        path.childId(),
        path.fieldName(),
        c.changeType() == null ? null : c.changeType().name(),
        clip(c.oldValue()),
        clip(c.newValue()),
        e.dateOfChange(),
        changeTime);
  }

  static List<XrefChangeRow> xrefChanges(BusinessEvent e, Instant changeTime) {
    return e.affectedXrefs().stream()
        .filter(x -> x.sourceSystemId() != null && x.sourcePKey() != null)
        .map(x -> xrefChange(e, x, changeTime))
        .toList();
  }

  private static XrefChangeRow xrefChange(BusinessEvent e, XrefInsight x, Instant changeTime) {
    return new XrefChangeRow(
        e.id01(),
        e.businessId(),
        x.sourceSystemId(),
        x.sourcePKey(),
        name(x.xrefType()),
        name(x.xrefChange()),
        name(x.membershipChange()),
        e.impliedOperation().name(),
        e.dateOfChange(),
        changeTime);
  }

  private static String name(Enum<?> value) {
    return value == null ? null : value.name();
  }

  private static String clip(String value) {
    return value == null || value.length() <= MAX_TEXT ? value : value.substring(0, MAX_TEXT);
  }

  /** An event without a businessId cannot be placed in history. */
  public static boolean isUsable(BusinessEvent e) {
    return e.businessId() != null;
  }
}
