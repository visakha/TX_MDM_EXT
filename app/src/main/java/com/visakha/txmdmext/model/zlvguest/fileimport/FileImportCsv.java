package com.visakha.txmdmext.model.zlvguest.fileimport;

import com.visakha.txmdmext.model.zlvguest.fileimport.GuestPlan.Address;
import com.visakha.txmdmext.model.zlvguest.fileimport.GuestPlan.AltId;
import com.visakha.txmdmext.model.zlvguest.fileimport.GuestPlan.Phone;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Writes B360 File Import CSV text. One 32-column header for every round (the round 3 layout: root
 * Operation Type first), so one saved wizard mapping fits all files. ASCII, no quotes, CRLF.
 */
public final class FileImportCsv {

  public static final String CRLF = "\r\n";

  public static final List<String> HEADER =
      List.of(
          "Operation Type",
          "Source Primary Key",
          "Source Last Update Date",
          "first_name",
          "last_name",
          "touch_notes",
          "x_phone.Operation Type",
          "x_phone.Source Primary Key",
          "x_phone.ph_typ",
          "x_phone.cntry_cd",
          "x_phone.phone_num",
          "Address.Operation Type",
          "Address.Source Primary Key",
          "Address.addressType",
          "Address.addressLine1",
          "Address.city",
          "Address.state",
          "Address.postalCode",
          "Address.country",
          "x_src_address.Operation Type",
          "x_src_address.Source Primary Key",
          "x_src_address.address_line_1",
          "x_src_address.city",
          "x_src_address.state_cd",
          "x_src_address.postal_cd",
          "x_src_address.country_cd",
          "x_src_address.address_typ",
          "x_src_address.address_status_cd",
          "x_alt_id.Operation Type",
          "x_alt_id.Source Primary Key",
          "x_alt_id.alt_id_typ",
          "x_alt_id.alt_id_val");

  /** What a round-3 row deletes. Address and x_src_address are separate groups. */
  public enum DeleteGroup {
    PHONE,
    ADDRESS,
    SRC_ADDRESS,
    ALT_ID
  }

  /** One delete row: the guest and the child key to remove. */
  public record DeleteRow(GuestPlan guest, DeleteGroup group, String childKey) {}

  private FileImportCsv() {}

  /**
   * Full state of each guest (rounds 1 and 2): cross product of phones x addresses x alt ids, empty
   * groups as blank columns. Root and group Operation Type stay blank, so the wizard choice
   * applies, as in the verified rounds 1 and 2.
   */
  public static String fullState(List<GuestPlan> guests, Map<String, String> dates) {
    List<String> lines = new ArrayList<>();
    lines.add(String.join(",", HEADER));
    for (GuestPlan g : guests) {
      for (Phone p : orNull(g.phones())) {
        for (Address a : orNull(g.addresses())) {
          for (AltId i : orNull(g.altIds())) {
            Map<String, String> row = root(g, dates.get(g.key()), "");
            if (p != null) {
              phone(row, p);
            }
            if (a != null) {
              address(row, g.key(), a);
            }
            if (i != null) {
              altId(row, i);
            }
            lines.add(line(row));
          }
        }
      }
    }
    return String.join(CRLF, lines) + CRLF;
  }

  /**
   * Round 3: one row per deletion. Root Operation Type = Patch with the guest's current root values
   * (so nothing is cleared), the group's Operation Type = Delete plus that child's key.
   */
  public static String deletes(List<DeleteRow> rows, Map<String, String> dates) {
    Stream<String> body =
        rows.stream()
            .map(
                r -> {
                  Map<String, String> row = root(r.guest(), dates.get(r.guest().key()), "Patch");
                  String group =
                      switch (r.group()) {
                        case PHONE -> "x_phone";
                        case ADDRESS -> "Address";
                        case SRC_ADDRESS -> "x_src_address";
                        case ALT_ID -> "x_alt_id";
                      };
                  row.put(group + ".Operation Type", "Delete");
                  row.put(group + ".Source Primary Key", r.childKey());
                  return line(row);
                });
    return Stream.concat(Stream.of(String.join(",", HEADER)), body)
            .collect(Collectors.joining(CRLF))
        + CRLF;
  }

  private static <T> List<T> orNull(List<T> list) {
    List<T> out = new ArrayList<>(list);
    if (out.isEmpty()) {
      out.add(null);
    }
    return out;
  }

  private static Map<String, String> root(GuestPlan g, String date, String operation) {
    Map<String, String> row = new LinkedHashMap<>();
    HEADER.forEach(c -> row.put(c, ""));
    row.put("Operation Type", operation);
    row.put("Source Primary Key", g.key());
    row.put("Source Last Update Date", date);
    row.put("first_name", g.firstName());
    row.put("last_name", g.lastName());
    row.put("touch_notes", g.touchNotes());
    return row;
  }

  private static void phone(Map<String, String> row, Phone p) {
    row.put("x_phone.Source Primary Key", p.key());
    row.put("x_phone.ph_typ", p.type());
    row.put("x_phone.cntry_cd", Integer.toString(p.countryCode()));
    row.put("x_phone.phone_num", Integer.toString(p.number()));
  }

  private static void address(Map<String, String> row, String root, Address a) {
    row.put("Address.Source Primary Key", a.addressKey(root));
    row.put("Address.addressType", a.type());
    row.put("Address.addressLine1", a.line1());
    row.put("Address.city", a.city());
    row.put("Address.state", a.state());
    row.put("Address.postalCode", a.zip());
    row.put("Address.country", Lookups.COUNTRY);
    row.put("x_src_address.Source Primary Key", a.srcAddressKey(root));
    row.put("x_src_address.address_line_1", a.line1());
    row.put("x_src_address.city", a.city());
    row.put("x_src_address.state_cd", a.state());
    row.put("x_src_address.postal_cd", a.zip());
    row.put("x_src_address.country_cd", Lookups.COUNTRY);
    row.put("x_src_address.address_typ", a.type());
    row.put("x_src_address.address_status_cd", Lookups.ADDRESS_STATUS);
  }

  private static void altId(Map<String, String> row, AltId i) {
    row.put("x_alt_id.Source Primary Key", i.key());
    row.put("x_alt_id.alt_id_typ", i.type());
    row.put("x_alt_id.alt_id_val", i.value());
  }

  /** Joins one row; refuses anything that would need quoting or is not ASCII. */
  static String line(Map<String, String> row) {
    row.values().forEach(FileImportCsv::checkValue);
    return String.join(",", row.values());
  }

  static void checkValue(String v) {
    boolean bad = v.chars().anyMatch(c -> c > 126 || c < 32) || v.contains(",") || v.contains("\"");
    if (bad) {
      throw new IllegalArgumentException("value not allowed in the import CSV: " + v);
    }
  }
}
