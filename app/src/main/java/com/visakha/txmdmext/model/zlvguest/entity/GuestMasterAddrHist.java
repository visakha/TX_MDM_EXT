package com.visakha.txmdmext.model.zlvguest.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** One row of EVT_ZLV_GUEST_MASTER_ADDR_HIST (ddl/V002): the OOTB Address group, Type 2. */
@Table(GuestMasterAddrHist.TABLE)
public record GuestMasterAddrHist(
    @Id @Column("hist_id") Long histId,
    @Column("business_id") String businessId,
    @Column("child_id") String childId,
    @Column("id01") String id01,
    @Column("default_indicator") Integer defaultIndicator,
    @Column("address_type_cd") String addressTypeCd,
    @Column("address_type_nm") String addressTypeNm,
    @Column("usage_type_cd") String usageTypeCd,
    @Column("usage_type_nm") String usageTypeNm,
    @Column("address_line1") String addressLine1,
    @Column("address_line2") String addressLine2,
    @Column("address_line3") String addressLine3,
    @Column("city") String city,
    @Column("county") String county,
    @Column("state_cd") String stateCd,
    @Column("state_nm") String stateNm,
    @Column("country_cd") String countryCd,
    @Column("country_nm") String countryNm,
    @Column("postal_code") String postalCode,
    @Column("postal_code_extension") String postalCodeExtension,
    @Column("coordinate_system") String coordinateSystem,
    @Column("location_coordinate_desc") String locationCoordinateDesc,
    @Column("address_status_cd") String addressStatusCd,
    @Column("address_status_nm") String addressStatusNm,
    @Column("latitude") String latitude,
    @Column("longitude") String longitude,
    @Column("effective_start_date") LocalDateTime effectiveStartDate,
    @Column("effective_end_date") LocalDateTime effectiveEndDate,
    @Column("enriched_indicator_cd") String enrichedIndicatorCd,
    @Column("enriched_indicator_nm") String enrichedIndicatorNm,
    @Column("status_code") String statusCode,
    @Column("status_message") String statusMessage,
    @Column("elevation") String elevation,
    @Column("is_deleted") Integer isDeleted,
    @Column("row_md5") String rowMd5,
    @Column("valid_from") LocalDateTime validFrom,
    @Column("valid_to") LocalDateTime validTo,
    @Column("is_current") Integer isCurrent,
    @ReadOnlyProperty @Column("load_dtm") LocalDateTime loadDtm) {

  public static final String TABLE = "EVT_ZLV_GUEST_MASTER_ADDR_HIST";

  /** The same row, closed: valid_to set, is_current = 0. Saved back as an UPDATE. */
  public GuestMasterAddrHist closed(LocalDateTime closedAt) {
    return new GuestMasterAddrHist(
        histId,
        businessId,
        childId,
        id01,
        defaultIndicator,
        addressTypeCd,
        addressTypeNm,
        usageTypeCd,
        usageTypeNm,
        addressLine1,
        addressLine2,
        addressLine3,
        city,
        county,
        stateCd,
        stateNm,
        countryCd,
        countryNm,
        postalCode,
        postalCodeExtension,
        coordinateSystem,
        locationCoordinateDesc,
        addressStatusCd,
        addressStatusNm,
        latitude,
        longitude,
        effectiveStartDate,
        effectiveEndDate,
        enrichedIndicatorCd,
        enrichedIndicatorNm,
        statusCode,
        statusMessage,
        elevation,
        isDeleted,
        rowMd5,
        validFrom,
        closedAt,
        HistFlags.NO,
        loadDtm);
  }
}
