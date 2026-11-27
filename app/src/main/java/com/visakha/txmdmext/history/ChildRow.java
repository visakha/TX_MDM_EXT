package com.visakha.txmdmext.history;

import java.time.Instant;
import java.time.LocalDate;

/**
 * One child row of a many-valued field group, keyed by its _id. Records compare by value, so
 * "changed" is simply !equals.
 */
public sealed interface ChildRow {

  String childId();

  FieldGroup group();

  /** x_phone row. */
  record Phone(String childId, Long cntryCd, Long phNum, Lookup phTyp, Boolean noMrktCalls)
      implements ChildRow {
    @Override
    public FieldGroup group() {
      return FieldGroup.X_PHONE;
    }
  }

  /** x_src_address row (custom group, 13 fields). */
  record SrcAddress(
      String childId,
      String addressLine1,
      String addressLine2,
      String addressLine3,
      String subBldgNm,
      String bldgName,
      String nearestAirport,
      String bldgMgmntCompany,
      String city,
      Lookup stateCd,
      Lookup addressStatusCd,
      Lookup addressTyp,
      Long postalCd,
      Lookup countryCd)
      implements ChildRow {
    @Override
    public FieldGroup group() {
      return FieldGroup.X_SRC_ADDRESS;
    }
  }

  /** OOTB Address row (23 fields). Not seen in real events yet. */
  record Address(
      String childId,
      Boolean defaultIndicator,
      Lookup addressType,
      Lookup usageType,
      String addressLine1,
      String addressLine2,
      String addressLine3,
      String city,
      String county,
      Lookup state,
      Lookup country,
      String postalCode,
      String postalCodeExtension,
      String coordinateSystem,
      String locationCoordinateDesc,
      Lookup addressStatus,
      String latitude,
      String longitude,
      Instant effectiveStartDate,
      Instant effectiveEndDate,
      Lookup enrichedIndicator,
      String statusCode,
      String statusMessage,
      String elevation)
      implements ChildRow {
    @Override
    public FieldGroup group() {
      return FieldGroup.ADDRESS;
    }
  }

  /** x_alt_id row. */
  record AltId(
      String childId,
      String altIdVal,
      Lookup altIdTyp,
      Boolean activeInd,
      LocalDate startDate,
      Lookup priSegmentTyp,
      Lookup secSegmentTyp,
      String userNotes)
      implements ChildRow {
    @Override
    public FieldGroup group() {
      return FieldGroup.X_ALT_ID;
    }
  }
}
