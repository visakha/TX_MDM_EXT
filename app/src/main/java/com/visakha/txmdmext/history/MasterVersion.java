package com.visakha.txmdmext.history;

import com.visakha.txmdmext.model.EntityRoot;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One new master history row. The event header, states and _meta are the same for every model; root
 * holds the model fields. The previous current row for businessId gets closed.
 */
public record MasterVersion(
    String businessId,
    String activeBusinessId,
    String id01,
    String eventType,
    String impliedOperation,
    String correlationId,
    Long dateOfChange,
    String userName,
    String eventSourceSystem,
    String mergedBusinessIds,
    String stateBase,
    String stateValidation,
    String stateCreateMode,
    MetaInfo meta,
    EntityRoot root,
    boolean deleted,
    String payloadJson,
    Instant validFrom,
    String rowMd5) {

  /** The model-independent master columns, in table order. */
  public static final List<String> HEADER_COLUMNS =
      List.of(
          "business_id",
          "active_business_id",
          "event_type",
          "implied_operation",
          "correlation_id",
          "date_of_change",
          "user_name",
          "event_source_system",
          "merged_business_ids",
          "state_base",
          "state_validation",
          "state_create_mode",
          "meta_status",
          "meta_created_by",
          "meta_creation_date",
          "meta_updated_by",
          "meta_last_update_date");

  /** Column name -> value of the header columns, for RowHash. Models add their root columns. */
  public Map<String, Object> headerValues() {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("business_id", businessId);
    m.put("active_business_id", activeBusinessId);
    m.put("event_type", eventType);
    m.put("implied_operation", impliedOperation);
    m.put("correlation_id", correlationId);
    m.put("date_of_change", dateOfChange);
    m.put("user_name", userName);
    m.put("event_source_system", eventSourceSystem);
    m.put("merged_business_ids", mergedBusinessIds);
    m.put("state_base", stateBase);
    m.put("state_validation", stateValidation);
    m.put("state_create_mode", stateCreateMode);
    m.put("meta_status", meta.status());
    m.put("meta_created_by", meta.createdBy());
    m.put("meta_creation_date", meta.creationDate());
    m.put("meta_updated_by", meta.updatedBy());
    m.put("meta_last_update_date", meta.lastUpdateDate());
    return m;
  }

  public MasterVersion withRowMd5(String md5) {
    return new MasterVersion(
        businessId,
        activeBusinessId,
        id01,
        eventType,
        impliedOperation,
        correlationId,
        dateOfChange,
        userName,
        eventSourceSystem,
        mergedBusinessIds,
        stateBase,
        stateValidation,
        stateCreateMode,
        meta,
        root,
        deleted,
        payloadJson,
        validFrom,
        md5);
  }
}
