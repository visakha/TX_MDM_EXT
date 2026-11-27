package com.visakha.txmdmext.model.zlvguest.repository;

import com.visakha.txmdmext.model.zlvguest.entity.GuestSourceHist;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;

/**
 * EVT_ZLV_GUEST_SOURCE_HIST (entity GuestSourceHist). Not written yet: it waits for the B360
 * source-record reader (see GuestSourceHist).
 */
public interface GuestSourceHistRepository extends ListCrudRepository<GuestSourceHist, Long> {

  /** The current row(s) of one source record. */
  List<GuestSourceHist> findBySourceSystemAndSourcePkeyAndIsCurrent(
      String sourceSystem, String sourcePkey, Integer isCurrent);
}
