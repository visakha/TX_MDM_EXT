package com.visakha.txmdmext.model.zlvguest.repository;

import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterAddrHist;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;

/** EVT_ZLV_GUEST_MASTER_ADDR_HIST (entity GuestMasterAddrHist). Derived queries only. */
public interface GuestMasterAddrHistRepository
    extends ListCrudRepository<GuestMasterAddrHist, Long> {

  /** Open rows of one master: is_current = 1 and is_deleted = 0. */
  List<GuestMasterAddrHist> findByBusinessIdAndIsCurrentAndIsDeleted(
      String businessId, Integer isCurrent, Integer isDeleted);

  /** The current row(s) of one child row, to be closed. */
  List<GuestMasterAddrHist> findByBusinessIdAndChildIdAndIsCurrent(
      String businessId, String childId, Integer isCurrent);

  /** Idempotency: did this event already write a row here? */
  boolean existsById01(String id01);
}
