package com.visakha.txmdmext.model.zlvguest;

import com.visakha.txmdmext.history.MasterVersion;
import com.visakha.txmdmext.model.ChildGroup;
import com.visakha.txmdmext.model.ChildRow;
import com.visakha.txmdmext.model.EntityModel;
import com.visakha.txmdmext.model.EntityRoot;
import com.visakha.txmdmext.model.zlvguest.entity.GuestFieldChange;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMemberHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestSourceHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestXrefChange;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

/** The zlv_guest business entity. Everything that names a guest field or table lives here. */
@Component
public class ZlvGuestModel implements EntityModel {

  public static final String NAME = "zlv_guest";

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public String masterTable() {
    return GuestMasterHist.TABLE;
  }

  @Override
  public String fieldChangeTable() {
    return GuestFieldChange.TABLE;
  }

  @Override
  public String memberTable() {
    return GuestMemberHist.TABLE;
  }

  @Override
  public String xrefTable() {
    return GuestXrefChange.TABLE;
  }

  @Override
  public String sourceTable() {
    return GuestSourceHist.TABLE;
  }

  @Override
  public List<ChildGroup> childGroups() {
    return Arrays.<ChildGroup>asList(FieldGroup.values());
  }

  @Override
  public Map<String, List<String>> hashableColumns() {
    return GuestHashColumns.hashableColumns();
  }

  @Override
  public EntityRoot root(JsonNode payload) {
    return GuestPayloadReader.root(payload);
  }

  @Override
  public List<ChildRow> children(JsonNode payload, ChildGroup group) {
    return GuestPayloadReader.children(payload, (FieldGroup) group);
  }

  @Override
  public Map<String, Object> masterValues(MasterVersion version) {
    return GuestHashValues.master(version);
  }

  @Override
  public Map<String, Object> childValues(String businessId, ChildRow row, boolean deleted) {
    return GuestHashValues.child(businessId, (GuestChild) row, deleted);
  }
}
