package com.visakha.txmdmext.history;

import com.visakha.txmdmext.event.EventParser;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.StreamSupport;
import tools.jackson.databind.JsonNode;

/** Reads the sparse master payload into typed values. A missing field is null. */
public final class PayloadReader {

  private PayloadReader() {}

  public static GuestRoot root(JsonNode p) {
    return new GuestRoot(
        text(p, "first_name"),
        text(p, "clns_first_name"),
        text(p, "last_name"),
        text(p, "clns_last_name"),
        text(p, "ffr_first_name"),
        text(p, "ffr_last_name"),
        bool(p, "do_not_contact_ind"),
        text(p, "linkedin_url"),
        instant(p, "last_touch_dtm"),
        instant(p, "prev_last_touch_dtm"),
        decimal(p, "touch_incr"),
        text(p, "touch_notes"),
        lookup(p, "touch_cd"));
  }

  public static MetaInfo meta(JsonNode p) {
    JsonNode m = p.get("_meta");
    if (m == null || !m.isObject()) {
      return new MetaInfo(null, null, null, null, null);
    }
    return new MetaInfo(
        text(m, "status"),
        text(m, "createdBy"),
        instant(m, "creationDate"),
        text(m, "updatedBy"),
        instant(m, "lastUpdateDate"));
  }

  /** All child rows of one group. A group missing from the payload means no rows. */
  public static List<ChildRow> children(JsonNode p, FieldGroup group) {
    JsonNode arr = p.get(group.jsonName());
    if (arr == null || !arr.isArray()) {
      return List.of();
    }
    return StreamSupport.stream(arr.spliterator(), false).map(row -> child(row, group)).toList();
  }

  private static ChildRow child(JsonNode r, FieldGroup group) {
    String id = text(r, "_id");
    return switch (group) {
      case X_PHONE ->
          new ChildRow.Phone(
              id,
              whole(r, "cntry_cd"),
              whole(r, "ph_num"),
              lookup(r, "ph_typ"),
              bool(r, "no_mrkt_calls"));
      case X_SRC_ADDRESS ->
          new ChildRow.SrcAddress(
              id,
              text(r, "address_line_1"),
              text(r, "address_line_2"),
              text(r, "address_line_3"),
              text(r, "sub_bldg_nm"),
              text(r, "bldg_name"),
              text(r, "nearest_airport"),
              text(r, "bldg_mgmnt_company"),
              text(r, "city"),
              lookup(r, "state_cd"),
              lookup(r, "address_status_cd"),
              lookup(r, "address_typ"),
              whole(r, "postal_cd"),
              lookup(r, "country_cd"));
      case ADDRESS ->
          new ChildRow.Address(
              id,
              bool(r, "defaultIndicator"),
              lookup(r, "addressType"),
              lookup(r, "usageType"),
              text(r, "addressLine1"),
              text(r, "addressLine2"),
              text(r, "addressLine3"),
              text(r, "city"),
              text(r, "county"),
              lookup(r, "state"),
              lookup(r, "country"),
              text(r, "postalCode"),
              text(r, "postalCodeExtension"),
              text(r, "coordinateSystem"),
              text(r, "locationCoordinateDesc"),
              lookup(r, "addressStatus"),
              text(r, "latitude"),
              text(r, "longitude"),
              instant(r, "effectiveStartDate"),
              instant(r, "effectiveEndDate"),
              lookup(r, "enrichedIndicator"),
              text(r, "statusCode"),
              text(r, "statusMessage"),
              text(r, "elevation"));
      case X_ALT_ID ->
          new ChildRow.AltId(
              id,
              text(r, "alt_id_val"),
              lookup(r, "alt_id_typ"),
              bool(r, "active_ind"),
              date(r, "start_date"),
              lookup(r, "pri_segment_typ"),
              lookup(r, "sec_segment_typ"),
              text(r, "user_notes"));
    };
  }

  private static String text(JsonNode n, String field) {
    return EventParser.text(n, field);
  }

  private static Boolean bool(JsonNode n, String field) {
    JsonNode v = n.get(field);
    return v != null && v.isBoolean() ? v.booleanValue() : null;
  }

  private static Long whole(JsonNode n, String field) {
    JsonNode v = n.get(field);
    return v != null && v.isNumber() ? v.longValue() : null;
  }

  private static Double decimal(JsonNode n, String field) {
    JsonNode v = n.get(field);
    return v != null && v.isNumber() ? v.doubleValue() : null;
  }

  private static Lookup lookup(JsonNode n, String field) {
    JsonNode v = n.get(field);
    if (v == null || !v.isObject()) {
      return null;
    }
    return new Lookup(text(v, "Code"), text(v, "Name"));
  }

  /** DateTime fields came as epoch ms in real events; ISO text is accepted too. */
  private static Instant instant(JsonNode n, String field) {
    JsonNode v = n.get(field);
    if (v == null || v.isNull()) {
      return null;
    }
    if (v.isNumber()) {
      return Instant.ofEpochMilli(v.longValue());
    }
    try {
      return Instant.parse(v.asString());
    } catch (DateTimeParseException e) {
      return null;
    }
  }

  private static LocalDate date(JsonNode n, String field) {
    JsonNode raw = n.get(field);
    if (raw != null && raw.isNumber()) {
      return LocalDate.ofInstant(Instant.ofEpochMilli(raw.longValue()), java.time.ZoneOffset.UTC);
    }
    String v = text(n, field);
    if (v == null) {
      return null;
    }
    try {
      return LocalDate.parse(v.length() > 10 ? v.substring(0, 10) : v);
    } catch (DateTimeParseException e) {
      return null;
    }
  }
}
