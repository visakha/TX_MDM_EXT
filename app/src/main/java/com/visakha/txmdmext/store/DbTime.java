package com.visakha.txmdmext.store;

import com.visakha.txmdmext.config.AppProperties;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

/** Converts event instants to the wall-clock zone the history tables use (txmdmext.zone). */
@Component
public class DbTime {

  private final ZoneId zone;

  public DbTime(AppProperties props) {
    this.zone = props.zoneId();
  }

  public LocalDateTime of(Instant instant) {
    return instant == null ? null : LocalDateTime.ofInstant(instant, zone);
  }

  public LocalDateTime now() {
    return LocalDateTime.now(zone);
  }

  public static java.sql.Date date(LocalDate date) {
    return date == null ? null : java.sql.Date.valueOf(date);
  }

  /** Booleans are NUMBER(1): 1 true, 0 false, null not supplied. */
  public static Integer flag(Boolean value) {
    return value == null ? null : (value ? 1 : 0);
  }
}
