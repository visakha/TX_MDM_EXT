package com.visakha.txmdmext.model.zlvguest.repository;

import com.visakha.txmdmext.model.zlvguest.entity.GuestFieldChange;
import org.springframework.data.repository.ListCrudRepository;

/** EVT_ZLV_GUEST_FIELD_CHANGE (entity GuestFieldChange). Append only: save() inserts. */
public interface GuestFieldChangeRepository extends ListCrudRepository<GuestFieldChange, Long> {

  /** Idempotency: did this event already write a row here? */
  boolean existsById01(String id01);
}
