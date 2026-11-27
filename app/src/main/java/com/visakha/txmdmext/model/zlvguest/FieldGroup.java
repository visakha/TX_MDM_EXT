package com.visakha.txmdmext.model.zlvguest;

import com.visakha.txmdmext.model.ChildGroup;

/** Many-valued field groups of zlv_guest. jsonName is the key in the event payload. */
public enum FieldGroup implements ChildGroup {
  X_PHONE("x_phone", GuestTables.MASTER_PHONE),
  X_SRC_ADDRESS("x_src_address", GuestTables.MASTER_SRC_ADDR),
  ADDRESS("Address", GuestTables.MASTER_ADDR),
  X_ALT_ID("x_alt_id", GuestTables.MASTER_ALT_ID);

  private final String jsonName;
  private final String table;

  FieldGroup(String jsonName, String table) {
    this.jsonName = jsonName;
    this.table = table;
  }

  @Override
  public String jsonName() {
    return jsonName;
  }

  @Override
  public String table() {
    return table;
  }
}
