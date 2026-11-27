package com.visakha.txmdmext.event;

/**
 * One entry of affectedXrefs[]. Its businessId is the source record's own id, which can differ from
 * the master (UC26), so history keys on sourceSystemId + sourcePKey only.
 */
public record XrefInsight(
    String sourceSystemId,
    String sourcePKey,
    String businessId,
    XrefType xrefType,
    XrefChange xrefChange,
    MembershipChange membershipChange) {}
