package com.visakha.txmdmext.model.zlvguest.fileimport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.visakha.txmdmext.model.zlvguest.fileimport.FileImportGenerator.Options;
import com.visakha.txmdmext.model.zlvguest.fileimport.FileImportGenerator.Round;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The seeded file import generator: determinism, format rules, and files vs expected results. */
class FileImportGeneratorTest {

  private static final Options OPTIONS = new Options(42, 12, 3, LocalDate.parse("2026-11-01"));

  private static List<Round> rounds(Options o) {
    return new FileImportGenerator(o).generate();
  }

  /** CSV text to rows of column name -> value (header first). */
  private static List<Map<String, String>> rows(String csv) {
    assertTrue(csv.endsWith("\r\n"));
    List<String> lines = Arrays.asList(csv.split("\r\n"));
    List<String> header = Arrays.asList(lines.get(0).split(",", -1));
    List<Map<String, String>> out = new ArrayList<>();
    for (String line : lines.subList(1, lines.size())) {
      String[] v = line.split(",", -1);
      assertEquals(header.size(), v.length, line);
      Map<String, String> row = new TreeMap<>();
      for (int i = 0; i < v.length; i++) {
        row.put(header.get(i), v[i]);
      }
      out.add(row);
    }
    return out;
  }

  /** Per guest, the distinct child keys of one key column in an import file. */
  private static Map<String, Set<String>> keys(List<Map<String, String>> rows, String column) {
    Map<String, Set<String>> out = new TreeMap<>();
    rows.forEach(
        r -> {
          Set<String> set = out.computeIfAbsent(r.get("Source Primary Key"), k -> new TreeSet<>());
          if (!r.get(column).isEmpty()) {
            set.add(r.get(column));
          }
        });
    return out;
  }

  /** Per guest, the keys listed in one column of an expected file. */
  private static Map<String, Set<String>> expectedKeys(Round r, String column) {
    return rows(r.expectedCsv()).stream()
        .collect(
            Collectors.toMap(
                e -> e.get("Source Primary Key"),
                e ->
                    e.get(column).isEmpty()
                        ? new TreeSet<>()
                        : new TreeSet<>(Arrays.asList(e.get(column).split(";"))),
                (a, b) -> a,
                TreeMap::new));
  }

  @Test
  void sameSeedGivesTheSameFiles() {
    List<Round> a = rounds(OPTIONS);
    List<Round> b = rounds(OPTIONS);
    for (int i = 0; i < 3; i++) {
      assertEquals(a.get(i).importCsv(), b.get(i).importCsv());
      assertEquals(a.get(i).expectedCsv(), b.get(i).expectedCsv());
    }
    List<Round> c = rounds(new Options(43, 12, 3, LocalDate.parse("2026-11-01")));
    assertNotEquals(a.get(0).importCsv(), c.get(0).importCsv());
    assertTrue(c.get(0).importCsv().contains("RG-43-1,"));
  }

  @Test
  void filesAreAsciiWithoutQuotesAndUseOneHeader() {
    for (Round r : rounds(OPTIONS)) {
      for (String text : List.of(r.importCsv(), r.expectedCsv())) {
        assertTrue(text.chars().allMatch(c -> c == '\r' || c == '\n' || (c >= 32 && c < 127)));
        assertTrue(!text.contains("\""));
        assertEquals(text.split("\n", -1).length - 1, text.split("\r\n", -1).length - 1);
      }
      assertTrue(r.importCsv().startsWith(String.join(",", FileImportCsv.HEADER) + "\r\n"));
      assertEquals(32, FileImportCsv.HEADER.size());
    }
  }

  @Test
  void roundsOneAndTwoListExactlyTheExpectedChildren() {
    for (Round r : rounds(OPTIONS).subList(0, 2)) {
      List<Map<String, String>> rows = rows(r.importCsv());
      assertEquals(expectedKeys(r, "phone_keys"), keys(rows, "x_phone.Source Primary Key"));
      assertEquals(expectedKeys(r, "address_keys"), keys(rows, "Address.Source Primary Key"));
      assertEquals(
          expectedKeys(r, "src_address_keys"), keys(rows, "x_src_address.Source Primary Key"));
      assertEquals(expectedKeys(r, "alt_id_keys"), keys(rows, "x_alt_id.Source Primary Key"));
      assertTrue(rows.stream().allMatch(x -> x.get("Operation Type").isEmpty()));
      assertTrue(rows.stream().noneMatch(x -> x.values().contains("Delete")));
    }
  }

  @Test
  void roundTwoOnlyAddsAndEditsEveryGuest() {
    List<Round> r = rounds(OPTIONS);
    for (String col : List.of("phone_keys", "address_keys", "alt_id_keys")) {
      Map<String, Set<String>> before = expectedKeys(r.get(0), col);
      Map<String, Set<String>> after = expectedKeys(r.get(1), col);
      before.forEach((guest, keys) -> assertTrue(after.get(guest).containsAll(keys), guest));
    }
    assertNotEquals(r.get(0).expectedCsv(), r.get(1).expectedCsv());
    rows(r.get(1).expectedCsv()).forEach(e -> assertTrue(e.get("touch_notes").contains("-R2 ")));
    // An edit never names an entry that round 2 itself added.
    rows(r.get(1).expectedCsv())
        .forEach(
            e -> {
              String notes = e.get("touch_notes");
              for (String part : notes.split(" add ")) {
                if (part.startsWith("PH-") || part.startsWith("AD-") || part.startsWith("AI-")) {
                  String added = part.split(" ")[0];
                  assertTrue(!notes.contains("edit " + added + " "), notes);
                }
              }
            });
  }

  @Test
  void roundThreeRowsEachDeleteExactlyOneEntry() {
    List<Round> r = rounds(OPTIONS);
    List<String> groups = List.of("x_phone", "Address", "x_src_address", "x_alt_id");
    Map<String, Map<String, String>> current =
        rows(r.get(1).expectedCsv()).stream()
            .collect(Collectors.toMap(e -> e.get("Source Primary Key"), e -> e));
    for (Map<String, String> row : rows(r.get(2).importCsv())) {
      assertEquals("Patch", row.get("Operation Type"));
      Map<String, String> guest = current.get(row.get("Source Primary Key"));
      assertEquals(guest.get("first_name"), row.get("first_name"));
      assertEquals(guest.get("last_name"), row.get("last_name"));
      List<String> deleting =
          groups.stream().filter(g -> "Delete".equals(row.get(g + ".Operation Type"))).toList();
      assertEquals(1, deleting.size(), row.toString());
      String g = deleting.get(0);
      assertTrue(!row.get(g + ".Source Primary Key").isEmpty());
      // Every other child column of the row is blank.
      long filled =
          row.entrySet().stream()
              .filter(e -> e.getKey().contains("."))
              .filter(e -> !e.getValue().isEmpty())
              .count();
      assertEquals(2, filled, row.toString());
    }
    // Expected after round 3 = round 2 minus exactly the keys the delete rows name.
    for (String[] pair :
        List.of(
            new String[] {"phone_keys", "x_phone"},
            new String[] {"address_keys", "Address"},
            new String[] {"src_address_keys", "x_src_address"},
            new String[] {"alt_id_keys", "x_alt_id"})) {
      Map<String, Set<String>> want = expectedKeys(r.get(1), pair[0]);
      rows(r.get(2).importCsv()).stream()
          .filter(x -> "Delete".equals(x.get(pair[1] + ".Operation Type")))
          .forEach(
              x ->
                  assertTrue(
                      want.get(x.get("Source Primary Key"))
                          .remove(x.get(pair[1] + ".Source Primary Key"))));
      assertEquals(want, expectedKeys(r.get(2), pair[0]));
    }
  }

  @Test
  void valuesFollowTheVerifiedImportRules() throws IOException {
    String lookups = Files.readString(Path.of("..", "internal-docs", "LOOKUPS.md"));
    List<String> codes = new ArrayList<>();
    codes.addAll(Lookups.PHONE_TYPES);
    codes.addAll(Lookups.ADDRESS_TYPES);
    codes.addAll(Lookups.ALT_ID_TYPES);
    codes.addAll(List.of(Lookups.ADDRESS_STATUS, Lookups.STATE, Lookups.COUNTRY));
    codes.forEach(c -> assertTrue(lookups.contains("| " + c + " |"), c));

    List<Round> r = rounds(OPTIONS);
    LocalDateTime previousMax = LocalDateTime.MIN;
    for (Round round : r) {
      List<Map<String, String>> rows = rows(round.importCsv());
      for (Map<String, String> row : rows) {
        String num = row.get("x_phone.phone_num");
        assertTrue(num.isEmpty() || num.matches("[1-9][0-9]{5}"), num);
        if (!row.get("Address.Source Primary Key").isEmpty()
            && row.get("Address.Operation Type").isEmpty()) {
          assertEquals("US", row.get("Address.country"));
        }
        assertTrue(row.get("Source Primary Key").startsWith("RG-42-"));
      }
      List<LocalDateTime> dates =
          rows.stream()
              .map(
                  x ->
                      LocalDateTime.parse(
                          x.get("Source Last Update Date"), FileImportGenerator.DATE))
              .toList();
      LocalDateTime min = dates.stream().min(LocalDateTime::compareTo).orElseThrow();
      assertTrue(min.isAfter(previousMax), "round " + round.number());
      previousMax = dates.stream().max(LocalDateTime::compareTo).orElseThrow();
    }
  }

  @Test
  void commandLineWritesSixFilesAndNeverOverwrites(@TempDir Path tmp) throws IOException {
    Options o = new Options(7, 3, 3, LocalDate.parse("2026-11-01"));
    List<Path> files = FileImportGeneratorMain.write(o, tmp.resolve(o.prefix()));
    assertEquals(6, files.size());
    assertTrue(files.get(0).getFileName().toString().equals("zlv_guest_RG-7_round1.csv"));
    assertThrows(
        IOException.class, () -> FileImportGeneratorMain.write(o, tmp.resolve(o.prefix())));
    assertThrows(
        IllegalArgumentException.class,
        () -> FileImportGeneratorMain.parse(new String[] {"--guest=5"}));
    assertThrows(IllegalArgumentException.class, () -> new Options(1, 0, 3, LocalDate.now()));
  }
}
