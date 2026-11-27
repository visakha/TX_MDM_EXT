package com.visakha.txmdmext.history;

import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * What the history already holds for one master: the payload of its current EVT_MASTER_HIST row
 * (null if none yet) and its open memberships.
 */
public record PriorState(JsonNode currentPayload, Set<MemberKey> openMembers) {

  public static final PriorState NONE = new PriorState(null, Set.of());
}
