package com.visakha.txmdmext.config;

import com.visakha.txmdmext.history.RowHash;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

/** Binds AppProperties the way Spring Boot does, from application.properties.example. */
public final class TestConfig {

  public static final Path EXAMPLE = Path.of("application.properties.example");

  private TestConfig() {}

  public static Map<String, String> exampleProperties() {
    try (Reader in = Files.newBufferedReader(EXAMPLE, StandardCharsets.UTF_8)) {
      Properties p = new Properties();
      p.load(in);
      Map<String, String> map = new HashMap<>();
      p.stringPropertyNames().forEach(k -> map.put(k, p.getProperty(k)));
      return map;
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  public static AppProperties bind(Map<String, String> properties) {
    Binder binder = new Binder(new MapConfigurationPropertySource(properties));
    return binder.bindOrCreate("txmdmext", AppProperties.class);
  }

  public static AppProperties example() {
    return bind(exampleProperties());
  }

  public static RowHash exampleRowHash() {
    return RowHash.fromConfig(example().md5());
  }
}
