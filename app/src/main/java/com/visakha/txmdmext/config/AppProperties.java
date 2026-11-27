package com.visakha.txmdmext.config;

import java.time.ZoneId;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/** All settings come from the external application.properties (prefix txmdmext). */
@ConfigurationProperties("txmdmext")
public record AppProperties(
    @DefaultValue("1000") int batchSize,
    @DefaultValue("UTC") String zone,
    @DefaultValue("false") boolean runOnce,
    @DefaultValue Idmc idmc) {

  public ZoneId zoneId() {
    return ZoneId.of(zone);
  }

  /**
   * Publish-job kick-off ("Pub EVT ZLV guest", a Business 360 job). Off by default. Credentials
   * only in the external file. publishJobKind is the {kind} path part and is UNVERIFIED.
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
