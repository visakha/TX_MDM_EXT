package com.visakha.txmdmext.history;

import java.time.Instant;

/** One field change log row (append only), from one dataDiff entry. */
public record FieldChangeRow(
    String id01,
    int seqNo,
    String businessId,
    String fieldPath,
    String fieldGroup,
    String childId,
    String fieldName,
    String changeType,
    String oldValue,
    String newValue,
    Long dateOfChange,
    Instant changeTime) {}
