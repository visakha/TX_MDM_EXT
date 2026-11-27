package com.visakha.txmdmext.history;

import com.visakha.txmdmext.event.EventParser;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import tools.jackson.databind.JsonNode;

/**
 * Typed reads of the sparse event payload, shared by all models. A missing field is null. Also
 * reads _meta, which every B360 master carries.
 */
public final class PayloadReader {

  private PayloadReader() {}

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

  public static String text(JsonNode n, String field) {
    return EventParser.text(n, field);
  }

  public static Boolean bool(JsonNode n, String field) {
    JsonNode v = n.get(field);
    return v != null && v.isBoolean() ? v.booleanValue() : null;
  }

  public static Long whole(JsonNode n, String field) {
    JsonNode v = n.get(field);
    return v != null && v.isNumber() ? v.longValue() : null;
  }

  public static Double decimal(JsonNode n, String field) {
    JsonNode v = n.get(field);
    return v != null && v.isNumber() ? v.doubleValue() : null;
  }

  public static Lookup lookup(JsonNode n, String field) {
    JsonNode v = n.get(field);
    if (v == null || !v.isObject()) {
      return null;
    }
    return new Lookup(text(v, "Code"), text(v, "Name"));
  }

  /** DateTime fields came as epoch ms in real events; ISO text is accepted too. */
  public static Instant instant(JsonNode n, String field) {
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

  public static LocalDate date(JsonNode n, String field) {
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
