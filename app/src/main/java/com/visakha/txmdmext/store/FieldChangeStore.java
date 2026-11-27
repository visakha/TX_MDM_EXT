package com.visakha.txmdmext.store;

import com.visakha.txmdmext.history.FieldChangeRow;

/** The model's field change log (append only). Implemented per model. */
public interface FieldChangeStore {

  void insert(FieldChangeRow row);
}
