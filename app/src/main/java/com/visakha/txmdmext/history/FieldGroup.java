package com.visakha.txmdmext.history;

/** Many-valued field groups of zlv_guest. jsonName is the key in the event payload. */
public enum FieldGroup {
  X_PHONE("x_phone"),
  X_SRC_ADDRESS("x_src_address"),
  ADDRESS("Address"),
  X_ALT_ID("x_alt_id");

  private final String jsonName;

  FieldGroup(String jsonName) {
    this.jsonName = jsonName;
  }

  public String jsonName() {
    return jsonName;
  }
}
