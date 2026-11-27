package com.visakha.txmdmext.web;

import com.visakha.txmdmext.TxMdmExtApplication;
import com.visakha.txmdmext.config.AppProperties;
import com.visakha.txmdmext.job.ScanStatus;
import java.time.Duration;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** GET /api/heartbeat: is the app up, and when did the last scan pass run. */
@RestController
public class HeartbeatController {

  private final ScanStatus scanStatus;
  private final String app;
  private final String version;
  private final Duration interval;
  private final Instant startedAt = Instant.now();

  public HeartbeatController(
      ScanStatus scanStatus,
      AppProperties props,
      @Value("${spring.application.name:tx-mdm-ext}") String app) {
    this.scanStatus = scanStatus;
    this.app = app;
    // Implementation-Version comes from the jar manifest; "dev" when run from classes.
    String v = TxMdmExtApplication.class.getPackage().getImplementationVersion();
    this.version = v == null ? "dev" : v;
    this.interval = props.scan() == null ? null : props.scan().interval();
  }

  @GetMapping("/api/heartbeat")
  public Heartbeat heartbeat() {
    return Heartbeat.of(app, version, startedAt, Instant.now(), scanStatus.snapshot(), interval);
  }
}
