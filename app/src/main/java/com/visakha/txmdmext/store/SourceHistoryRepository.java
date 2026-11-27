package com.visakha.txmdmext.store;

import com.visakha.txmdmext.history.MemberKey;
import com.visakha.txmdmext.history.MemberVersion;
import com.visakha.txmdmext.history.XrefChangeRow;
import com.visakha.txmdmext.model.EntityModel;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/**
 * Source side from events, same for every model: membership history (Type 2) and the xref change
 * log (append only). Table names come from the model.
 */
@Repository
public class SourceHistoryRepository {

  static final String OPEN_MEMBERS =
      """
      SELECT source_system, source_pkey
        FROM %1$s
       WHERE business_id = :businessId
         AND is_current = 1
      """;

  static final String CLOSE_MEMBER =
      """
      UPDATE %1$s
         SET valid_to = :validTo, is_current = 0, id01_closed = :id01
       WHERE business_id = :businessId
         AND source_system = :sourceSystem
         AND source_pkey = :sourcePkey
         AND is_current = 1
      """;

  static final String OPEN_MEMBER =
      """
      INSERT INTO %1$s (
        business_id, source_system, source_pkey, id01_opened, row_md5, valid_from, valid_to,
        is_current)
      VALUES (:businessId, :sourceSystem, :sourcePkey, :id01, :rowMd5, :validFrom, NULL, 1)
      """;

  static final String INSERT_XREF_CHANGE =
      """
      INSERT INTO %1$s (
        id01, business_id, source_system, source_pkey, xref_type, xref_change,
        membership_change, implied_operation, date_of_change, change_dtm)
      VALUES (
        :id01, :businessId, :sourceSystem, :sourcePkey, :xrefType, :xrefChange,
        :membershipChange, :impliedOperation, :dateOfChange, :changeDtm)
      """;

  private final JdbcClient jdbc;
  private final DbTime time;
  private final String openMembers;
  private final String closeMember;
  private final String openMember;
  private final String insertXrefChange;

  public SourceHistoryRepository(JdbcClient jdbc, DbTime time, EntityModel model) {
    this.jdbc = jdbc;
    this.time = time;
    String members = SqlNames.table(model.memberTable());
    this.openMembers = OPEN_MEMBERS.formatted(members);
    this.closeMember = CLOSE_MEMBER.formatted(members);
    this.openMember = OPEN_MEMBER.formatted(members);
    this.insertXrefChange = INSERT_XREF_CHANGE.formatted(SqlNames.table(model.xrefTable()));
  }

  public Set<MemberKey> openMembers(String businessId) {
    return new LinkedHashSet<>(
        jdbc.sql(openMembers)
            .param("businessId", businessId)
            .query((rs, n) ->
                new MemberKey(rs.getString("source_system"), rs.getString("source_pkey")))
            .list());
  }

  public void closeMember(String businessId, MemberKey m, String id01, LocalDateTime validTo) {
    jdbc.sql(closeMember)
        .param("validTo", validTo, Types.TIMESTAMP)
        .param("id01", id01, Types.VARCHAR)
        .param("businessId", businessId, Types.VARCHAR)
        .param("sourceSystem", m.sourceSystem(), Types.VARCHAR)
        .param("sourcePkey", m.sourcePkey(), Types.VARCHAR)
        .update();
  }

  public void openMember(String businessId, MemberVersion v, String id01, LocalDateTime validFrom) {
    MemberKey m = v.key();
    jdbc.sql(openMember)
        .param("rowMd5", v.rowMd5(), Types.CHAR)
        .param("businessId", businessId, Types.VARCHAR)
        .param("sourceSystem", m.sourceSystem(), Types.VARCHAR)
        .param("sourcePkey", m.sourcePkey(), Types.VARCHAR)
        .param("id01", id01, Types.VARCHAR)
        .param("validFrom", validFrom, Types.TIMESTAMP)
        .update();
  }

  public void insertXrefChange(XrefChangeRow x) {
    jdbc.sql(insertXrefChange)
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
