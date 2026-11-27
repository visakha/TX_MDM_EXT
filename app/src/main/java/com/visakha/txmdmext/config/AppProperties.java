package com.visakha.txmdmext.config;

import java.time.Duration;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** All settings come from the external application.properties (prefix txmdmext). */
@ConfigurationProperties("txmdmext")
public record AppProperties(
    @DefaultValue("1000") int batchSize,
    @DefaultValue("UTC") String zone,
    @DefaultValue("false") boolean runOnce,
    @DefaultValue Scan scan,
    @DefaultValue Idmc idmc,
    Map<String, List<String>> md5) {

  public ZoneId zoneId() {
    return ZoneId.of(zone);
  }

  /**
   * How often the event store is scanned, e.g. 1m, 5m, 10m, 1h, 2h. No default in code: it must be
   * set in application.properties (txmdmext.scan.interval).
   */
  public record Scan(Duration interval) {}

  /**
   * Publish-job kick-off (mdm_zlv_guest_pub_evts_001, Job Process Type Publish Business Events;
   * tested by Vamsi 2026-11-26). Credentials only in the external file.
   */
  public record Idmc(
      @DefaultValue("false") boolean enabled,
      String loginUrl,
      String mdmUrl,
      String username,
      String password,
      String publishJobKind,
      String publishJobDefinitionId) {}
}
