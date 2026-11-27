package com.visakha.txmdmext.history;

import java.time.Instant;

/** One new EVT_MASTER_HIST row. The previous current row for businessId gets closed. */
public record MasterVersion(
    String businessId,
    String activeBusinessId,
    String id01,
    String eventType,
    String impliedOperation,
    String correlationId,
    Long dateOfChange,
    String userName,
    String eventSourceSystem,
    String mergedBusinessIds,
    String stateBase,
    String stateValidation,
    String stateCreateMode,
    MetaInfo meta,
    GuestRoot root,
    boolean deleted,
    String payloadJson,
    Instant validFrom) {}
