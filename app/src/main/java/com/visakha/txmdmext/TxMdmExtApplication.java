package com.visakha.txmdmext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Reads business events from ZLV_MDM_EVENTS_STORE and writes the history tables of the model (today
 * zlv_guest, see package model.zlvguest).
 */
@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan
public class TxMdmExtApplication {

  public static void main(String[] args) {
    SpringApplication.run(TxMdmExtApplication.class, args);
  }
}
