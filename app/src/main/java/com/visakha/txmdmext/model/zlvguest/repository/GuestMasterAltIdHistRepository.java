package com.visakha.txmdmext.model.zlvguest.repository;

import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterAltIdHist;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;

/** EVT_ZLV_GUEST_MASTER_ALT_ID_HIST (entity GuestMasterAltIdHist). Derived queries only. */
public interface GuestMasterAltIdHistRepository
    extends ListCrudRepository<GuestMasterAltIdHist, Long> {

  /** Open rows of one master: is_current = 1 and is_deleted = 0. */
  List<GuestMasterAltIdHist> findByBusinessIdAndIsCurrentAndIsDeleted(
      String businessId, Integer isCurrent, Integer isDeleted);

  /** The current row(s) of one child row, to be closed. */
  List<GuestMasterAltIdHist> findByBusinessIdAndChildIdAndIsCurrent(
      String businessId, String childId, Integer isCurrent);

  /** Idempotency: did this event already write a row here? */
  boolean existsById01(String id01);
}
