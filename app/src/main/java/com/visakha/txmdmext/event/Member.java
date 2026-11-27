package com.visakha.txmdmext.event;

/** One entry of members[]: a source record that belongs to the master. */
public record Member(String sourceSystemId, String sourcePKey, String businessId) {}
