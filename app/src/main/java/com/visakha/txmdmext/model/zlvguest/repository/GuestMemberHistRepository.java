package com.visakha.txmdmext.model.zlvguest.repository;

import com.visakha.txmdmext.model.zlvguest.entity.GuestMemberHist;
import java.util.List;
import org.springframework.data.repository.ListCrudRepository;

/** EVT_ZLV_GUEST_MEMBER_HIST (entity GuestMemberHist). Derived queries only. */
public interface GuestMemberHistRepository extends ListCrudRepository<GuestMemberHist, Long> {

  /** Current memberships of one master. */
  List<GuestMemberHist> findByBusinessIdAndIsCurrent(String businessId, Integer isCurrent);

  /** The current row(s) of one membership, to be closed. */
  List<GuestMemberHist> findByBusinessIdAndSourceSystemAndSourcePkeyAndIsCurrent(
      String businessId, String sourceSystem, String sourcePkey, Integer isCurrent);

  /** Idempotency: did this event open or close a membership? */
  boolean existsById01OpenedOrId01Closed(String id01Opened, String id01Closed);
}
