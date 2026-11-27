package com.visakha.txmdmext.history;

/** A membership to open in EVT_MEMBER_HIST, with its row_md5. */
public record MemberVersion(MemberKey key, String rowMd5) {}
