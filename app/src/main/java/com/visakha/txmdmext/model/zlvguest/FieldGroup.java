package com.visakha.txmdmext.model.zlvguest;

import com.visakha.txmdmext.model.ChildGroup;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterAddrHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterAltIdHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterPhoneHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterSrcAddrHist;

/**
 * Many-valued field groups of zlv_guest. jsonName is the key in the event payload, table the @Table
 * name of the group's entity.
 */
public enum FieldGroup implements ChildGroup {
  X_PHONE("x_phone", GuestMasterPhoneHist.TABLE),
  X_SRC_ADDRESS("x_src_address", GuestMasterSrcAddrHist.TABLE),
  ADDRESS("Address", GuestMasterAddrHist.TABLE),
  X_ALT_ID("x_alt_id", GuestMasterAltIdHist.TABLE);

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
