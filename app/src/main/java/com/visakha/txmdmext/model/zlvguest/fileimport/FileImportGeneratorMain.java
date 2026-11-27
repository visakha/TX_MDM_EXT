package com.visakha.txmdmext.model.zlvguest.fileimport;

import com.visakha.txmdmext.model.zlvguest.fileimport.FileImportGenerator.Options;
import com.visakha.txmdmext.model.zlvguest.fileimport.FileImportGenerator.Round;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command line entry for the file import generator. Plain main: Spring, the scan job and the
 * database are never started. Run it through the Spring Boot jar with loader.main, see README.
 */
public final class FileImportGeneratorMain {

  private FileImportGeneratorMain() {}

  public static void main(String[] args) throws IOException {
    Map<String, String> a = parse(args);
    Options options =
        new Options(
            Long.parseLong(a.getOrDefault("seed", "42")),
            Integer.parseInt(a.getOrDefault("guests", "5")),
            Integer.parseInt(a.getOrDefault("rounds", "3")),
            LocalDate.parse(a.getOrDefault("base-date", "2026-11-01")));
    Path dir = Path.of(a.getOrDefault("out-dir", "file-import-out")).resolve(options.prefix());
    List<Path> written = write(options, dir);
    written.forEach(p -> System.out.println("wrote " + p));
  }

  /** Writes the round files into dir; never overwrites an existing file. */
  public static List<Path> write(Options options, Path dir) throws IOException {
    Files.createDirectories(dir);
    List<Round> rounds = new FileImportGenerator(options).generate();
    List<Path> out = new ArrayList<>();
    for (Round r : rounds) {
      String base = "zlv_guest_" + options.prefix() + "_round" + r.number();
      out.add(create(dir.resolve(base + ".csv"), r.importCsv()));
      out.add(create(dir.resolve(base + "_expected.csv"), r.expectedCsv()));
    }
    return out;
  }

  private static Path create(Path file, String text) throws IOException {
    // CREATE_NEW: a second run with the same seed fails instead of overwriting files.
    return Files.writeString(file, text, StandardCharsets.US_ASCII, StandardOpenOption.CREATE_NEW);
  }

  /** --name=value pairs. Unknown names fail, so a typo is not silently ignored. */
  static Map<String, String> parse(String[] args) {
    List<String> known = List.of("seed", "guests", "rounds", "base-date", "out-dir");
    Map<String, String> out = new HashMap<>();
    for (String arg : args) {
      if (!arg.startsWith("--") || !arg.contains("=")) {
        throw new IllegalArgumentException("expected --name=value, got: " + arg);
      }
      String name = arg.substring(2, arg.indexOf('='));
      if (!known.contains(name)) {
        throw new IllegalArgumentException("unknown option --" + name + ", known: " + known);
      }
      out.put(name, arg.substring(arg.indexOf('=') + 1));
    }
    return out;
  }
}
