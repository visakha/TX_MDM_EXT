package com.visakha.txmdmext.model.zlvguest.store;

import com.visakha.txmdmext.model.zlvguest.repository.GuestFieldChangeRepository;
import com.visakha.txmdmext.model.zlvguest.repository.GuestMasterAddrHistRepository;
import com.visakha.txmdmext.model.zlvguest.repository.GuestMasterAltIdHistRepository;
import com.visakha.txmdmext.model.zlvguest.repository.GuestMasterHistRepository;
import com.visakha.txmdmext.model.zlvguest.repository.GuestMasterPhoneHistRepository;
import com.visakha.txmdmext.model.zlvguest.repository.GuestMasterSrcAddrHistRepository;
import com.visakha.txmdmext.model.zlvguest.repository.GuestMemberHistRepository;
import com.visakha.txmdmext.model.zlvguest.repository.GuestXrefChangeRepository;
import com.visakha.txmdmext.store.LoadedEventCheck;
import java.util.List;
import java.util.function.Predicate;
import org.springframework.stereotype.Component;

/**
 * Idempotency for zlv_guest: an ID01 is already loaded when ANY of the 8 written history tables has
 * a row from it (membership: opened or closed by it). Source history is not written yet.
 */
@Component
public class GuestLoadedEvents implements LoadedEventCheck {

  private final List<Predicate<String>> checks;

  public GuestLoadedEvents(
      GuestMasterHistRepository masters,
      GuestMasterPhoneHistRepository phones,
      GuestMasterSrcAddrHistRepository srcAddresses,
      GuestMasterAddrHistRepository addresses,
      GuestMasterAltIdHistRepository altIds,
      GuestFieldChangeRepository fieldChanges,
      GuestXrefChangeRepository xrefChanges,
      GuestMemberHistRepository members) {
    this.checks =
        List.of(
            masters::existsById01,
            phones::existsById01,
            srcAddresses::existsById01,
            addresses::existsById01,
            altIds::existsById01,
            fieldChanges::existsById01,
            xrefChanges::existsById01,
            id01 -> members.existsById01OpenedOrId01Closed(id01, id01));
  }

  @Override
  public boolean alreadyLoaded(String id01) {
    return checks.stream().anyMatch(check -> check.test(id01));
  }
}
