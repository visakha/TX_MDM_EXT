package com.visakha.txmdmext.web;

import com.visakha.txmdmext.job.ScanStatus;
import java.time.Duration;
import java.time.Instant;

/** JSON body of GET /api/heartbeat. Read-only, built from memory, no database call. */
public record Heartbeat(
    String status,
    String app,
    String version,
    Instant startedAt,
    Instant now,
    long uptimeSeconds,
    Instant lastScanStartedAt,
    Instant lastScanEndedAt,
    ScanCounts lastScanResult,
    String lastScanError,
    Instant nextScanDueAt,
    String scanInterval) {

  /** Counts of the last pass; null in the heartbeat until the first pass has finished. */
  public record ScanCounts(
      int rowsRead, int distinctEvents, int loaded, int alreadyLoaded, int failed, int skipped) {}

  public static Heartbeat of(
      String app,
      String version,
      Instant startedAt,
      Instant now,
      ScanStatus.Snapshot scan,
      Duration interval) {
    ScanCounts counts =
        scan.lastScanResult() == null
            ? null
            : new ScanCounts(
                scan.lastScanResult().rowsRead(),
                scan.lastScanResult().distinctEvents(),
                scan.lastScanResult().loaded(),
                scan.lastScanResult().alreadyLoaded(),
                scan.lastScanResult().failed(),
                scan.lastScanResult().skipped());
    return new Heartbeat(
        "UP",
        app,
        version,
        startedAt,
        now,
        Duration.between(startedAt, now).toSeconds(),
        scan.lastScanStartedAt(),
        scan.lastScanEndedAt(),
        counts,
        scan.lastScanError(),
        scan.nextScanDueAt(),
        interval == null ? null : interval.toString());
  }
}
