package com.visakha.txmdmext.model.zlvguest.repository;

import com.visakha.txmdmext.model.zlvguest.entity.GuestXrefChange;
import org.springframework.data.repository.ListCrudRepository;

/** EVT_ZLV_GUEST_XREF_CHANGE (entity GuestXrefChange). Append only: save() inserts. */
public interface GuestXrefChangeRepository extends ListCrudRepository<GuestXrefChange, Long> {

  /** Idempotency: did this event already write a row here? */
  boolean existsById01(String id01);
}
