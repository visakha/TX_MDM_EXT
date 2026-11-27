package com.visakha.txmdmext.job;

/** Counts for one pass, for the log line. */
public record BatchResult(
    int rowsRead, int distinctEvents, int loaded, int alreadyLoaded, int failed, int skipped) {

  @Override
  public String toString() {
    return "rows read=%d, distinct ID01=%d, loaded=%d, already loaded=%d, failed=%d, skipped=%d"
        .formatted(rowsRead, distinctEvents, loaded, alreadyLoaded, failed, skipped);
  }
}
