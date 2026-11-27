package com.visakha.txmdmext.history;

import java.time.Instant;

/** Root (single-valued) zlv_guest fields from the payload. */
public record GuestRoot(
    String firstName,
    String clnsFirstName,
    String lastName,
    String clnsLastName,
    String ffrFirstName,
    String ffrLastName,
    Boolean doNotContactInd,
    String linkedinUrl,
    Instant lastTouchDtm,
    Instant prevLastTouchDtm,
    Double touchIncr,
    String touchNotes,
    Lookup touchCd) {}
