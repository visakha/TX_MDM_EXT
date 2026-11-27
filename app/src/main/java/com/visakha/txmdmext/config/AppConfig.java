package com.visakha.txmdmext.config;

import com.visakha.txmdmext.event.EventParser;
import com.visakha.txmdmext.idmc.B360JobPublishTrigger;
import com.visakha.txmdmext.idmc.DisabledPublishTrigger;
import com.visakha.txmdmext.idmc.IdmcPublishTrigger;
import com.visakha.txmdmext.source.NotConfiguredSourceRecordReader;
import com.visakha.txmdmext.source.SourceRecordReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the plain-Java pieces. The IDMC trigger is off unless txmdmext.idmc.enabled=true. */
@Configuration
public class AppConfig {

  @Bean
  EventParser eventParser() {
    return new EventParser();
  }

  @Bean
  IdmcPublishTrigger idmcPublishTrigger(AppProperties props) {
    return props.idmc().enabled()
        ? new B360JobPublishTrigger(props.idmc())
        : new DisabledPublishTrigger();
  }

  @Bean
  SourceRecordReader sourceRecordReader() {
    return new NotConfiguredSourceRecordReader();
  }
}
