package com.visakha.txmdmext.model;

import com.visakha.txmdmext.history.MasterVersion;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * Everything the generic engine needs to know about one business entity (for example zlv_guest):
 * its table names, its field groups, how to read its payload and its hash input. Implemented once
 * per model, in its own package under model (zlv_guest: package model.zlvguest).
 */
public interface EntityModel {

  /** The business entity name, for logs. */
  String name();

  String masterTable();

  String fieldChangeTable();

  String memberTable();

  String xrefTable();

  String sourceTable();

  List<ChildGroup> childGroups();

  /** Type 2 table -> every column that may be listed in its txmdmext.md5 formula. */
  Map<String, List<String>> hashableColumns();

  EntityRoot root(JsonNode payload);

  /** All rows of one group in the payload. A group missing from the payload means no rows. */
  List<ChildRow> children(JsonNode payload, ChildGroup group);

  /** Column name -> value of a master row, the input of RowHash. */
  Map<String, Object> masterValues(MasterVersion version);

  /** Column name -> value of a child row, the input of RowHash. */
  Map<String, Object> childValues(String businessId, ChildRow row, boolean deleted);
}
