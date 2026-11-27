package com.visakha.txmdmext.model.zlvguest.entity;

/** NUMBER(1) flag values of the history tables: is_current, is_deleted and the _ind columns. */
public final class HistFlags {

  public static final int YES = 1;
  public static final int NO = 0;

  private HistFlags() {}

  public static Integer of(Boolean value) {
    return value == null ? null : (value ? YES : NO);
  }

  public static int of(boolean value) {
    return value ? YES : NO;
  }

  public static Boolean bool(Integer value) {
    return value == null ? null : value == YES;
  }
}
