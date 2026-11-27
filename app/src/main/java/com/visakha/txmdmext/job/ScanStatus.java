package com.visakha.txmdmext.job;

import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;

/** In-memory state of the last scan pass, read by the heartbeat. Lost on restart, by design. */
@Component
public class ScanStatus {

  /** One immutable snapshot, swapped whole so readers never see half an update. */
  public record Snapshot(
      Instant lastScanStartedAt,
      Instant lastScanEndedAt,
      BatchResult lastScanResult,
      String lastScanError,
      Instant nextScanDueAt) {}

  private volatile Snapshot snapshot = new Snapshot(null, null, null, null, null);

  public Snapshot snapshot() {
    return snapshot;
  }

  public void started(Instant at) {
    Snapshot s = snapshot;
    snapshot = new Snapshot(at, s.lastScanEndedAt(), s.lastScanResult(), s.lastScanError(), null);
  }

  /** interval null = run-once mode, no next pass. */
  public void finished(Instant at, BatchResult result, String error, Duration interval) {
    Instant next = interval == null ? null : at.plus(interval);
    snapshot = new Snapshot(snapshot.lastScanStartedAt(), at, result, error, next);
  }
}
