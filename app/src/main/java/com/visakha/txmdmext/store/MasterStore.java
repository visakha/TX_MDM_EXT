package com.visakha.txmdmext.store;

import com.visakha.txmdmext.history.MasterVersion;
import java.time.LocalDateTime;
import java.util.Optional;

/** The model's master history table (Type 2). Implemented per model, it knows the columns. */
public interface MasterStore {

  /** row_md5 of the current row; empty when there is none (or it predates row_md5). */
  Optional<String> currentMd5(String businessId);

  void closeCurrent(String businessId, LocalDateTime validTo);

  void insert(MasterVersion version);
}
