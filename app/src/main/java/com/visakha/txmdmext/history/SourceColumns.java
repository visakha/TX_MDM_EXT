package com.visakha.txmdmext.history;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Columns of the generic, model-independent history tables that may be hashed: membership and
 * source history look the same for every model, only their table names differ.
 */
public final class SourceColumns {

  public static final List<String> MEMBER = List.of("business_id", "source_system", "source_pkey");

  public static final List<String> SOURCE =
      List.of(
          "source_system", "source_pkey", "business_id", "xref_type", "is_deleted", "source_json");

  private SourceColumns() {}

  public static Map<String, Object> member(String businessId, MemberKey key) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("business_id", businessId);
    m.put("source_system", key.sourceSystem());
    m.put("source_pkey", key.sourcePkey());
    return m;
  }
}
