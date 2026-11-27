package com.visakha.txmdmext.history;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * row_md5 for change data capture. The formula per table (which columns, in which order) comes from
 * application.properties (txmdmext.md5.TABLE=col1,col2,...); this class only normalizes, joins with
 * '|' and hashes. Startup fails fast on a missing table or an unknown column.
 */
public final class RowHash {

  /** Stands for a null value, so null and "" hash differently. */
  public static final String NULL_MARKER = "\\N";

  public static final String DELIMITER = "|";

  private final Map<String, List<String>> formulas;

  private RowHash(Map<String, List<String>> formulas) {
    this.formulas = formulas;
  }

  /**
   * Builds and validates the formulas from the txmdmext.md5 map of application.properties against
   * the model's tables and their hashable columns (EntityModel.hashableColumns).
   */
  public static RowHash fromConfig(
      Map<String, List<String>> config, Map<String, List<String>> knownColumns) {
    List<String> errors = new ArrayList<>();
    Map<String, List<String>> formulas = new LinkedHashMap<>();
    Map<String, List<String>> safe = config == null ? Map.of() : config;
    safe.forEach((key, columns) -> {
      String table = tableFor(key, knownColumns.keySet());
      if (table == null) {
        errors.add("txmdmext.md5." + key + ": not a Type 2 history table");
        return;
      }
      List<String> cols =
          (columns == null ? List.<String>of() : columns)
              .stream()
                  .map(c -> c.trim().toLowerCase(Locale.ROOT))
                  .filter(c -> !c.isEmpty())
                  .toList();
      if (cols.isEmpty()) {
        errors.add("txmdmext.md5." + table + ": column list is empty");
      }
      cols.stream()
          .filter(c -> !knownColumns.get(table).contains(c))
          .forEach(c ->
              errors.add(
                  "txmdmext.md5."
                      + table
                      + ": unknown column '"
                      + c
                      + "'. Known: "
                      + String.join(",", knownColumns.get(table))));
      if (new LinkedHashSet<>(cols).size() != cols.size()) {
        errors.add("txmdmext.md5." + table + ": a column is listed twice");
      }
      formulas.put(table, cols);
    });
    for (String table : knownColumns.keySet()) {
      if (!formulas.containsKey(table)) {
        errors.add("txmdmext.md5." + table + " is missing (no default, list its columns)");
      }
    }
    if (!errors.isEmpty()) {
      throw new IllegalStateException(
          "Invalid row_md5 configuration in application.properties:\n  "
              + String.join("\n  ", errors));
    }
    return new RowHash(formulas);
  }

  public List<String> columns(String table) {
    return formulas.get(table);
  }

  /** The joined, normalized text that is hashed. Public so tests and docs can show it. */
  public String canonical(String table, Map<String, Object> values) {
    return formulas.get(table).stream()
        .map(col -> normalize(values.get(col)))
        .collect(Collectors.joining(DELIMITER));
  }

  public String md5(String table, Map<String, Object> values) {
    try {
      byte[] digest =
          MessageDigest.getInstance("MD5")
              .digest(canonical(table, values).getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("MD5 not available in this JVM", e);
    }
  }

  /**
   * null -> \N; text trimmed with '\' and '|' escaped; booleans 1/0 like the columns; numbers plain
   * without trailing zeros; Instant as ISO UTC; LocalDate as ISO date.
   */
  static String normalize(Object value) {
    return switch (value) {
      case null -> NULL_MARKER;
      case String s -> s.trim().replace("\\", "\\\\").replace("|", "\\|");
      case Boolean b -> b ? "1" : "0";
      case Double d -> BigDecimal.valueOf(d).stripTrailingZeros().toPlainString();
      case BigDecimal d -> d.stripTrailingZeros().toPlainString();
      case Number n -> new BigDecimal(n.toString()).stripTrailingZeros().toPlainString();
      case Instant i -> i.toString();
      case LocalDate d -> d.toString();
      default -> normalize(value.toString());
    };
  }

  private static String tableFor(String key, Set<String> tables) {
    String wanted = letters(key);
    for (String t : tables) {
      if (letters(t).equals(wanted)) {
        return t;
      }
    }
    return null;
  }

  // Spring may hand map keys back without '_' or in another case; compare letters only.
  private static String letters(String s) {
    return s.chars()
        .filter(Character::isLetterOrDigit)
        .mapToObj(c -> String.valueOf((char) Character.toUpperCase(c)))
        .collect(Collectors.joining());
  }

  /** For checks: the set of tables a config covers. */
  public Set<String> tables() {
    return formulas.keySet();
  }
}
