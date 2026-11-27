package com.visakha.txmdmext.event;

/** dataDiff changeType. A cleared field or a removed child row comes as DELETED. */
public enum ChangeType {
  CREATED,
  UPDATED,
  DELETED;

  public static ChangeType from(String value) {
    return value == null ? null : valueOf(value);
  }
}
