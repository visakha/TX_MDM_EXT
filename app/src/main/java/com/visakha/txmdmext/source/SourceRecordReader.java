package com.visakha.txmdmext.source;

import com.visakha.txmdmext.history.MemberKey;
import java.util.Optional;

/**
 * Reads one source record's VALUES from the B360 source-record API. Events carry only source keys,
 * so the source history table (EVT_<DOMAIN>_SOURCE_HIST) can only be filled through this.
 */
public interface SourceRecordReader {

  /** The source record as JSON, or empty when it cannot be read. */
  Optional<String> read(MemberKey source);
}
