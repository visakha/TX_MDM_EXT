package com.visakha.txmdmext.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.visakha.txmdmext.config.AppProperties;
import com.visakha.txmdmext.job.BatchResult;
import com.visakha.txmdmext.job.ScanStatus;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class HeartbeatTest {

  private static final Instant T0 = Instant.parse("2026-11-27T08:00:00Z");

  @Test
  void beforeFirstPassScanFieldsAreNull() {
    Heartbeat hb =
        Heartbeat.of(
            "tx-mdm-ext",
            "0.1.0",
            T0,
            T0.plusSeconds(90),
            new ScanStatus().snapshot(),
            Duration.ofHours(1));
    assertEquals("UP", hb.status());
    assertEquals(90, hb.uptimeSeconds());
    assertNull(hb.lastScanResult());
    assertNull(hb.lastScanStartedAt());
    assertNull(hb.nextScanDueAt());
    assertEquals("PT1H", hb.scanInterval());
  }

  @Test
  void afterPassShowsCountsAndNextDue() {
    ScanStatus status = new ScanStatus();
    status.started(T0);
    BatchResult result = new BatchResult(44, 36, 35, 0, List.of("bad1"), 1);
    status.finished(T0.plusSeconds(4), result, null, Duration.ofHours(1));
    Heartbeat hb =
        Heartbeat.of(
            "tx-mdm-ext", "0.1.0", T0, T0.plusSeconds(60), status.snapshot(), Duration.ofHours(1));
    assertEquals(T0, hb.lastScanStartedAt());
    assertEquals(T0.plusSeconds(4), hb.lastScanEndedAt());
    assertEquals(44, hb.lastScanResult().rowsRead());
    assertEquals(36, hb.lastScanResult().distinctEvents());
    assertEquals(1, hb.lastScanResult().failed());
    assertEquals(1, hb.lastScanResult().skipped());
    assertEquals(T0.plusSeconds(4).plus(Duration.ofHours(1)), hb.nextScanDueAt());
  }

  @Test
  void runOnceHasNoNextDueAndKeepsError() {
    ScanStatus status = new ScanStatus();
    status.started(T0);
    status.finished(T0.plusSeconds(2), null, "IllegalStateException: boom", null);
    Heartbeat hb = Heartbeat.of("tx-mdm-ext", "dev", T0, T0, status.snapshot(), null);
    assertNull(hb.nextScanDueAt());
    assertNull(hb.lastScanResult());
    assertNull(hb.scanInterval());
    assertEquals("IllegalStateException: boom", hb.lastScanError());
  }

  @Test
  void getHeartbeatReturnsJson() throws Exception {
    AppProperties props =
        new AppProperties(
            1000,
            "UTC",
            false,
            new AppProperties.Scan(Duration.ofMinutes(5)),
            new AppProperties.Idmc(false, null, null, null, null, null, null),
            Map.of());
    MockMvc mvc =
        MockMvcBuilders.standaloneSetup(
                new HeartbeatController(new ScanStatus(), props, "tx-mdm-ext"))
            .build();
    String body =
        mvc.perform(get("/api/heartbeat"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString();
    assertTrue(body.contains("\"status\":\"UP\""), body);
    assertTrue(body.contains("\"app\":\"tx-mdm-ext\""), body);
    assertTrue(body.contains("\"version\":\"dev\""), body);
    assertTrue(body.contains("\"scanInterval\":\"PT5M\""), body);
    assertTrue(body.contains("\"lastScanResult\":null"), body);
  }
}
