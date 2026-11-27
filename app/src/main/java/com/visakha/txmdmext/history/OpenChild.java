package com.visakha.txmdmext.history;

/** A current (is_current = 1, is_deleted = 0) child row as stored, with its stored row_md5. */
public record OpenChild(ChildRow row, String rowMd5) {}
