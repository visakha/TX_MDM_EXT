package com.visakha.txmdmext.store;

import com.visakha.txmdmext.history.MemberKey;
import com.visakha.txmdmext.history.MemberVersion;
import com.visakha.txmdmext.history.XrefChangeRow;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * The model's source side from events: membership history (Type 2) and the xref change log (append
 * only). Implemented per model.
 */
public interface SourceStore {

  Set<MemberKey> openMembers(String businessId);

  void closeMember(String businessId, MemberKey member, String id01, LocalDateTime validTo);

  void openMember(String businessId, MemberVersion member, String id01, LocalDateTime validFrom);

  void insertXrefChange(XrefChangeRow row);
}
