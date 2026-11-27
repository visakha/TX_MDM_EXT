package com.visakha.txmdmext.history;

import java.util.LinkedHashMap;
import java.util.Map;

/** Column name -> value of each history row, the input of RowHash. Keys are V001 column names. */
public final class HashValues {

  private HashValues() {}

  public static Map<String, Object> master(MasterVersion v) {
    Map<String, Object> m = new LinkedHashMap<>();
    MetaInfo meta = v.meta();
    GuestRoot r = v.root();
    m.put("business_id", v.businessId());
    m.put("active_business_id", v.activeBusinessId());
    m.put("event_type", v.eventType());
    m.put("implied_operation", v.impliedOperation());
    m.put("correlation_id", v.correlationId());
    m.put("date_of_change", v.dateOfChange());
    m.put("user_name", v.userName());
    m.put("event_source_system", v.eventSourceSystem());
    m.put("merged_business_ids", v.mergedBusinessIds());
    m.put("state_base", v.stateBase());
    m.put("state_validation", v.stateValidation());
    m.put("state_create_mode", v.stateCreateMode());
    m.put("meta_status", meta.status());
    m.put("meta_created_by", meta.createdBy());
    m.put("meta_creation_date", meta.creationDate());
    m.put("meta_updated_by", meta.updatedBy());
    m.put("meta_last_update_date", meta.lastUpdateDate());
    m.put("first_name", r.firstName());
    m.put("clns_first_name", r.clnsFirstName());
    m.put("last_name", r.lastName());
    m.put("clns_last_name", r.clnsLastName());
    m.put("ffr_first_name", r.ffrFirstName());
    m.put("ffr_last_name", r.ffrLastName());
    m.put("do_not_contact_ind", r.doNotContactInd());
    m.put("linkedin_url", r.linkedinUrl());
    m.put("last_touch_dtm", r.lastTouchDtm());
    m.put("prev_last_touch_dtm", r.prevLastTouchDtm());
    m.put("touch_incr", r.touchIncr());
    m.put("touch_notes", r.touchNotes());
    m.put("touch_cd_cd", code(r.touchCd()));
    m.put("touch_cd_nm", name(r.touchCd()));
    m.put("is_deleted", v.deleted());
    return m;
  }

  public static Map<String, Object> child(String businessId, ChildRow row, boolean deleted) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("business_id", businessId);
    m.put("child_id", row.childId());
    switch (row) {
      case ChildRow.Phone p -> {
        m.put("cntry_cd", p.cntryCd());
        m.put("ph_num", p.phNum());
        m.put("ph_typ_cd", code(p.phTyp()));
        m.put("ph_typ_nm", name(p.phTyp()));
        m.put("no_mrkt_calls", p.noMrktCalls());
      }
      case ChildRow.SrcAddress a -> {
        m.put("address_line_1", a.addressLine1());
        m.put("address_line_2", a.addressLine2());
        m.put("address_line_3", a.addressLine3());
        m.put("sub_bldg_nm", a.subBldgNm());
        m.put("bldg_name", a.bldgName());
        m.put("nearest_airport", a.nearestAirport());
        m.put("bldg_mgmnt_company", a.bldgMgmntCompany());
        m.put("city", a.city());
        m.put("state_cd_cd", code(a.stateCd()));
        m.put("state_cd_nm", name(a.stateCd()));
        m.put("address_status_cd_cd", code(a.addressStatusCd()));
        m.put("address_status_cd_nm", name(a.addressStatusCd()));
        m.put("address_typ_cd", code(a.addressTyp()));
        m.put("address_typ_nm", name(a.addressTyp()));
        m.put("postal_cd", a.postalCd());
        m.put("country_cd_cd", code(a.countryCd()));
        m.put("country_cd_nm", name(a.countryCd()));
      }
      case ChildRow.Address a -> {
        m.put("default_indicator", a.defaultIndicator());
        m.put("address_type_cd", code(a.addressType()));
        m.put("address_type_nm", name(a.addressType()));
        m.put("usage_type_cd", code(a.usageType()));
        m.put("usage_type_nm", name(a.usageType()));
        m.put("address_line1", a.addressLine1());
        m.put("address_line2", a.addressLine2());
        m.put("address_line3", a.addressLine3());
        m.put("city", a.city());
        m.put("county", a.county());
        m.put("state_cd", code(a.state()));
        m.put("state_nm", name(a.state()));
        m.put("country_cd", code(a.country()));
        m.put("country_nm", name(a.country()));
        m.put("postal_code", a.postalCode());
        m.put("postal_code_extension", a.postalCodeExtension());
        m.put("coordinate_system", a.coordinateSystem());
        m.put("location_coordinate_desc", a.locationCoordinateDesc());
        m.put("address_status_cd", code(a.addressStatus()));
        m.put("address_status_nm", name(a.addressStatus()));
        m.put("latitude", a.latitude());
        m.put("longitude", a.longitude());
        m.put("effective_start_date", a.effectiveStartDate());
        m.put("effective_end_date", a.effectiveEndDate());
        m.put("enriched_indicator_cd", code(a.enrichedIndicator()));
        m.put("enriched_indicator_nm", name(a.enrichedIndicator()));
        m.put("status_code", a.statusCode());
        m.put("status_message", a.statusMessage());
        m.put("elevation", a.elevation());
      }
      case ChildRow.AltId a -> {
        m.put("alt_id_val", a.altIdVal());
        m.put("alt_id_typ_cd", code(a.altIdTyp()));
        m.put("alt_id_typ_nm", name(a.altIdTyp()));
        m.put("active_ind", a.activeInd());
        m.put("start_date", a.startDate());
        m.put("pri_segment_typ_cd", code(a.priSegmentTyp()));
        m.put("pri_segment_typ_nm", name(a.priSegmentTyp()));
        m.put("sec_segment_typ_cd", code(a.secSegmentTyp()));
        m.put("sec_segment_typ_nm", name(a.secSegmentTyp()));
        m.put("user_notes", a.userNotes());
      }
    }
    m.put("is_deleted", deleted);
    return m;
  }

  public static Map<String, Object> member(String businessId, MemberKey key) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("business_id", businessId);
    m.put("source_system", key.sourceSystem());
    m.put("source_pkey", key.sourcePkey());
    return m;
  }

  private static String code(Lookup l) {
    return l == null ? null : l.code();
  }

  private static String name(Lookup l) {
    return l == null ? null : l.name();
  }
}
