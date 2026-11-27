package com.visakha.txmdmext.model.zlvguest;

import com.visakha.txmdmext.history.MasterVersion;
import com.visakha.txmdmext.history.SourceColumns;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The zlv_guest history tables (ddl/V001, ddl/V002). Every name carries the domain prefix so other
 * domains (person, provider, ...) get their own tables. Names over 30 chars need Oracle 12.2+.
 */
public final class GuestTables {

  public static final String PREFIX = "EVT_ZLV_GUEST_";

  public static final String MASTER = PREFIX + "MASTER_HIST";
  public static final String MASTER_PHONE = PREFIX + "MASTER_PHONE_HIST";
  public static final String MASTER_SRC_ADDR = PREFIX + "MASTER_SRC_ADDR_HIST";
  public static final String MASTER_ADDR = PREFIX + "MASTER_ADDR_HIST";
  public static final String MASTER_ALT_ID = PREFIX + "MASTER_ALT_ID_HIST";
  public static final String FIELD_CHANGE = PREFIX + "FIELD_CHANGE";
  public static final String MEMBER = PREFIX + "MEMBER_HIST";
  public static final String XREF_CHANGE = PREFIX + "XREF_CHANGE";
  public static final String SOURCE = PREFIX + "SOURCE_HIST";

  /** Root (single-valued) zlv_guest columns of the master table, then is_deleted. */
  static final List<String> ROOT_COLUMNS =
      List.of(
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
          "is_deleted");

  static final List<String> PHONE_COLUMNS =
      List.of(
          "business_id",
          "child_id",
          "cntry_cd",
          "ph_num",
          "ph_typ_cd",
          "ph_typ_nm",
          "no_mrkt_calls",
          "is_deleted");

  static final List<String> SRC_ADDR_COLUMNS =
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
          "is_deleted");

  static final List<String> ADDR_COLUMNS =
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
          "is_deleted");

  static final List<String> ALT_ID_COLUMNS =
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
          "is_deleted");

  private GuestTables() {}

  /** Type 2 table -> columns that may be hashed (not hist_id, id01, valid_*, load_dtm, payload). */
  static Map<String, List<String>> hashableColumns() {
    Map<String, List<String>> m = new LinkedHashMap<>();
    m.put(
        MASTER,
        Stream.concat(MasterVersion.HEADER_COLUMNS.stream(), ROOT_COLUMNS.stream()).toList());
    m.put(MASTER_PHONE, PHONE_COLUMNS);
    m.put(MASTER_SRC_ADDR, SRC_ADDR_COLUMNS);
    m.put(MASTER_ADDR, ADDR_COLUMNS);
    m.put(MASTER_ALT_ID, ALT_ID_COLUMNS);
    m.put(MEMBER, SourceColumns.MEMBER);
    m.put(SOURCE, SourceColumns.SOURCE);
    return m;
  }
}
