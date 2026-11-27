package com.visakha.txmdmext.model.zlvguest.repository;

import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterHist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.ListCrudRepository;

/** EVT_ZLV_GUEST_MASTER_HIST (entity GuestMasterHist). Derived queries only. */
public interface GuestMasterHistRepository extends ListCrudRepository<GuestMasterHist, Long> {

  /** The newest current row of one master (its row_md5 drives change detection). */
  Optional<GuestMasterHist> findFirstByBusinessIdAndIsCurrentOrderByValidFromDescHistIdDesc(
      String businessId, Integer isCurrent);

  /** Every current row of one master, to be closed. */
  List<GuestMasterHist> findByBusinessIdAndIsCurrent(String businessId, Integer isCurrent);

  /** Idempotency: did this event already write a row here? */
  boolean existsById01(String id01);
}
