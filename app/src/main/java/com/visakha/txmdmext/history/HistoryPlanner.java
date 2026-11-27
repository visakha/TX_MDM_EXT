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
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Pure Java: turns one event plus what history already holds into the rows to write. Change data
 * capture: a master or child version is written only when its row_md5 differs from the stored one.
 */
public final class HistoryPlanner {

  private static final int MAX_TEXT = 4000;

  private final RowHash hash;

  public HistoryPlanner(RowHash hash) {
    this.hash = hash;
  }

  public HistoryPlan plan(BusinessEvent e, PriorState prior) {
    Instant validFrom = Instant.ofEpochMilli(e.effectiveTime());
    return new HistoryPlan(
        e.id01(),
        e.businessId(),
        validFrom,
        master(e, validFrom, prior),
        childVersions(e, prior),
        membersToOpen(e, prior),
        membersToClose(e, prior),
        fieldChanges(e, validFrom),
        xrefChanges(e, validFrom));
  }

  /** No payload means the master content is unknown, so no version is written. */
  MasterVersion master(BusinessEvent e, Instant validFrom, PriorState prior) {
    if (!e.hasPayload()) {
      return null;
    }
    MasterVersion v = masterRow(e, validFrom);
    String md5 = hash.md5(HistTable.EVT_MASTER_HIST, HashValues.master(v));
    return md5.equals(prior.masterMd5()) ? null : v.withRowMd5(md5);
  }

  static MasterVersion masterRow(BusinessEvent e, Instant validFrom) {
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
        PayloadReader.meta(e.payload()),
        PayloadReader.root(e.payload()),
        deleted,
        e.payload().toString(),
        validFrom,
        null);
  }

  /**
   * Per group: a payload row whose md5 differs from the open row (or is new) gets a version; an
   * open row missing from the payload gets a deleted version. No payload: nothing changes.
   */
  List<ChildVersion> childVersions(BusinessEvent e, PriorState prior) {
    if (!e.hasPayload()) {
      return List.of();
    }
    return Arrays.stream(FieldGroup.values()).flatMap(group -> diffGroup(e, prior, group)).toList();
  }

  private Stream<ChildVersion> diffGroup(BusinessEvent e, PriorState prior, FieldGroup g) {
    HistTable table = HistTable.of(g);
    Map<String, OpenChild> open = prior.openChildren(g);
    List<ChildRow> after =
        PayloadReader.children(e.payload(), g).stream().filter(r -> r.childId() != null).toList();
    Set<String> afterIds = after.stream().map(ChildRow::childId).collect(Collectors.toSet());
    Stream<ChildVersion> changed =
        after.stream()
            .map(row -> version(table, e.businessId(), row, false))
            .filter(v -> !sameMd5(open.get(v.row().childId()), v.rowMd5()));
    Stream<ChildVersion> removed =
        open.values().stream()
            .filter(o -> !afterIds.contains(o.row().childId()))
            .map(o -> version(table, e.businessId(), o.row(), true));
    return Stream.concat(changed, removed);
  }

  private ChildVersion version(HistTable table, String bid, ChildRow row, boolean deleted) {
    return new ChildVersion(row, deleted, hash.md5(table, HashValues.child(bid, row, deleted)));
  }

  private static boolean sameMd5(OpenChild open, String md5) {
    return open != null && md5.equals(open.rowMd5());
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

  List<MemberVersion> membersToOpen(BusinessEvent e, PriorState prior) {
    return membersAfter(e).stream()
        .filter(m -> !prior.openMembers().contains(m))
        .map(m ->
            new MemberVersion(
                m, hash.md5(HistTable.EVT_MEMBER_HIST, HashValues.member(e.businessId(), m))))
        .toList();
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
