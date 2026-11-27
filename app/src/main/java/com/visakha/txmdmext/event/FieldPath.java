package com.visakha.txmdmext.event;

/**
 * A dataDiff fieldPath split into parts. "grp[abc].typ.Code" gives group grp, child abc, field
 * typ.Code. A root field like "name" has no group and no child. Same rule for every model.
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
