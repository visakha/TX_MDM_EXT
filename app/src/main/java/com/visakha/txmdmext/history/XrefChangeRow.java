package com.visakha.txmdmext.history;

import java.time.Instant;

/** One EVT_XREF_CHANGE row (append only). businessId is the MASTER of the event (UC26). */
public record XrefChangeRow(
    String id01,
    String businessId,
    String sourceSystem,
    String sourcePkey,
    String xrefType,
    String xrefChange,
    String membershipChange,
    String impliedOperation,
    Long dateOfChange,
    Instant changeTime) {}
