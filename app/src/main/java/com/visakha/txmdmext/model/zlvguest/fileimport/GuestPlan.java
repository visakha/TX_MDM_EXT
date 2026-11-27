package com.visakha.txmdmext.model.zlvguest.fileimport;

import java.util.List;

/** One generated guest as it should look in MDM after a round. Lists are in key order. */
public record GuestPlan(
    String key,
    String firstName,
    String lastName,
    String touchNotes,
    List<Phone> phones,
    List<Address> addresses,
    List<AltId> altIds) {

  /** One x_phone entry. Key like RG-42-1-PH-1. */
  public record Phone(String key, String type, int countryCode, int number) {}

  /**
   * One address, written to BOTH Address (key ...-AD-n) and x_src_address (key ...-SA-n) with the
   * same values, like the verified round files.
   */
  public record Address(int seq, String line1, String city, String state, String zip, String type) {

    public String addressKey(String root) {
      return root + "-AD-" + seq;
    }

    public String srcAddressKey(String root) {
      return root + "-SA-" + seq;
    }
  }

  /** One x_alt_id entry. */
  public record AltId(String key, String type, String value) {}
}
