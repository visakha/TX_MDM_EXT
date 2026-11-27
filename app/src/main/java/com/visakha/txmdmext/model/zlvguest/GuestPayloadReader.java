package com.visakha.txmdmext.model.zlvguest;

import static com.visakha.txmdmext.history.PayloadReader.bool;
import static com.visakha.txmdmext.history.PayloadReader.date;
import static com.visakha.txmdmext.history.PayloadReader.decimal;
import static com.visakha.txmdmext.history.PayloadReader.instant;
import static com.visakha.txmdmext.history.PayloadReader.lookup;
import static com.visakha.txmdmext.history.PayloadReader.text;
import static com.visakha.txmdmext.history.PayloadReader.whole;

import com.visakha.txmdmext.model.ChildRow;
import java.util.List;
import java.util.stream.StreamSupport;
import tools.jackson.databind.JsonNode;

/** Reads the zlv_guest root fields and field-group rows of a payload. A missing field is null. */
final class GuestPayloadReader {

  private GuestPayloadReader() {}

  static GuestRoot root(JsonNode p) {
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

  /** All child rows of one group. A group missing from the payload means no rows. */
  static List<ChildRow> children(JsonNode p, FieldGroup group) {
    JsonNode arr = p.get(group.jsonName());
    if (arr == null || !arr.isArray()) {
      return List.of();
    }
    return StreamSupport.stream(arr.spliterator(), false)
        .<ChildRow>map(row -> child(row, group))
        .toList();
  }

  private static GuestChild child(JsonNode r, FieldGroup group) {
    String id = text(r, "_id");
    return switch (group) {
      case X_PHONE ->
          new GuestChild.Phone(
              id,
              whole(r, "cntry_cd"),
              whole(r, "ph_num"),
              lookup(r, "ph_typ"),
              bool(r, "no_mrkt_calls"));
      case X_SRC_ADDRESS ->
          new GuestChild.SrcAddress(
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
          new GuestChild.Address(
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
          new GuestChild.AltId(
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
}
