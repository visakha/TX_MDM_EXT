package com.visakha.txmdmext.model.zlvguest.repository;

import com.visakha.txmdmext.model.zlvguest.entity.ZlvMdmEvent;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Limit;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

/**
 * ZLV_MDM_EVENTS_STORE (entity ZlvMdmEvent). Read and stamp only: it extends Repository, not
 * CrudRepository, so no save or delete exists here at all. Never deletes from the event store.
 */
public interface ZlvMdmEventRepository extends Repository<ZlvMdmEvent, String> {

  /** Unprocessed rows, oldest first (evt_dtm, then id01). Duplicates of an ID01 come back too. */
  List<ZlvMdmEvent> findByProcessedDtmIsNullOrderByEvtDtmAscId01Asc(Limit limit);

  /**
   * Stamps every duplicate row of the same ID01 at once. The one @Query in the app: the table has
   * no primary key and repeats ID01, so a save() of one entity cannot express "all copies".
   */
  @Modifying
  @Query(
      "UPDATE ZLV_MDM_EVENTS_STORE SET processed_dtm = :processedDtm"
          + " WHERE id01 = :id01 AND processed_dtm IS NULL")
  int markProcessed(@Param("id01") String id01, @Param("processedDtm") LocalDateTime processedDtm);
}
