package com.visakha.txmdmext.event;

import java.util.List;
import java.util.stream.StreamSupport;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Plain Java, no Spring. Turns the PAYLOAD JSON of one event-store row into a BusinessEvent.
 * Missing field = null.
 */
public class EventParser {

  private final ObjectMapper mapper;

  public EventParser() {
    this.mapper = JsonMapper.builder().build();
  }

  public JsonNode readTree(String json) {
    return mapper.readTree(json);
  }

  public BusinessEvent parse(StoredEvent row) {
    JsonNode root = mapper.readTree(row.payload());
    String eventTypeName = text(root, "eventType");
    return new BusinessEvent(
        row.id01(),
        row.evtDtm(),
        eventTypeName,
        EventType.from(eventTypeName),
        ImpliedOperation.from(text(root, "impliedOperation")),
        text(root, "businessId"),
        text(root, "activeBusinessId"),
        text(root, "assetId"),
        number(root, "dateOfChange"),
        text(root, "correlationId"),
        text(root, "userName"),
        text(root, "sourceSystem"),
        states(root.get("states")),
        elements(root, "changedFields").map(JsonNode::asString).toList(),
        elements(root, "mergedBusinessIds").map(JsonNode::asString).toList(),
        elements(root, "members").map(EventParser::member).toList(),
        elements(root, "affectedXrefs").map(EventParser::xref).toList(),
        elements(root, "dataDiff").map(EventParser::fieldChange).toList(),
        root.get("payload"));
  }

  public List<BusinessEvent> parseAll(List<StoredEvent> rows) {
    return rows.stream().map(this::parse).toList();
  }

  private static Member member(JsonNode n) {
    return new Member(text(n, "sourceSystemId"), text(n, "sourcePKey"), text(n, "businessId"));
  }

  private static XrefInsight xref(JsonNode n) {
    return new XrefInsight(
        text(n, "sourceSystemId"),
        text(n, "sourcePKey"),
        text(n, "businessId"),
        XrefType.from(text(n, "xrefType")),
        XrefChange.from(text(n, "xrefChange")),
        MembershipChange.from(text(n, "membershipChange")));
  }

  private static FieldChange fieldChange(JsonNode n) {
    return new FieldChange(
        text(n, "fieldPath"),
        ChangeType.from(text(n, "changeType")),
        valueText(n.get("oldValue")),
        valueText(n.get("newValue")));
  }

  private static States states(JsonNode n) {
    if (n == null || !n.isObject()) {
      return States.EMPTY;
    }
    return new States(text(n, "base"), text(n, "validation"), text(n, "createMode"));
  }

  /** A JSON value as text: strings as-is, numbers and booleans as written, objects as JSON. */
  public static String valueText(JsonNode n) {
    if (n == null || n.isNull() || n.isMissingNode()) {
      return null;
    }
    return n.isString() ? n.asString() : n.toString();
  }

  public static String text(JsonNode parent, String field) {
    return valueText(parent.get(field));
  }

  public static Long number(JsonNode parent, String field) {
    JsonNode n = parent.get(field);
    return n != null && n.isNumber() ? n.longValue() : null;
  }

  private static java.util.stream.Stream<JsonNode> elements(JsonNode parent, String field) {
    JsonNode n = parent.get(field);
    if (n == null || !n.isArray()) {
      return java.util.stream.Stream.empty();
    }
    return StreamSupport.stream(n.spliterator(), false);
  }
}
