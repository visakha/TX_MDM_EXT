package com.visakha.txmdmext.model.zlvguest.fileimport;

import com.visakha.txmdmext.model.zlvguest.fileimport.FileImportCsv.DeleteGroup;
import com.visakha.txmdmext.model.zlvguest.fileimport.FileImportCsv.DeleteRow;
import com.visakha.txmdmext.model.zlvguest.fileimport.GuestPlan.Address;
import com.visakha.txmdmext.model.zlvguest.fileimport.GuestPlan.AltId;
import com.visakha.txmdmext.model.zlvguest.fileimport.GuestPlan.Phone;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Seeded random test data for the B360 File Import, in three rounds on the same guests: round 1
 * creates, round 2 adds and edits in place, round 3 deletes (one entry per row). Same seed and
 * options = byte-identical files. No database, no Spring.
 */
public final class FileImportGenerator {

  /** Date format the import accepts (verified 2026-11-27). */
  public static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /** Generator options. baseDate = day after which rounds 2 and 3 are dated. */
  public record Options(long seed, int guests, int rounds, LocalDate baseDate) {
    public Options {
      if (guests < 1 || guests > 500) {
        throw new IllegalArgumentException("guests must be 1..500");
      }
      if (rounds < 1 || rounds > 3) {
        throw new IllegalArgumentException("rounds must be 1..3");
      }
    }

    public String prefix() {
      return "RG-" + seed;
    }
  }

  /** One round: the import CSV and the expected state of every guest after it. */
  public record Round(int number, String importCsv, String expectedCsv, List<GuestPlan> after) {}

  private final Options options;
  private final Random random;
  private final Set<Integer> usedPhones = new HashSet<>();

  public FileImportGenerator(Options options) {
    this.options = options;
    this.random = new Random(options.seed());
  }

  public List<Round> generate() {
    List<Round> rounds = new ArrayList<>();
    List<GuestPlan> guests = round1Guests();
    rounds.add(
        new Round(1, FileImportCsv.fullState(guests, dates(guests, 1)), expected(guests), guests));
    if (options.rounds() >= 2) {
      guests = guests.stream().map(this::round2).toList();
      rounds.add(
          new Round(
              2, FileImportCsv.fullState(guests, dates(guests, 2)), expected(guests), guests));
    }
    if (options.rounds() >= 3) {
      List<DeleteRow> deletes = new ArrayList<>();
      guests = guests.stream().map(g -> round3(g, deletes)).toList();
      rounds.add(
          new Round(3, FileImportCsv.deletes(deletes, dates(guests, 3)), expected(guests), guests));
    }
    return rounds;
  }

  // ---- round 1: creates -------------------------------------------------------------------

  private List<GuestPlan> round1Guests() {
    List<GuestPlan> out = new ArrayList<>();
    for (int n = 1; n <= options.guests(); n++) {
      String key = options.prefix() + "-" + n;
      List<Phone> phones = new ArrayList<>();
      List<Address> addresses = new ArrayList<>();
      List<AltId> altIds = new ArrayList<>();
      int p = random.nextInt(4);
      int a = random.nextInt(4);
      int i = random.nextInt(4);
      for (int s = 1; s <= p; s++) {
        phones.add(newPhone(key, s));
      }
      for (int s = 1; s <= a; s++) {
        addresses.add(newAddress(s));
      }
      for (int s = 1; s <= i; s++) {
        altIds.add(newAltId(key, s));
      }
      String notes = label(n, 1) + " " + p + "ph " + a + "ad " + i + "ai";
      out.add(
          new GuestPlan(
              key,
              pick(Lookups.FIRST_NAMES),
              pick(Lookups.LAST_NAMES),
              notes,
              phones,
              addresses,
              altIds));
    }
    return out;
  }

  // ---- round 2: adds and in-place edits, no deletes ---------------------------------------

  // Edits first (enum order), so an edit always hits an entry that existed before round 2.
  private enum Change {
    EDIT_PHONE_TYPE,
    EDIT_ZIP,
    ADD_PHONE,
    ADD_ADDRESS,
    ADD_ALT_ID
  }

  private GuestPlan round2(GuestPlan g) {
    List<Change> possible =
        new ArrayList<>(List.of(Change.ADD_PHONE, Change.ADD_ADDRESS, Change.ADD_ALT_ID));
    if (!g.phones().isEmpty()) {
      possible.add(Change.EDIT_PHONE_TYPE);
    }
    if (!g.addresses().isEmpty()) {
      possible.add(Change.EDIT_ZIP);
    }
    // 1..3 distinct changes, in a seeded order.
    List<Change> chosen = new ArrayList<>();
    int count = 1 + random.nextInt(3);
    while (chosen.size() < count) {
      Change c = possible.get(random.nextInt(possible.size()));
      if (!chosen.contains(c)) {
        chosen.add(c);
      }
    }
    chosen.sort(null);
    List<Phone> phones = new ArrayList<>(g.phones());
    List<Address> addresses = new ArrayList<>(g.addresses());
    List<AltId> altIds = new ArrayList<>(g.altIds());
    List<String> notes = new ArrayList<>();
    for (Change c : chosen) {
      switch (c) {
        case ADD_PHONE -> {
          Phone p = newPhone(g.key(), nextSeq(phones.stream().map(Phone::key)));
          phones.add(p);
          notes.add("add " + shortKey(g, p.key()));
        }
        case ADD_ADDRESS -> {
          Address a = newAddress(addresses.stream().mapToInt(Address::seq).max().orElse(0) + 1);
          addresses.add(a);
          notes.add("add AD-" + a.seq());
        }
        case ADD_ALT_ID -> {
          AltId i = newAltId(g.key(), nextSeq(altIds.stream().map(AltId::key)));
          altIds.add(i);
          notes.add("add " + shortKey(g, i.key()));
        }
        case EDIT_PHONE_TYPE -> {
          int at = random.nextInt(phones.size());
          Phone old = phones.get(at);
          String type = other(Lookups.PHONE_TYPES, old.type());
          phones.set(at, new Phone(old.key(), type, old.countryCode(), old.number()));
          notes.add("edit " + shortKey(g, old.key()) + " type");
        }
        case EDIT_ZIP -> {
          int at = random.nextInt(addresses.size());
          Address old = addresses.get(at);
          List<String> zips = city(old.city()).zips();
          String zip = other(zips, old.zip());
          addresses.set(
              at, new Address(old.seq(), old.line1(), old.city(), old.state(), zip, old.type()));
          notes.add("edit AD-" + old.seq() + " zip");
        }
      }
    }
    return new GuestPlan(
        g.key(),
        g.firstName(),
        g.lastName(),
        label(g) + "-R2 " + String.join(" ", notes),
        phones,
        addresses,
        altIds);
  }

  // ---- round 3: deletes only ---------------------------------------------------------------

  private GuestPlan round3(GuestPlan g, List<DeleteRow> rows) {
    // Candidates in a fixed order; pick 1..2 of them (none when the guest has no children).
    List<String> candidates =
        Stream.of(
                g.phones().stream().map(Phone::key),
                g.addresses().stream().map(a -> a.addressKey(g.key())),
                g.altIds().stream().map(AltId::key))
            .flatMap(s -> s)
            .collect(Collectors.toCollection(ArrayList::new));
    if (candidates.isEmpty()) {
      return g;
    }
    int count = Math.min(candidates.size(), 1 + random.nextInt(2));
    List<String> picked = new ArrayList<>();
    while (picked.size() < count) {
      picked.add(candidates.remove(random.nextInt(candidates.size())));
    }
    List<String> sorted = picked.stream().sorted().toList();
    String notes =
        label(g)
            + "-R3 delete "
            + sorted.stream().map(k -> shortKey(g, k)).collect(Collectors.joining(" "));
    GuestPlan after =
        new GuestPlan(
            g.key(),
            g.firstName(),
            g.lastName(),
            notes,
            g.phones().stream().filter(p -> !sorted.contains(p.key())).toList(),
            g.addresses().stream().filter(a -> !sorted.contains(a.addressKey(g.key()))).toList(),
            g.altIds().stream().filter(i -> !sorted.contains(i.key())).toList());
    for (String k : sorted) {
      if (k.contains("-PH-")) {
        rows.add(new DeleteRow(after, DeleteGroup.PHONE, k));
      } else if (k.contains("-AI-")) {
        rows.add(new DeleteRow(after, DeleteGroup.ALT_ID, k));
      } else {
        // One address = two groups, so two rows.
        rows.add(new DeleteRow(after, DeleteGroup.ADDRESS, k));
        rows.add(new DeleteRow(after, DeleteGroup.SRC_ADDRESS, k.replace("-AD-", "-SA-")));
      }
    }
    return after;
  }

  // ---- expected results --------------------------------------------------------------------

  public static final List<String> EXPECTED_HEADER =
      List.of(
          "Source Primary Key",
          "first_name",
          "last_name",
          "touch_notes",
          "phone_count",
          "phone_keys",
          "address_count",
          "address_keys",
          "src_address_keys",
          "alt_id_count",
          "alt_id_keys",
          "phones",
          "addresses",
          "alt_ids");

  /** Expected state after a round; lists are ";"-separated so no field needs quoting. */
  static String expected(List<GuestPlan> guests) {
    Stream<String> rows =
        guests.stream()
            .map(
                g -> {
                  Map<String, String> r = new LinkedHashMap<>();
                  r.put("key", g.key());
                  r.put("first", g.firstName());
                  r.put("last", g.lastName());
                  r.put("notes", g.touchNotes());
                  r.put("pc", Integer.toString(g.phones().size()));
                  r.put("pk", join(g.phones().stream().map(Phone::key)));
                  r.put("ac", Integer.toString(g.addresses().size()));
                  r.put("ak", join(g.addresses().stream().map(a -> a.addressKey(g.key()))));
                  r.put("sk", join(g.addresses().stream().map(a -> a.srcAddressKey(g.key()))));
                  r.put("ic", Integer.toString(g.altIds().size()));
                  r.put("ik", join(g.altIds().stream().map(AltId::key)));
                  r.put("pv", join(g.phones().stream().map(p -> p.type() + "/" + p.number())));
                  r.put(
                      "av",
                      join(
                          g.addresses().stream()
                              .map(
                                  a ->
                                      a.line1() + "/" + a.city() + "/" + a.zip() + "/"
                                          + a.type())));
                  r.put("iv", join(g.altIds().stream().map(i -> i.type() + "/" + i.value())));
                  return FileImportCsv.line(r);
                });
    return Stream.concat(Stream.of(String.join(",", EXPECTED_HEADER)), rows)
            .collect(Collectors.joining(FileImportCsv.CRLF))
        + FileImportCsv.CRLF;
  }

  // ---- helpers -----------------------------------------------------------------------------

  /** Round 1: random time in the 3 years before baseDate. Rounds 2/3: baseDate+1 / +2 days. */
  private Map<String, String> dates(List<GuestPlan> guests, int round) {
    Map<String, String> out = new LinkedHashMap<>();
    for (int n = 0; n < guests.size(); n++) {
      LocalDateTime at =
          switch (round) {
            case 1 ->
                options
                    .baseDate()
                    .minusDays(1 + random.nextInt(1095))
                    .atTime(8 + random.nextInt(10), random.nextInt(60));
            default -> options.baseDate().plusDays(round - 1).atTime(9, 0).plusMinutes(n);
          };
      out.put(guests.get(n).key(), at.format(DATE));
    }
    return out;
  }

  private Phone newPhone(String root, int seq) {
    int number;
    do {
      number = 100000 + random.nextInt(900000);
    } while (!usedPhones.add(number));
    return new Phone(root + "-PH-" + seq, pick(Lookups.PHONE_TYPES), 1, number);
  }

  private Address newAddress(int seq) {
    Lookups.City c = pick(Lookups.CITIES);
    String line1 = (100 + random.nextInt(900)) + " " + pick(Lookups.STREETS);
    return new Address(
        seq, line1, c.name(), Lookups.STATE, pick(c.zips()), pick(Lookups.ADDRESS_TYPES));
  }

  private AltId newAltId(String root, int seq) {
    String type = pick(Lookups.ALT_ID_TYPES);
    String value =
        switch (type) {
          case "DL" -> "TXDL" + digits(6);
          case "Passport" -> "P" + digits(7);
          case "TIN" -> "TIN" + digits(5);
          case "EMP ID" -> "EMP" + digits(4);
          default -> "000" + digits(6);
        };
    return new AltId(root + "-AI-" + seq, type, value);
  }

  private String digits(int n) {
    StringBuilder b = new StringBuilder();
    for (int k = 0; k < n; k++) {
      b.append(random.nextInt(10));
    }
    return b.toString();
  }

  private <T> T pick(List<T> list) {
    return list.get(random.nextInt(list.size()));
  }

  private String other(List<String> list, String current) {
    List<String> rest = list.stream().filter(v -> !v.equals(current)).toList();
    return pick(rest);
  }

  private static Lookups.City city(String name) {
    return Lookups.CITIES.stream().filter(c -> c.name().equals(name)).findFirst().orElseThrow();
  }

  private static int nextSeq(Stream<String> keys) {
    return keys.mapToInt(k -> Integer.parseInt(k.substring(k.lastIndexOf('-') + 1))).max().orElse(0)
        + 1;
  }

  private static String shortKey(GuestPlan g, String childKey) {
    return childKey.substring(g.key().length() + 1);
  }

  private String label(int n, int round) {
    return "UC-RG" + n + "-R" + round;
  }

  private String label(GuestPlan g) {
    return "UC-RG" + g.key().substring(g.key().lastIndexOf('-') + 1);
  }

  private static String join(Stream<String> values) {
    return values.collect(Collectors.joining(";"));
  }
}
