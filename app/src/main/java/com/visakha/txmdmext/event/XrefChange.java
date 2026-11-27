package com.visakha.txmdmext.event;

/** xrefChange from the schema. */
public enum XrefChange {
  CREATED,
  UPDATED,
  DELETED,
  RESTORED,
  PURGED,
  VALIDATED,
  NONE;

  public static XrefChange from(String value) {
    return value == null ? null : valueOf(value);
  }
}
