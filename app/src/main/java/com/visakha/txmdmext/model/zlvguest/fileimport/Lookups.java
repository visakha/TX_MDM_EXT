package com.visakha.txmdmext.model.zlvguest.fileimport;

import java.util.List;

/**
 * The lookup codes the generator may use. Every code is in internal-docs/LOOKUPS.md (read live from
 * Reference 360, 2026-11-27); codes are case sensitive. A test checks them against that file.
 */
public final class Lookups {

  public static final List<String> PHONE_TYPES =
      List.of("Mobile", "Fax", "LandLine", "Cell", "VoIP", "Telephone");
  public static final List<String> ADDRESS_TYPES = List.of("MA", "PA", "CR", "AM1", "AM2");
  public static final List<String> ALT_ID_TYPES = List.of("DL", "Passport", "TIN", "EMP ID", "SSN");
  public static final String ADDRESS_STATUS = "Active";
  public static final String STATE = "TX";
  public static final String COUNTRY = "US";

  /** City and its zips. Only cities with two or more zips, so a zip edit keeps the city. */
  public record City(String name, List<String> zips) {}

  public static final List<City> CITIES =
      List.of(
          new City("Austin", List.of("78701", "78702", "78703", "78704", "78705", "78731")),
          new City("Round Rock", List.of("78664", "78665", "78681")),
          new City("Georgetown", List.of("78626", "78628", "78633")),
          new City("Leander", List.of("78641", "78645")),
          new City("Pflugerville", List.of("78660", "78691")));

  public static final List<String> STREETS =
      List.of("Test Ln", "Sample Rd", "Demo St", "Trial Ave", "Check Dr");
  public static final List<String> FIRST_NAMES =
      List.of("Ana", "Ben", "Cara", "Dev", "Ela", "Finn", "Gia", "Hal", "Ivy", "Jon");
  public static final List<String> LAST_NAMES =
      List.of("Rgalpha", "Rgbeta", "Rggamma", "Rgdelta", "Rgomega", "Rgsigma", "Rgtheta");

  private Lookups() {}
}
