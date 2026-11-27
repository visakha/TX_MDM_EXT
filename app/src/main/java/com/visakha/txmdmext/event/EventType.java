package com.visakha.txmdmext.event;

/** Event types seen in real events. The schema does not list them, so anything else is OTHER. */
public enum EventType {
  MASTER_CREATED,
  MASTER_UPDATED,
  MASTER_MERGED,
  MASTER_UNMERGED,
  OTHER;

  public static EventType from(String value) {
    return switch (value) {
      case "MASTER_CREATED" -> MASTER_CREATED;
      case "MASTER_UPDATED" -> MASTER_UPDATED;
      case "MASTER_MERGED" -> MASTER_MERGED;
      case "MASTER_UNMERGED" -> MASTER_UNMERGED;
      case null, default -> OTHER;
    };
  }
}
