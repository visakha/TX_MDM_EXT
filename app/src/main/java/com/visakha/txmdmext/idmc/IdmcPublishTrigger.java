package com.visakha.txmdmext.idmc;

/** Starts the publish-events job ("Pub EVT ZLV guest") before each hourly pass. */
public interface IdmcPublishTrigger {

  /** Returns a short status line for the log. Must never log credentials. */
  String triggerPublish();
}
