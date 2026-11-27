package com.visakha.txmdmext.event;

/** impliedOperation enum from the payload schema (pub_evt_zlv_guest schema 002). */
public enum ImpliedOperation {
  CREATE,
  UPDATE,
  DELETE,
  RESTORE,
  MERGE,
  UNMERGE,
  PURGE,
  TRUST_OVERRIDE,
  VALIDATE,
  UNKNOWN;

  public static ImpliedOperation from(String value) {
    return value == null ? UNKNOWN : valueOf(value);
  }
}
