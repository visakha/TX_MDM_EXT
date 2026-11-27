package com.visakha.txmdmext.store;

import java.util.regex.Pattern;

/**
 * Table names are put into SQL text (they come from the model constants, never from input). They
 * are checked once so nothing but a plain Oracle identifier can get in.
 */
public final class SqlNames {

  private static final Pattern IDENTIFIER = Pattern.compile("[A-Z][A-Z0-9_]{0,127}");

  private SqlNames() {}

  public static String table(String name) {
    if (name == null || !IDENTIFIER.matcher(name).matches()) {
      throw new IllegalArgumentException("not a plain table name: " + name);
    }
    return name;
  }
}
