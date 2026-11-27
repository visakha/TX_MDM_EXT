package com.visakha.txmdmext.history;

/** A lookup value as it arrives in events: {Code, Name}. Stored as _cd / _nm columns. */
public record Lookup(String code, String name) {}
