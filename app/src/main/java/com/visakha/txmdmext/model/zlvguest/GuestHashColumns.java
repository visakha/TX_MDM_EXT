package com.visakha.txmdmext.model.zlvguest;

import com.visakha.txmdmext.history.MasterVersion;
import com.visakha.txmdmext.history.SourceColumns;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterAddrHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterAltIdHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterPhoneHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterSrcAddrHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMemberHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestSourceHist;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * The columns of each zlv_guest Type 2 history table that may be listed in its txmdmext.md5 formula
 * (application.properties). Table names are the literal TABLE constants of the entities.
 */
public final class GuestHashColumns {

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
          "phone_num",
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

  private GuestHashColumns() {}

  /** Type 2 table -> columns that may be hashed (not hist_id, id01, valid_*, load_dtm, payload). */
  static Map<String, List<String>> hashableColumns() {
    Map<String, List<String>> m = new LinkedHashMap<>();
    m.put(
        GuestMasterHist.TABLE,
        Stream.concat(MasterVersion.HEADER_COLUMNS.stream(), ROOT_COLUMNS.stream()).toList());
    m.put(GuestMasterPhoneHist.TABLE, PHONE_COLUMNS);
    m.put(GuestMasterSrcAddrHist.TABLE, SRC_ADDR_COLUMNS);
    m.put(GuestMasterAddrHist.TABLE, ADDR_COLUMNS);
    m.put(GuestMasterAltIdHist.TABLE, ALT_ID_COLUMNS);
    m.put(GuestMemberHist.TABLE, SourceColumns.MEMBER);
    m.put(GuestSourceHist.TABLE, SourceColumns.SOURCE);
    return m;
  }
}
