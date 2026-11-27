package com.visakha.txmdmext.history;

/** A membership to open in the membership history table, with its row_md5. */
public record MemberVersion(MemberKey key, String rowMd5) {}
