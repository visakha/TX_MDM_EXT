package com.visakha.txmdmext.event;

/** One dataDiff entry. Old and new values are kept as text (the schema types them as Object). */
public record FieldChange(
    String fieldPath, ChangeType changeType, String oldValue, String newValue) {}
