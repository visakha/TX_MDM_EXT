package com.visakha.txmdmext.history;

/** A source record, keyed the only safe way: source system + source primary key. */
public record MemberKey(String sourceSystem, String sourcePkey) {}
