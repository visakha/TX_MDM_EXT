package com.visakha.txmdmext.store;

import com.visakha.txmdmext.history.ChildVersion;
import com.visakha.txmdmext.history.OpenChild;
import com.visakha.txmdmext.model.ChildGroup;
import java.time.LocalDateTime;
import java.util.Map;

/** The model's child group history tables (Type 2). Implemented per model. */
public interface ChildStore {

  /** Open (current, not deleted) rows of one group, by child _id, read back as stored. */
  Map<String, OpenChild> openRows(ChildGroup group, String businessId);

  void closeCurrent(ChildGroup group, String businessId, String childId, LocalDateTime validTo);

  void insert(String businessId, String id01, ChildVersion version, LocalDateTime validFrom);
}
