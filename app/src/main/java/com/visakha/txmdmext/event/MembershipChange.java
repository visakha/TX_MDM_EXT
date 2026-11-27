package com.visakha.txmdmext.event;

/** membershipChange from the schema. */
public enum MembershipChange {
  ADDED,
  REMOVED,
  NONE;

  public static MembershipChange from(String value) {
    return value == null ? null : valueOf(value);
  }
}
