package com.visakha.txmdmext.job;

import java.util.List;

/** Counts for one pass, for the log line. failedId01 rows stay unprocessed for the next pass. */
public record BatchResult(
    int rowsRead,
    int distinctEvents,
    int loaded,
    int alreadyLoaded,
    List<String> failedId01,
    int skipped) {

  public int failed() {
    return failedId01.size();
  }

  @Override
  public String toString() {
    return "rows read=%d, distinct ID01=%d, loaded=%d, already loaded=%d, failed=%d %s, skipped=%d"
        .formatted(rowsRead, distinctEvents, loaded, alreadyLoaded, failed(), failedId01, skipped);
  }
}
