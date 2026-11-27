package com.visakha.txmdmext.store;

import com.visakha.txmdmext.history.MemberKey;
import com.visakha.txmdmext.history.XrefChangeRow;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Source side from events: EVT_MEMBER_HIST (Type 2) and EVT_XREF_CHANGE (append only). */
@Repository
public class SourceHistoryRepository {

  static final String OPEN_MEMBERS =
      """
      SELECT source_system, source_pkey
        FROM EVT_MEMBER_HIST
       WHERE business_id = :businessId
         AND is_current = 1
      """;

  static final String CLOSE_MEMBER =
      """
      UPDATE EVT_MEMBER_HIST
         SET valid_to = :validTo, is_current = 0, id01_closed = :id01
       WHERE business_id = :businessId
         AND source_system = :sourceSystem
         AND source_pkey = :sourcePkey
         AND is_current = 1
      """;

  static final String OPEN_MEMBER =
      """
      INSERT INTO EVT_MEMBER_HIST (
        business_id, source_system, source_pkey, id01_opened, valid_from, valid_to, is_current)
      VALUES (:businessId, :sourceSystem, :sourcePkey, :id01, :validFrom, NULL, 1)
      """;

  static final String INSERT_XREF_CHANGE =
      """
      INSERT INTO EVT_XREF_CHANGE (
        id01, business_id, source_system, source_pkey, xref_type, xref_change,
        membership_change, implied_operation, date_of_change, change_dtm)
      VALUES (
        :id01, :businessId, :sourceSystem, :sourcePkey, :xrefType, :xrefChange,
        :membershipChange, :impliedOperation, :dateOfChange, :changeDtm)
      """;

  private final JdbcClient jdbc;
  private final DbTime time;

  public SourceHistoryRepository(JdbcClient jdbc, DbTime time) {
    this.jdbc = jdbc;
    this.time = time;
  }

  public Set<MemberKey> openMembers(String businessId) {
    return new LinkedHashSet<>(
        jdbc.sql(OPEN_MEMBERS)
            .param("businessId", businessId)
            .query((rs, n) ->
                new MemberKey(rs.getString("source_system"), rs.getString("source_pkey")))
            .list());
  }

  public void closeMember(String businessId, MemberKey m, String id01, LocalDateTime validTo) {
    jdbc.sql(CLOSE_MEMBER)
        .param("validTo", validTo, Types.TIMESTAMP)
        .param("id01", id01, Types.VARCHAR)
        .param("businessId", businessId, Types.VARCHAR)
        .param("sourceSystem", m.sourceSystem(), Types.VARCHAR)
        .param("sourcePkey", m.sourcePkey(), Types.VARCHAR)
        .update();
  }

  public void openMember(String businessId, MemberKey m, String id01, LocalDateTime validFrom) {
    jdbc.sql(OPEN_MEMBER)
        .param("businessId", businessId, Types.VARCHAR)
        .param("sourceSystem", m.sourceSystem(), Types.VARCHAR)
        .param("sourcePkey", m.sourcePkey(), Types.VARCHAR)
        .param("id01", id01, Types.VARCHAR)
        .param("validFrom", validFrom, Types.TIMESTAMP)
        .update();
  }

  public void insertXrefChange(XrefChangeRow x) {
    jdbc.sql(INSERT_XREF_CHANGE)
        .param("id01", x.id01(), Types.VARCHAR)
        .param("businessId", x.businessId(), Types.VARCHAR)
        .param("sourceSystem", x.sourceSystem(), Types.VARCHAR)
        .param("sourcePkey", x.sourcePkey(), Types.VARCHAR)
        .param("xrefType", x.xrefType(), Types.VARCHAR)
        .param("xrefChange", x.xrefChange(), Types.VARCHAR)
        .param("membershipChange", x.membershipChange(), Types.VARCHAR)
        .param("impliedOperation", x.impliedOperation(), Types.VARCHAR)
        .param("dateOfChange", x.dateOfChange(), Types.BIGINT)
        .param("changeDtm", time.of(x.changeTime()), Types.TIMESTAMP)
        .update();
  }
}
