package com.visakha.txmdmext.history;

import java.util.List;

/**
 * The Type 2 history tables that carry row_md5, with every column that MAY be part of the hash
 * (business columns of V001; not hist_id, id01, valid_from/to, is_current, load_dtm, payload).
 */
public enum HistTable {
  EVT_MASTER_HIST(
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
          "meta_last_update_date",
          "first_name",
          "clns_first_name",
          "last_name",
          "clns_last_name",
          "ffr_first_name",
          "ffr_last_name",
          "do_not_contact_ind",
          "linkedin_url",
          "last_touch_dtm",
          "prev_last_touch_dtm",
          "touch_incr",
          "touch_notes",
          "touch_cd_cd",
          "touch_cd_nm",
          "is_deleted")),
  EVT_MASTER_PHONE_HIST(
      List.of(
          "business_id",
          "child_id",
          "cntry_cd",
          "ph_num",
          "ph_typ_cd",
          "ph_typ_nm",
          "no_mrkt_calls",
          "is_deleted")),
  EVT_MASTER_SRC_ADDR_HIST(
      List.of(
          "business_id",
          "child_id",
          "address_line_1",
          "address_line_2",
          "address_line_3",
          "sub_bldg_nm",
          "bldg_name",
          "nearest_airport",
          "bldg_mgmnt_company",
          "city",
          "state_cd_cd",
          "state_cd_nm",
          "address_status_cd_cd",
          "address_status_cd_nm",
          "address_typ_cd",
          "address_typ_nm",
          "postal_cd",
          "country_cd_cd",
          "country_cd_nm",
          "is_deleted")),
  EVT_MASTER_ADDR_HIST(
      List.of(
          "business_id",
          "child_id",
          "default_indicator",
          "address_type_cd",
          "address_type_nm",
          "usage_type_cd",
          "usage_type_nm",
          "address_line1",
          "address_line2",
          "address_line3",
          "city",
          "county",
          "state_cd",
          "state_nm",
          "country_cd",
          "country_nm",
          "postal_code",
          "postal_code_extension",
          "coordinate_system",
          "location_coordinate_desc",
          "address_status_cd",
          "address_status_nm",
          "latitude",
          "longitude",
          "effective_start_date",
          "effective_end_date",
          "enriched_indicator_cd",
          "enriched_indicator_nm",
          "status_code",
          "status_message",
          "elevation",
          "is_deleted")),
  EVT_MASTER_ALT_ID_HIST(
      List.of(
          "business_id",
          "child_id",
          "alt_id_val",
          "alt_id_typ_cd",
          "alt_id_typ_nm",
          "active_ind",
          "start_date",
          "pri_segment_typ_cd",
          "pri_segment_typ_nm",
          "sec_segment_typ_cd",
          "sec_segment_typ_nm",
          "user_notes",
          "is_deleted")),
  EVT_MEMBER_HIST(List.of("business_id", "source_system", "source_pkey")),
  EVT_SOURCE_HIST(
      List.of(
          "source_system", "source_pkey", "business_id", "xref_type", "is_deleted", "source_json"));

  private final List<String> knownColumns;

  HistTable(List<String> knownColumns) {
    this.knownColumns = knownColumns;
  }

  public List<String> knownColumns() {
    return knownColumns;
  }

  public static HistTable of(FieldGroup group) {
    return switch (group) {
      case X_PHONE -> EVT_MASTER_PHONE_HIST;
      case X_SRC_ADDRESS -> EVT_MASTER_SRC_ADDR_HIST;
      case ADDRESS -> EVT_MASTER_ADDR_HIST;
      case X_ALT_ID -> EVT_MASTER_ALT_ID_HIST;
    };
  }
}
