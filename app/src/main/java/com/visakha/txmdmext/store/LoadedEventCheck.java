package com.visakha.txmdmext.store;

/**
 * Idempotency: has any history table of the model already got a row from this ID01? With row_md5
 * CDC an event may write no master row, so every written table is checked. Implemented per model.
 */
public interface LoadedEventCheck {

  boolean alreadyLoaded(String id01);
}
