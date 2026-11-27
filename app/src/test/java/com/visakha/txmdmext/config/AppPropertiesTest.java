package com.visakha.txmdmext.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.visakha.txmdmext.job.ScanJob;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AppPropertiesTest {

  private static Duration scan(String value) {
    Map<String, String> props = new HashMap<>(TestConfig.exampleProperties());
    props.put("txmdmext.scan.interval", value);
    return ScanJob.interval(TestConfig.bind(props));
  }

  @Test
  void scanIntervalOneMinute() {
    assertEquals(Duration.ofMinutes(1), scan("1m"));
  }

  @Test
  void scanIntervalFiveMinutes() {
    assertEquals(Duration.ofMinutes(5), scan("5m"));
  }

  @Test
  void scanIntervalTenMinutes() {
    assertEquals(Duration.ofMinutes(10), scan("10m"));
  }

  @Test
  void scanIntervalOneHour() {
    assertEquals(Duration.ofHours(1), scan("1h"));
  }

  @Test
  void scanIntervalTwoHours() {
    assertEquals(Duration.ofHours(2), scan("2h"));
  }

  @Test
  void missingScanIntervalFailsFast() {
    Map<String, String> props = new HashMap<>(TestConfig.exampleProperties());
    props.remove("txmdmext.scan.interval");
    AppProperties bound = TestConfig.bind(props);
    IllegalStateException e =
        assertThrows(IllegalStateException.class, () -> ScanJob.interval(bound));
    assertTrue(e.getMessage().contains("txmdmext.scan.interval"));
  }

  @Test
  void exampleFileBindsEveryMd5TableAndKeepsIdmcOff() {
    AppProperties p = TestConfig.example();
    assertFalse(p.idmc().enabled());
    assertEquals(Duration.ofHours(1), ScanJob.interval(p));
    assertEquals(
        TestConfig.MODEL.hashableColumns().size(), TestConfig.exampleRowHash().tables().size());
  }
}
