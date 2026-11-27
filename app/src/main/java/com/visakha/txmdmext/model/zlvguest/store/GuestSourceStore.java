package com.visakha.txmdmext.model.zlvguest.store;

import com.visakha.txmdmext.history.MemberKey;
import com.visakha.txmdmext.history.MemberVersion;
import com.visakha.txmdmext.history.XrefChangeRow;
import com.visakha.txmdmext.model.zlvguest.entity.GuestMemberHist;
import com.visakha.txmdmext.model.zlvguest.entity.GuestXrefChange;
import com.visakha.txmdmext.model.zlvguest.entity.HistFlags;
import com.visakha.txmdmext.model.zlvguest.repository.GuestMemberHistRepository;
import com.visakha.txmdmext.model.zlvguest.repository.GuestXrefChangeRepository;
import com.visakha.txmdmext.store.DbTime;
import com.visakha.txmdmext.store.SourceStore;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * zlv_guest source side: membership history (Type 2) through GuestMemberHistRepository and the xref
 * change log (append only) through GuestXrefChangeRepository.
 */
@Component
public class GuestSourceStore implements SourceStore {

  private final GuestMemberHistRepository members;
  private final GuestXrefChangeRepository xrefChanges;
  private final DbTime time;

  public GuestSourceStore(
      GuestMemberHistRepository members, GuestXrefChangeRepository xrefChanges, DbTime time) {
    this.members = members;
    this.xrefChanges = xrefChanges;
    this.time = time;
  }

  @Override
  public Set<MemberKey> openMembers(String businessId) {
    return members.findByBusinessIdAndIsCurrent(businessId, HistFlags.YES).stream()
        .map(row -> new MemberKey(row.sourceSystem(), row.sourcePkey()))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  @Override
  public void closeMember(String businessId, MemberKey m, String id01, LocalDateTime validTo) {
    members.saveAll(
        members
            .findByBusinessIdAndSourceSystemAndSourcePkeyAndIsCurrent(
                businessId, m.sourceSystem(), m.sourcePkey(), HistFlags.YES)
            .stream()
            .map(row -> row.closed(id01, validTo))
            .toList());
  }

  @Override
  public void openMember(String businessId, MemberVersion v, String id01, LocalDateTime validFrom) {
    members.save(
        new GuestMemberHist(
            null,
            businessId,
            v.key().sourceSystem(),
            v.key().sourcePkey(),
            id01,
            null,
            v.rowMd5(),
            validFrom,
            null,
            HistFlags.YES,
            null));
  }

  @Override
  public void insertXrefChange(XrefChangeRow x) {
    xrefChanges.save(
        new GuestXrefChange(
            null,
            x.id01(),
            x.businessId(),
            x.sourceSystem(),
            x.sourcePkey(),
            x.xrefType(),
            x.xrefChange(),
            x.membershipChange(),
            x.impliedOperation(),
            x.dateOfChange(),
            time.of(x.changeTime()),
            null));
  }
}
