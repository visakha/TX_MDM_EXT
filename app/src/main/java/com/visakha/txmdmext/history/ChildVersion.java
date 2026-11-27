package com.visakha.txmdmext.history;

/**
 * A new version of one child row. deleted = the row left the payload: it is closed and gets a final
 * version with is_deleted = 1 (rows are never removed).
 */
public record ChildVersion(ChildRow row, boolean deleted, String rowMd5) {}
