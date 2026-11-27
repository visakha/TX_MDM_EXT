package com.visakha.txmdmext.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SqlNamesTest {

  @Test
  void plainOracleNamesPass() {
    assertEquals("EVT_ZLV_GUEST_MASTER_HIST", SqlNames.table("EVT_ZLV_GUEST_MASTER_HIST"));
  }

  @Test
  void anythingElseIsRejected() {
    assertThrows(IllegalArgumentException.class, () -> SqlNames.table("EVT; X"));
    assertThrows(IllegalArgumentException.class, () -> SqlNames.table("evt_lower"));
    assertThrows(IllegalArgumentException.class, () -> SqlNames.table(null));
    assertThrows(IllegalArgumentException.class, () -> SqlNames.table("A".repeat(129)));
  }
}
