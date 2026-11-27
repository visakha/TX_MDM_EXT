package com.visakha.txmdmext.store;

import com.visakha.txmdmext.history.ChildRow;
import com.visakha.txmdmext.history.ChildVersion;
import com.visakha.txmdmext.history.FieldGroup;
import com.visakha.txmdmext.history.Lookup;
import java.sql.Types;
import java.time.LocalDateTime;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The four child-group history tables. Type 2: close the open row, then append. */
@Repository
public class ChildHistoryRepository {

  static final String CLOSE_PHONE =
      """
      UPDATE EVT_MASTER_PHONE_HIST SET valid_to = :validTo, is_current = 0
       WHERE business_id = :businessId AND child_id = :childId AND is_current = 1
      """;

  static final String CLOSE_SRC_ADDR =
      """
      UPDATE EVT_MASTER_SRC_ADDR_HIST SET valid_to = :validTo, is_current = 0
       WHERE business_id = :businessId AND child_id = :childId AND is_current = 1
      """;

  static final String CLOSE_ADDR =
      """
      UPDATE EVT_MASTER_ADDR_HIST SET valid_to = :validTo, is_current = 0
       WHERE business_id = :businessId AND child_id = :childId AND is_current = 1
      """;

  static final String CLOSE_ALT_ID =
      """
      UPDATE EVT_MASTER_ALT_ID_HIST SET valid_to = :validTo, is_current = 0
       WHERE business_id = :businessId AND child_id = :childId AND is_current = 1
      """;

  static final String INSERT_PHONE =
      """
      INSERT INTO EVT_MASTER_PHONE_HIST (
        business_id, child_id, id01, cntry_cd, ph_num, ph_typ_cd, ph_typ_nm, no_mrkt_calls,
        is_deleted, valid_from, valid_to, is_current)
      VALUES (
        :businessId, :childId, :id01, :cntryCd, :phNum, :phTypCd, :phTypNm, :noMrktCalls,
        :isDeleted, :validFrom, NULL, 1)
      """;

  static final String INSERT_SRC_ADDR =
      """
      INSERT INTO EVT_MASTER_SRC_ADDR_HIST (
        business_id, child_id, id01, address_line_1, address_line_2, address_line_3,
        sub_bldg_nm, bldg_name, nearest_airport, bldg_mgmnt_company, city,
        state_cd_cd, state_cd_nm, address_status_cd_cd, address_status_cd_nm,
        address_typ_cd, address_typ_nm, postal_cd, country_cd_cd, country_cd_nm,
        is_deleted, valid_from, valid_to, is_current)
      VALUES (
        :businessId, :childId, :id01, :addressLine1, :addressLine2, :addressLine3,
        :subBldgNm, :bldgName, :nearestAirport, :bldgMgmntCompany, :city,
        :stateCdCd, :stateCdNm, :addressStatusCdCd, :addressStatusCdNm,
        :addressTypCd, :addressTypNm, :postalCd, :countryCdCd, :countryCdNm,
        :isDeleted, :validFrom, NULL, 1)
      """;

  static final String INSERT_ADDR =
      """
      INSERT INTO EVT_MASTER_ADDR_HIST (
        business_id, child_id, id01, default_indicator, address_type_cd, address_type_nm,
        usage_type_cd, usage_type_nm, address_line1, address_line2, address_line3, city, county,
        state_cd, state_nm, country_cd, country_nm, postal_code, postal_code_extension,
        coordinate_system, location_coordinate_desc, address_status_cd, address_status_nm,
        latitude, longitude, effective_start_date, effective_end_date,
        enriched_indicator_cd, enriched_indicator_nm, status_code, status_message, elevation,
        is_deleted, valid_from, valid_to, is_current)
      VALUES (
        :businessId, :childId, :id01, :defaultIndicator, :addressTypeCd, :addressTypeNm,
        :usageTypeCd, :usageTypeNm, :addressLine1, :addressLine2, :addressLine3, :city, :county,
        :stateCd, :stateNm, :countryCd, :countryNm, :postalCode, :postalCodeExtension,
        :coordinateSystem, :locationCoordinateDesc, :addressStatusCd, :addressStatusNm,
        :latitude, :longitude, :effectiveStartDate, :effectiveEndDate,
        :enrichedIndicatorCd, :enrichedIndicatorNm, :statusCode, :statusMessage, :elevation,
        :isDeleted, :validFrom, NULL, 1)
      """;

  static final String INSERT_ALT_ID =
      """
      INSERT INTO EVT_MASTER_ALT_ID_HIST (
        business_id, child_id, id01, alt_id_val, alt_id_typ_cd, alt_id_typ_nm, active_ind,
        start_date, pri_segment_typ_cd, pri_segment_typ_nm, sec_segment_typ_cd,
        sec_segment_typ_nm, user_notes, is_deleted, valid_from, valid_to, is_current)
      VALUES (
        :businessId, :childId, :id01, :altIdVal, :altIdTypCd, :altIdTypNm, :activeInd,
        :startDate, :priSegmentTypCd, :priSegmentTypNm, :secSegmentTypCd,
        :secSegmentTypNm, :userNotes, :isDeleted, :validFrom, NULL, 1)
      """;

  private static final Lookup NO_LOOKUP = new Lookup(null, null);

  private final JdbcClient jdbc;
  private final DbTime time;

  public ChildHistoryRepository(JdbcClient jdbc, DbTime time) {
    this.jdbc = jdbc;
    this.time = time;
  }

  public void closeCurrent(
      FieldGroup group, String businessId, String childId, LocalDateTime validTo) {
    String sql =
        switch (group) {
          case X_PHONE -> CLOSE_PHONE;
          case X_SRC_ADDRESS -> CLOSE_SRC_ADDR;
          case ADDRESS -> CLOSE_ADDR;
          case X_ALT_ID -> CLOSE_ALT_ID;
        };
    jdbc.sql(sql)
        .param("validTo", validTo, Types.TIMESTAMP)
        .param("businessId", businessId, Types.VARCHAR)
        .param("childId", childId, Types.VARCHAR)
        .update();
  }

  public void insert(
      String businessId, String id01, ChildVersion version, LocalDateTime validFrom) {
    JdbcClient.StatementSpec spec =
        switch (version.row()) {
          case ChildRow.Phone p ->
              jdbc.sql(INSERT_PHONE)
                  .param("cntryCd", p.cntryCd(), Types.BIGINT)
                  .param("phNum", p.phNum(), Types.BIGINT)
                  .param("phTypCd", code(p.phTyp()), Types.VARCHAR)
                  .param("phTypNm", name(p.phTyp()), Types.VARCHAR)
                  .param("noMrktCalls", DbTime.flag(p.noMrktCalls()), Types.INTEGER);
          case ChildRow.SrcAddress a ->
              jdbc.sql(INSERT_SRC_ADDR)
                  .param("addressLine1", a.addressLine1(), Types.VARCHAR)
                  .param("addressLine2", a.addressLine2(), Types.VARCHAR)
                  .param("addressLine3", a.addressLine3(), Types.VARCHAR)
                  .param("subBldgNm", a.subBldgNm(), Types.VARCHAR)
                  .param("bldgName", a.bldgName(), Types.VARCHAR)
                  .param("nearestAirport", a.nearestAirport(), Types.VARCHAR)
                  .param("bldgMgmntCompany", a.bldgMgmntCompany(), Types.VARCHAR)
                  .param("city", a.city(), Types.VARCHAR)
                  .param("stateCdCd", code(a.stateCd()), Types.VARCHAR)
                  .param("stateCdNm", name(a.stateCd()), Types.VARCHAR)
                  .param("addressStatusCdCd", code(a.addressStatusCd()), Types.VARCHAR)
                  .param("addressStatusCdNm", name(a.addressStatusCd()), Types.VARCHAR)
                  .param("addressTypCd", code(a.addressTyp()), Types.VARCHAR)
                  .param("addressTypNm", name(a.addressTyp()), Types.VARCHAR)
                  .param("postalCd", a.postalCd(), Types.BIGINT)
                  .param("countryCdCd", code(a.countryCd()), Types.VARCHAR)
                  .param("countryCdNm", name(a.countryCd()), Types.VARCHAR);
          case ChildRow.Address a ->
              jdbc.sql(INSERT_ADDR)
                  .param("defaultIndicator", DbTime.flag(a.defaultIndicator()), Types.INTEGER)
                  .param("addressTypeCd", code(a.addressType()), Types.VARCHAR)
                  .param("addressTypeNm", name(a.addressType()), Types.VARCHAR)
                  .param("usageTypeCd", code(a.usageType()), Types.VARCHAR)
                  .param("usageTypeNm", name(a.usageType()), Types.VARCHAR)
                  .param("addressLine1", a.addressLine1(), Types.VARCHAR)
                  .param("addressLine2", a.addressLine2(), Types.VARCHAR)
                  .param("addressLine3", a.addressLine3(), Types.VARCHAR)
                  .param("city", a.city(), Types.VARCHAR)
                  .param("county", a.county(), Types.VARCHAR)
                  .param("stateCd", code(a.state()), Types.VARCHAR)
                  .param("stateNm", name(a.state()), Types.VARCHAR)
                  .param("countryCd", code(a.country()), Types.VARCHAR)
                  .param("countryNm", name(a.country()), Types.VARCHAR)
                  .param("postalCode", a.postalCode(), Types.VARCHAR)
                  .param("postalCodeExtension", a.postalCodeExtension(), Types.VARCHAR)
                  .param("coordinateSystem", a.coordinateSystem(), Types.VARCHAR)
                  .param("locationCoordinateDesc", a.locationCoordinateDesc(), Types.VARCHAR)
                  .param("addressStatusCd", code(a.addressStatus()), Types.VARCHAR)
                  .param("addressStatusNm", name(a.addressStatus()), Types.VARCHAR)
                  .param("latitude", a.latitude(), Types.VARCHAR)
                  .param("longitude", a.longitude(), Types.VARCHAR)
                  .param("effectiveStartDate", time.of(a.effectiveStartDate()), Types.TIMESTAMP)
                  .param("effectiveEndDate", time.of(a.effectiveEndDate()), Types.TIMESTAMP)
                  .param("enrichedIndicatorCd", code(a.enrichedIndicator()), Types.VARCHAR)
                  .param("enrichedIndicatorNm", name(a.enrichedIndicator()), Types.VARCHAR)
                  .param("statusCode", a.statusCode(), Types.VARCHAR)
                  .param("statusMessage", a.statusMessage(), Types.VARCHAR)
                  .param("elevation", a.elevation(), Types.VARCHAR);
          case ChildRow.AltId a ->
              jdbc.sql(INSERT_ALT_ID)
                  .param("altIdVal", a.altIdVal(), Types.VARCHAR)
                  .param("altIdTypCd", code(a.altIdTyp()), Types.VARCHAR)
                  .param("altIdTypNm", name(a.altIdTyp()), Types.VARCHAR)
                  .param("activeInd", DbTime.flag(a.activeInd()), Types.INTEGER)
                  .param("startDate", DbTime.date(a.startDate()), Types.DATE)
                  .param("priSegmentTypCd", code(a.priSegmentTyp()), Types.VARCHAR)
                  .param("priSegmentTypNm", name(a.priSegmentTyp()), Types.VARCHAR)
                  .param("secSegmentTypCd", code(a.secSegmentTyp()), Types.VARCHAR)
                  .param("secSegmentTypNm", name(a.secSegmentTyp()), Types.VARCHAR)
                  .param("userNotes", a.userNotes(), Types.VARCHAR);
        };
    spec.param("businessId", businessId, Types.VARCHAR)
        .param("childId", version.row().childId(), Types.VARCHAR)
        .param("id01", id01, Types.VARCHAR)
        .param("isDeleted", version.deleted() ? 1 : 0, Types.INTEGER)
        .param("validFrom", validFrom, Types.TIMESTAMP)
        .update();
  }

  private static String code(Lookup l) {
    return (l == null ? NO_LOOKUP : l).code();
  }

  private static String name(Lookup l) {
    return (l == null ? NO_LOOKUP : l).name();
  }
}
