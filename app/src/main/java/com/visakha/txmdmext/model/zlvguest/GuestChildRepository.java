package com.visakha.txmdmext.model.zlvguest;

import com.visakha.txmdmext.history.ChildVersion;
import com.visakha.txmdmext.history.Lookup;
import com.visakha.txmdmext.history.OpenChild;
import com.visakha.txmdmext.model.ChildGroup;
import com.visakha.txmdmext.store.ChildStore;
import com.visakha.txmdmext.store.DbTime;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The four zlv_guest child-group history tables. Type 2: close the open row, then append. */
@Repository
public class GuestChildRepository implements ChildStore {

  static final String OPEN_PHONES =
      """
      SELECT child_id, cntry_cd, ph_num, ph_typ_cd, ph_typ_nm, no_mrkt_calls, row_md5
        FROM %s
       WHERE business_id = :businessId AND is_current = 1 AND is_deleted = 0
      """
          .formatted(GuestTables.MASTER_PHONE);

  static final String OPEN_SRC_ADDRS =
      """
      SELECT child_id, address_line_1, address_line_2, address_line_3, sub_bldg_nm, bldg_name,
             nearest_airport, bldg_mgmnt_company, city, state_cd_cd, state_cd_nm,
             address_status_cd_cd, address_status_cd_nm, address_typ_cd, address_typ_nm,
             postal_cd, country_cd_cd, country_cd_nm, row_md5
        FROM %s
       WHERE business_id = :businessId AND is_current = 1 AND is_deleted = 0
      """
          .formatted(GuestTables.MASTER_SRC_ADDR);

  static final String OPEN_ADDRS =
      """
      SELECT child_id, default_indicator, address_type_cd, address_type_nm, usage_type_cd,
             usage_type_nm, address_line1, address_line2, address_line3, city, county,
             state_cd, state_nm, country_cd, country_nm, postal_code, postal_code_extension,
             coordinate_system, location_coordinate_desc, address_status_cd, address_status_nm,
             latitude, longitude, effective_start_date, effective_end_date,
             enriched_indicator_cd, enriched_indicator_nm, status_code, status_message,
             elevation, row_md5
        FROM %s
       WHERE business_id = :businessId AND is_current = 1 AND is_deleted = 0
      """
          .formatted(GuestTables.MASTER_ADDR);

  static final String OPEN_ALT_IDS =
      """
      SELECT child_id, alt_id_val, alt_id_typ_cd, alt_id_typ_nm, active_ind, start_date,
             pri_segment_typ_cd, pri_segment_typ_nm, sec_segment_typ_cd, sec_segment_typ_nm,
             user_notes, row_md5
        FROM %s
       WHERE business_id = :businessId AND is_current = 1 AND is_deleted = 0
      """
          .formatted(GuestTables.MASTER_ALT_ID);

  static final String CLOSE_PHONE =
      """
      UPDATE %s SET valid_to = :validTo, is_current = 0
       WHERE business_id = :businessId AND child_id = :childId AND is_current = 1
      """
          .formatted(GuestTables.MASTER_PHONE);

  static final String CLOSE_SRC_ADDR =
      """
      UPDATE %s SET valid_to = :validTo, is_current = 0
       WHERE business_id = :businessId AND child_id = :childId AND is_current = 1
      """
          .formatted(GuestTables.MASTER_SRC_ADDR);

  static final String CLOSE_ADDR =
      """
      UPDATE %s SET valid_to = :validTo, is_current = 0
       WHERE business_id = :businessId AND child_id = :childId AND is_current = 1
      """
          .formatted(GuestTables.MASTER_ADDR);

  static final String CLOSE_ALT_ID =
      """
      UPDATE %s SET valid_to = :validTo, is_current = 0
       WHERE business_id = :businessId AND child_id = :childId AND is_current = 1
      """
          .formatted(GuestTables.MASTER_ALT_ID);

  static final String INSERT_PHONE =
      """
      INSERT INTO %s (
        business_id, child_id, id01, cntry_cd, ph_num, ph_typ_cd, ph_typ_nm, no_mrkt_calls,
        is_deleted, row_md5, valid_from, valid_to, is_current)
      VALUES (
        :businessId, :childId, :id01, :cntryCd, :phNum, :phTypCd, :phTypNm, :noMrktCalls,
        :isDeleted, :rowMd5, :validFrom, NULL, 1)
      """
          .formatted(GuestTables.MASTER_PHONE);

  static final String INSERT_SRC_ADDR =
      """
      INSERT INTO %s (
        business_id, child_id, id01, address_line_1, address_line_2, address_line_3,
        sub_bldg_nm, bldg_name, nearest_airport, bldg_mgmnt_company, city,
        state_cd_cd, state_cd_nm, address_status_cd_cd, address_status_cd_nm,
        address_typ_cd, address_typ_nm, postal_cd, country_cd_cd, country_cd_nm,
        is_deleted, row_md5, valid_from, valid_to, is_current)
      VALUES (
        :businessId, :childId, :id01, :addressLine1, :addressLine2, :addressLine3,
        :subBldgNm, :bldgName, :nearestAirport, :bldgMgmntCompany, :city,
        :stateCdCd, :stateCdNm, :addressStatusCdCd, :addressStatusCdNm,
        :addressTypCd, :addressTypNm, :postalCd, :countryCdCd, :countryCdNm,
        :isDeleted, :rowMd5, :validFrom, NULL, 1)
      """
          .formatted(GuestTables.MASTER_SRC_ADDR);

  static final String INSERT_ADDR =
      """
      INSERT INTO %s (
        business_id, child_id, id01, default_indicator, address_type_cd, address_type_nm,
        usage_type_cd, usage_type_nm, address_line1, address_line2, address_line3, city, county,
        state_cd, state_nm, country_cd, country_nm, postal_code, postal_code_extension,
        coordinate_system, location_coordinate_desc, address_status_cd, address_status_nm,
        latitude, longitude, effective_start_date, effective_end_date,
        enriched_indicator_cd, enriched_indicator_nm, status_code, status_message, elevation,
        is_deleted, row_md5, valid_from, valid_to, is_current)
      VALUES (
        :businessId, :childId, :id01, :defaultIndicator, :addressTypeCd, :addressTypeNm,
        :usageTypeCd, :usageTypeNm, :addressLine1, :addressLine2, :addressLine3, :city, :county,
        :stateCd, :stateNm, :countryCd, :countryNm, :postalCode, :postalCodeExtension,
        :coordinateSystem, :locationCoordinateDesc, :addressStatusCd, :addressStatusNm,
        :latitude, :longitude, :effectiveStartDate, :effectiveEndDate,
        :enrichedIndicatorCd, :enrichedIndicatorNm, :statusCode, :statusMessage, :elevation,
        :isDeleted, :rowMd5, :validFrom, NULL, 1)
      """
          .formatted(GuestTables.MASTER_ADDR);

  static final String INSERT_ALT_ID =
      """
      INSERT INTO %s (
        business_id, child_id, id01, alt_id_val, alt_id_typ_cd, alt_id_typ_nm, active_ind,
        start_date, pri_segment_typ_cd, pri_segment_typ_nm, sec_segment_typ_cd,
        sec_segment_typ_nm, user_notes, is_deleted, row_md5, valid_from, valid_to, is_current)
      VALUES (
        :businessId, :childId, :id01, :altIdVal, :altIdTypCd, :altIdTypNm, :activeInd,
        :startDate, :priSegmentTypCd, :priSegmentTypNm, :secSegmentTypCd,
        :secSegmentTypNm, :userNotes, :isDeleted, :rowMd5, :validFrom, NULL, 1)
      """
          .formatted(GuestTables.MASTER_ALT_ID);

  private static final Lookup NO_LOOKUP = new Lookup(null, null);

  private final JdbcClient jdbc;
  private final DbTime time;

  public GuestChildRepository(JdbcClient jdbc, DbTime time) {
    this.jdbc = jdbc;
    this.time = time;
  }

  @Override
  public Map<String, OpenChild> openRows(ChildGroup group, String businessId) {
    String sql =
        switch ((FieldGroup) group) {
          case X_PHONE -> OPEN_PHONES;
          case X_SRC_ADDRESS -> OPEN_SRC_ADDRS;
          case ADDRESS -> OPEN_ADDRS;
          case X_ALT_ID -> OPEN_ALT_IDS;
        };
    List<OpenChild> rows =
        jdbc.sql(sql)
            .param("businessId", businessId)
            .query((rs, n) ->
                new OpenChild(row((FieldGroup) group, rs), trim(rs.getString("row_md5"))))
            .list();
    Map<String, OpenChild> byId = new LinkedHashMap<>();
    rows.forEach(o -> byId.put(o.row().childId(), o));
    return byId;
  }

  private GuestChild row(FieldGroup group, ResultSet rs) throws SQLException {
    String id = rs.getString("child_id");
    return switch (group) {
      case X_PHONE ->
          new GuestChild.Phone(
              id,
              whole(rs, "cntry_cd"),
              whole(rs, "ph_num"),
              lookup(rs, "ph_typ_cd", "ph_typ_nm"),
              bool(rs, "no_mrkt_calls"));
      case X_SRC_ADDRESS ->
          new GuestChild.SrcAddress(
              id,
              rs.getString("address_line_1"),
              rs.getString("address_line_2"),
              rs.getString("address_line_3"),
              rs.getString("sub_bldg_nm"),
              rs.getString("bldg_name"),
              rs.getString("nearest_airport"),
              rs.getString("bldg_mgmnt_company"),
              rs.getString("city"),
              lookup(rs, "state_cd_cd", "state_cd_nm"),
              lookup(rs, "address_status_cd_cd", "address_status_cd_nm"),
              lookup(rs, "address_typ_cd", "address_typ_nm"),
              whole(rs, "postal_cd"),
              lookup(rs, "country_cd_cd", "country_cd_nm"));
      case ADDRESS ->
          new GuestChild.Address(
              id,
              bool(rs, "default_indicator"),
              lookup(rs, "address_type_cd", "address_type_nm"),
              lookup(rs, "usage_type_cd", "usage_type_nm"),
              rs.getString("address_line1"),
              rs.getString("address_line2"),
              rs.getString("address_line3"),
              rs.getString("city"),
              rs.getString("county"),
              lookup(rs, "state_cd", "state_nm"),
              lookup(rs, "country_cd", "country_nm"),
              rs.getString("postal_code"),
              rs.getString("postal_code_extension"),
              rs.getString("coordinate_system"),
              rs.getString("location_coordinate_desc"),
              lookup(rs, "address_status_cd", "address_status_nm"),
              rs.getString("latitude"),
              rs.getString("longitude"),
              time.instant(rs.getObject("effective_start_date", LocalDateTime.class)),
              time.instant(rs.getObject("effective_end_date", LocalDateTime.class)),
              lookup(rs, "enriched_indicator_cd", "enriched_indicator_nm"),
              rs.getString("status_code"),
              rs.getString("status_message"),
              rs.getString("elevation"));
      case X_ALT_ID ->
          new GuestChild.AltId(
              id,
              rs.getString("alt_id_val"),
              lookup(rs, "alt_id_typ_cd", "alt_id_typ_nm"),
              bool(rs, "active_ind"),
              date(rs, "start_date"),
              lookup(rs, "pri_segment_typ_cd", "pri_segment_typ_nm"),
              lookup(rs, "sec_segment_typ_cd", "sec_segment_typ_nm"),
              rs.getString("user_notes"));
    };
  }

  private static Long whole(ResultSet rs, String col) throws SQLException {
    long v = rs.getLong(col);
    return rs.wasNull() ? null : v;
  }

  private static Boolean bool(ResultSet rs, String col) throws SQLException {
    int v = rs.getInt(col);
    return rs.wasNull() ? null : v == 1;
  }

  private static LocalDate date(ResultSet rs, String col) throws SQLException {
    java.sql.Date d = rs.getDate(col);
    return d == null ? null : d.toLocalDate();
  }

  private static Lookup lookup(ResultSet rs, String codeCol, String nameCol) throws SQLException {
    String code = rs.getString(codeCol);
    String name = rs.getString(nameCol);
    return code == null && name == null ? null : new Lookup(code, name);
  }

  private static String trim(String s) {
    return s == null ? null : s.trim();
  }

  @Override
  public void closeCurrent(
      ChildGroup group, String businessId, String childId, LocalDateTime validTo) {
    String sql =
        switch ((FieldGroup) group) {
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

  @Override
  public void insert(
      String businessId, String id01, ChildVersion version, LocalDateTime validFrom) {
    JdbcClient.StatementSpec spec =
        switch ((GuestChild) version.row()) {
          case GuestChild.Phone p ->
              jdbc.sql(INSERT_PHONE)
                  .param("cntryCd", p.cntryCd(), Types.BIGINT)
                  .param("phNum", p.phNum(), Types.BIGINT)
                  .param("phTypCd", code(p.phTyp()), Types.VARCHAR)
                  .param("phTypNm", name(p.phTyp()), Types.VARCHAR)
                  .param("noMrktCalls", DbTime.flag(p.noMrktCalls()), Types.INTEGER);
          case GuestChild.SrcAddress a ->
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
          case GuestChild.Address a ->
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
          case GuestChild.AltId a ->
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
        .param("rowMd5", version.rowMd5(), Types.CHAR)
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
