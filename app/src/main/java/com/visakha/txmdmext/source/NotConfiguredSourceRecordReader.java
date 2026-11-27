package com.visakha.txmdmext.source;

import com.visakha.txmdmext.history.MemberKey;
import java.util.Optional;

/**
 * TODO: call the B360 source-record API (tx-mdm backend has the read path) and fill
 * EVT_SOURCE_HIST. Until then nothing is written there - we do not fake source values.
 */
public class NotConfiguredSourceRecordReader implements SourceRecordReader {

  @Override
  public Optional<String> read(MemberKey source) {
    return Optional.empty();
  }
}
