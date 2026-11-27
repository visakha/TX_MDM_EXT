package com.visakha.txmdmext.history;

import java.util.Map;
import java.util.Set;

/**
 * What history already holds for one master: row_md5 of its current EVT_MASTER_HIST row (null if
 * none), its open child rows per group (by child _id) and its open memberships.
 */
public record PriorState(
    String masterMd5,
    Map<FieldGroup, Map<String, OpenChild>> openChildren,
    Set<MemberKey> openMembers) {

  public static final PriorState NONE = new PriorState(null, Map.of(), Set.of());

  public Map<String, OpenChild> openChildren(FieldGroup group) {
    return openChildren.getOrDefault(group, Map.of());
  }
}
