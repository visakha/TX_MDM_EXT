package com.visakha.txmdmext.model.zlvguest.entity;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/** One row of EVT_ZLV_GUEST_MASTER_SRC_ADDR_HIST (ddl/V002): the x_src_address group, Type 2. */
@Table(GuestMasterSrcAddrHist.TABLE)
public record GuestMasterSrcAddrHist(
    @Id @Column("hist_id") Long histId,
    @Column("business_id") String businessId,
    @Column("child_id") String childId,
    @Column("id01") String id01,
    @Column("address_line_1") String addressLine1,
    @Column("address_line_2") String addressLine2,
    @Column("address_line_3") String addressLine3,
    @Column("sub_bldg_nm") String subBldgNm,
    @Column("bldg_name") String bldgName,
    @Column("nearest_airport") String nearestAirport,
    @Column("bldg_mgmnt_company") String bldgMgmntCompany,
    @Column("city") String city,
    @Column("state_cd_cd") String stateCdCd,
    @Column("state_cd_nm") String stateCdNm,
    @Column("address_status_cd_cd") String addressStatusCdCd,
    @Column("address_status_cd_nm") String addressStatusCdNm,
    @Column("address_typ_cd") String addressTypCd,
    @Column("address_typ_nm") String addressTypNm,
    @Column("postal_cd") Long postalCd,
    @Column("country_cd_cd") String countryCdCd,
    @Column("country_cd_nm") String countryCdNm,
    @Column("is_deleted") Integer isDeleted,
    @Column("row_md5") String rowMd5,
    @Column("valid_from") LocalDateTime validFrom,
    @Column("valid_to") LocalDateTime validTo,
    @Column("is_current") Integer isCurrent,
    @ReadOnlyProperty @Column("load_dtm") LocalDateTime loadDtm) {

  public static final String TABLE = "EVT_ZLV_GUEST_MASTER_SRC_ADDR_HIST";

  /** The same row, closed: valid_to set, is_current = 0. Saved back as an UPDATE. */
  public GuestMasterSrcAddrHist closed(LocalDateTime closedAt) {
    return new GuestMasterSrcAddrHist(
        histId,
        businessId,
        childId,
        id01,
        addressLine1,
        addressLine2,
        addressLine3,
        subBldgNm,
        bldgName,
        nearestAirport,
        bldgMgmntCompany,
        city,
        stateCdCd,
        stateCdNm,
        addressStatusCdCd,
        addressStatusCdNm,
        addressTypCd,
        addressTypNm,
        postalCd,
        countryCdCd,
        countryCdNm,
        isDeleted,
        rowMd5,
        validFrom,
        closedAt,
        HistFlags.NO,
        loadDtm);
  }
}
