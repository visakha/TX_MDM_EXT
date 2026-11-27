package com.visakha.txmdmext.event;

/**
 * A dataDiff fieldPath split into parts. "x_phone[abc].ph_typ.Code" gives group x_phone, child abc,
 * field ph_typ.Code. A root field like "last_name" has no group and no child.
 */
public record FieldPath(String group, String childId, String fieldName) {

  public static FieldPath parse(String path) {
    int open = path.indexOf('[');
    int close = path.indexOf("].");
    if (open <= 0 || close < open) {
      return new FieldPath(null, null, path);
    }
    return new FieldPath(
        path.substring(0, open), path.substring(open + 1, close), path.substring(close + 2));
  }
}
