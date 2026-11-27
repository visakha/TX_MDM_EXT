package com.visakha.txmdmext.model.zlvguest.store;

import com.visakha.txmdmext.history.ChildVersion;
import com.visakha.txmdmext.history.Lookup;
import com.visakha.txmdmext.history.OpenChild;
import com.visakha.txmdmext.model.ChildGroup;
import com.visakha.txmdmext.model.zlvguest.FieldGroup;
import com.visakha.txmdmext.model.zlvguest.GuestChild;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterAddrHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterAltIdHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterPhoneHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterSrcAddrHist;
import com.visakha.txmdmext.model.zlvguest.entity.HistFlags;
import com.visakha.txmdmext.model.zlvguest.repository.GuestMasterAddrHistRepository;
import com.visakha.txmdmext.model.zlvguest.repository.GuestMasterAltIdHistRepository;
import com.visakha.txmdmext.model.zlvguest.repository.GuestMasterPhoneHistRepository;
import com.visakha.txmdmext.model.zlvguest.repository.GuestMasterSrcAddrHistRepository;
import com.visakha.txmdmext.store.ChildStore;
import com.visakha.txmdmext.store.DbTime;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * The four zlv_guest field group history tables (Type 2), one repository each: close the open row,
 * then append. Entity rows are turned into GuestChild records for the planner and back.
 */
@Component
public class GuestChildStore implements ChildStore {

  private static final Lookup NO_LOOKUP = new Lookup(null, null);

  private final GuestMasterPhoneHistRepository phones;
  private final GuestMasterSrcAddrHistRepository srcAddresses;
  private final GuestMasterAddrHistRepository addresses;
  private final GuestMasterAltIdHistRepository altIds;
  private final DbTime time;

  public GuestChildStore(
      GuestMasterPhoneHistRepository phones,
      GuestMasterSrcAddrHistRepository srcAddresses,
      GuestMasterAddrHistRepository addresses,
      GuestMasterAltIdHistRepository altIds,
      DbTime time) {
    this.phones = phones;
    this.srcAddresses = srcAddresses;
    this.addresses = addresses;
    this.altIds = altIds;
    this.time = time;
  }

  @Override
  public Map<String, OpenChild> openRows(ChildGroup group, String businessId) {
    int yes = HistFlags.YES;
    int no = HistFlags.NO;
    List<OpenChild> rows =
        switch ((FieldGroup) group) {
          case X_PHONE ->
              phones.findByBusinessIdAndIsCurrentAndIsDeleted(businessId, yes, no).stream()
                  .map(e -> new OpenChild(phone(e), trim(e.rowMd5())))
                  .toList();
          case X_SRC_ADDRESS ->
              srcAddresses.findByBusinessIdAndIsCurrentAndIsDeleted(businessId, yes, no).stream()
                  .map(e -> new OpenChild(srcAddress(e), trim(e.rowMd5())))
                  .toList();
          case ADDRESS ->
              addresses.findByBusinessIdAndIsCurrentAndIsDeleted(businessId, yes, no).stream()
                  .map(e -> new OpenChild(address(e), trim(e.rowMd5())))
                  .toList();
          case X_ALT_ID ->
              altIds.findByBusinessIdAndIsCurrentAndIsDeleted(businessId, yes, no).stream()
                  .map(e -> new OpenChild(altId(e), trim(e.rowMd5())))
                  .toList();
        };
    Map<String, OpenChild> byId = new LinkedHashMap<>();
    rows.forEach(o -> byId.put(o.row().childId(), o));
    return byId;
  }

  @Override
  public void closeCurrent(
      ChildGroup group, String businessId, String childId, LocalDateTime validTo) {
    int yes = HistFlags.YES;
    switch ((FieldGroup) group) {
      case X_PHONE ->
          phones.saveAll(
              phones.findByBusinessIdAndChildIdAndIsCurrent(businessId, childId, yes).stream()
                  .map(e -> e.closed(validTo))
                  .toList());
      case X_SRC_ADDRESS ->
          srcAddresses.saveAll(
              srcAddresses.findByBusinessIdAndChildIdAndIsCurrent(businessId, childId, yes).stream()
                  .map(e -> e.closed(validTo))
                  .toList());
      case ADDRESS ->
          addresses.saveAll(
              addresses.findByBusinessIdAndChildIdAndIsCurrent(businessId, childId, yes).stream()
                  .map(e -> e.closed(validTo))
                  .toList());
      case X_ALT_ID ->
          altIds.saveAll(
              altIds.findByBusinessIdAndChildIdAndIsCurrent(businessId, childId, yes).stream()
                  .map(e -> e.closed(validTo))
                  .toList());
    }
  }

  @Override
  public void insert(
      String businessId, String id01, ChildVersion version, LocalDateTime validFrom) {
    int deleted = HistFlags.of(version.deleted());
    String md5 = version.rowMd5();
    switch ((GuestChild) version.row()) {
      case GuestChild.Phone p ->
          phones.save(
              new GuestMasterPhoneHist(
                  null,
                  businessId,
                  p.childId(),
                  id01,
                  p.cntryCd(),
                  p.phoneNum(),
                  code(p.phTyp()),
                  name(p.phTyp()),
                  HistFlags.of(p.noMrktCalls()),
                  deleted,
                  md5,
                  validFrom,
                  null,
                  HistFlags.YES,
                  null));
      case GuestChild.SrcAddress a ->
          srcAddresses.save(
              new GuestMasterSrcAddrHist(
                  null,
                  businessId,
                  a.childId(),
                  id01,
                  a.addressLine1(),
                  a.addressLine2(),
                  a.addressLine3(),
                  a.subBldgNm(),
                  a.bldgName(),
                  a.nearestAirport(),
                  a.bldgMgmntCompany(),
                  a.city(),
                  code(a.stateCd()),
                  name(a.stateCd()),
                  code(a.addressStatusCd()),
                  name(a.addressStatusCd()),
                  code(a.addressTyp()),
                  name(a.addressTyp()),
                  a.postalCd(),
                  code(a.countryCd()),
                  name(a.countryCd()),
                  deleted,
                  md5,
                  validFrom,
                  null,
                  HistFlags.YES,
                  null));
      case GuestChild.Address a ->
          addresses.save(
              new GuestMasterAddrHist(
                  null,
                  businessId,
                  a.childId(),
                  id01,
                  HistFlags.of(a.defaultIndicator()),
                  code(a.addressType()),
                  name(a.addressType()),
                  code(a.usageType()),
                  name(a.usageType()),
                  a.addressLine1(),
                  a.addressLine2(),
                  a.addressLine3(),
                  a.city(),
                  a.county(),
                  code(a.state()),
                  name(a.state()),
                  code(a.country()),
                  name(a.country()),
                  a.postalCode(),
                  a.postalCodeExtension(),
                  a.coordinateSystem(),
                  a.locationCoordinateDesc(),
                  code(a.addressStatus()),
                  name(a.addressStatus()),
                  a.latitude(),
                  a.longitude(),
                  time.of(a.effectiveStartDate()),
                  time.of(a.effectiveEndDate()),
                  code(a.enrichedIndicator()),
                  name(a.enrichedIndicator()),
                  a.statusCode(),
                  a.statusMessage(),
                  a.elevation(),
                  deleted,
                  md5,
                  validFrom,
                  null,
                  HistFlags.YES,
                  null));
      case GuestChild.AltId a ->
          altIds.save(
              new GuestMasterAltIdHist(
                  null,
                  businessId,
                  a.childId(),
                  id01,
                  a.altIdVal(),
                  code(a.altIdTyp()),
                  name(a.altIdTyp()),
                  HistFlags.of(a.activeInd()),
                  a.startDate(),
                  code(a.priSegmentTyp()),
                  name(a.priSegmentTyp()),
                  code(a.secSegmentTyp()),
                  name(a.secSegmentTyp()),
                  a.userNotes(),
                  deleted,
                  md5,
                  validFrom,
                  null,
                  HistFlags.YES,
                  null));
    }
  }

  // --- entity row -> GuestChild record, as stored ---

  private static GuestChild phone(GuestMasterPhoneHist e) {
    return new GuestChild.Phone(
        e.childId(),
        e.cntryCd(),
        e.phoneNum(),
        lookup(e.phTypCd(), e.phTypNm()),
        HistFlags.bool(e.noMrktCalls()));
  }

  private static GuestChild srcAddress(GuestMasterSrcAddrHist e) {
    return new GuestChild.SrcAddress(
        e.childId(),
        e.addressLine1(),
        e.addressLine2(),
        e.addressLine3(),
        e.subBldgNm(),
        e.bldgName(),
        e.nearestAirport(),
        e.bldgMgmntCompany(),
        e.city(),
        lookup(e.stateCdCd(), e.stateCdNm()),
        lookup(e.addressStatusCdCd(), e.addressStatusCdNm()),
        lookup(e.addressTypCd(), e.addressTypNm()),
        e.postalCd(),
        lookup(e.countryCdCd(), e.countryCdNm()));
  }

  private GuestChild address(GuestMasterAddrHist e) {
    return new GuestChild.Address(
        e.childId(),
        HistFlags.bool(e.defaultIndicator()),
        lookup(e.addressTypeCd(), e.addressTypeNm()),
        lookup(e.usageTypeCd(), e.usageTypeNm()),
        e.addressLine1(),
        e.addressLine2(),
        e.addressLine3(),
        e.city(),
        e.county(),
        lookup(e.stateCd(), e.stateNm()),
        lookup(e.countryCd(), e.countryNm()),
        e.postalCode(),
        e.postalCodeExtension(),
        e.coordinateSystem(),
        e.locationCoordinateDesc(),
        lookup(e.addressStatusCd(), e.addressStatusNm()),
        e.latitude(),
        e.longitude(),
        time.instant(e.effectiveStartDate()),
        time.instant(e.effectiveEndDate()),
        lookup(e.enrichedIndicatorCd(), e.enrichedIndicatorNm()),
        e.statusCode(),
        e.statusMessage(),
        e.elevation());
  }

  private static GuestChild altId(GuestMasterAltIdHist e) {
    return new GuestChild.AltId(
        e.childId(),
        e.altIdVal(),
        lookup(e.altIdTypCd(), e.altIdTypNm()),
        HistFlags.bool(e.activeInd()),
        e.startDate(),
        lookup(e.priSegmentTypCd(), e.priSegmentTypNm()),
        lookup(e.secSegmentTypCd(), e.secSegmentTypNm()),
        e.userNotes());
  }

  private static Lookup lookup(String code, String name) {
    return code == null && name == null ? null : new Lookup(code, name);
  }

  private static String trim(String s) {
    return s == null ? null : s.trim();
  }

  private static String code(Lookup l) {
    return (l == null ? NO_LOOKUP : l).code();
  }

  private static String name(Lookup l) {
    return (l == null ? NO_LOOKUP : l).name();
  }
}
