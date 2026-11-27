package com.visakha.txmdmext.event;

/** xrefType from the schema. PATCH = UI edit, DATA = source record. */
public enum XrefType {
  DATA,
  PATCH,
  DELETION,
  ANCHOR;

  public static XrefType from(String value) {
    return value == null ? null : valueOf(value);
  }
}
