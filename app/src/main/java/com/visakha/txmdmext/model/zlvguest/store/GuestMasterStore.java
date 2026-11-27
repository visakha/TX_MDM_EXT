package com.visakha.txmdmext.model.zlvguest.store;

import com.visakha.txmdmext.history.Lookup;
import com.visakha.txmdmext.history.MasterVersion;
import com.visakha.txmdmext.history.MetaInfo;
import com.visakha.txmdmext.model.zlvguest.GuestRoot;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMasterHist;
import com.visakha.txmdmext.model.zlvguest.entity.HistFlags;
import com.visakha.txmdmext.model.zlvguest.repository.GuestMasterHistRepository;
import com.visakha.txmdmext.store.DbTime;
import com.visakha.txmdmext.store.MasterStore;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** zlv_guest master history (Type 2) through GuestMasterHistRepository. Close, then append. */
@Component
public class GuestMasterStore implements MasterStore {

  private static final Lookup NO_LOOKUP = new Lookup(null, null);

  private final GuestMasterHistRepository masters;
  private final DbTime time;

  public GuestMasterStore(GuestMasterHistRepository masters, DbTime time) {
    this.masters = masters;
    this.time = time;
  }

  @Override
  public Optional<String> currentMd5(String businessId) {
    return masters
        .findFirstByBusinessIdAndIsCurrentOrderByValidFromDescHistIdDesc(businessId, HistFlags.YES)
        .map(GuestMasterHist::rowMd5)
        .map(String::trim);
  }

  @Override
  public void closeCurrent(String businessId, LocalDateTime validTo) {
    masters.saveAll(
        masters.findByBusinessIdAndIsCurrent(businessId, HistFlags.YES).stream()
            .map(row -> row.closed(validTo))
            .toList());
  }

  @Override
  public void insert(MasterVersion v) {
    masters.save(toEntity(v));
  }

  /** A new, current row (histId null: the database assigns it). */
  GuestMasterHist toEntity(MasterVersion v) {
    MetaInfo m = v.meta();
    GuestRoot r = (GuestRoot) v.root();
    Lookup touchCd = r.touchCd() == null ? NO_LOOKUP : r.touchCd();
    return new GuestMasterHist(
        null,
        v.businessId(),
        v.activeBusinessId(),
        v.id01(),
        v.eventType(),
        v.impliedOperation(),
        v.correlationId(),
        v.dateOfChange(),
        v.userName(),
        v.eventSourceSystem(),
        v.mergedBusinessIds(),
        v.stateBase(),
        v.stateValidation(),
        v.stateCreateMode(),
        m.status(),
        m.createdBy(),
        time.of(m.creationDate()),
        m.updatedBy(),
        time.of(m.lastUpdateDate()),
        r.firstName(),
        r.clnsFirstName(),
        r.lastName(),
        r.clnsLastName(),
        r.ffrFirstName(),
        r.ffrLastName(),
        HistFlags.of(r.doNotContactInd()),
        r.linkedinUrl(),
        time.of(r.lastTouchDtm()),
        time.of(r.prevLastTouchDtm()),
        r.touchIncr(),
        r.touchNotes(),
        touchCd.code(),
        touchCd.name(),
        HistFlags.of(v.deleted()),
        v.payloadJson(),
        v.rowMd5(),
        time.of(v.validFrom()),
        null,
        HistFlags.YES,
        null);
  }
}
