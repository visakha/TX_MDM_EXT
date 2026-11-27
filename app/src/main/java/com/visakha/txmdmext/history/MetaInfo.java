package com.visakha.txmdmext.history;

import java.time.Instant;

/** payload._meta. Not present in the events seen so far, so usually all null. */
public record MetaInfo(
    String status,
    String createdBy,
    Instant creationDate,
    String updatedBy,
    Instant lastUpdateDate) {}
