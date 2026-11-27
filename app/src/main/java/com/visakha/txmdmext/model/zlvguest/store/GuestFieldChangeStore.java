package com.visakha.txmdmext.model.zlvguest.store;

import com.visakha.txmdmext.history.FieldChangeRow;
import com.visakha.txmdmext.model.zlvguest.entity.GuestFieldChange;
import com.visakha.txmdmext.model.zlvguest.repository.GuestFieldChangeRepository;
import com.visakha.txmdmext.store.DbTime;
import com.visakha.txmdmext.store.FieldChangeStore;
import org.springframework.stereotype.Component;

/** zlv_guest field change log through GuestFieldChangeRepository. Append only. */
@Component
public class GuestFieldChangeStore implements FieldChangeStore {

  private final GuestFieldChangeRepository fieldChanges;
  private final DbTime time;

  public GuestFieldChangeStore(GuestFieldChangeRepository fieldChanges, DbTime time) {
    this.fieldChanges = fieldChanges;
    this.time = time;
  }

  @Override
  public void insert(FieldChangeRow c) {
    fieldChanges.save(
        new GuestFieldChange(
            null,
            c.id01(),
            c.seqNo(),
            c.businessId(),
            c.fieldPath(),
            c.fieldGroup(),
            c.childId(),
            c.fieldName(),
            c.changeType(),
            c.oldValue(),
            c.newValue(),
            c.dateOfChange(),
            time.of(c.changeTime()),
            null));
  }
}
