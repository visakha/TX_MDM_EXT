package com.visakha.txmdmext.store;

import com.visakha.txmdmext.config.AppProperties;
import java.time.Instant;
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

  public Instant instant(LocalDateTime value) {
    return value == null ? null : value.atZone(zone).toInstant();
  }

  public LocalDateTime now() {
    return LocalDateTime.now(zone);
  }
}
