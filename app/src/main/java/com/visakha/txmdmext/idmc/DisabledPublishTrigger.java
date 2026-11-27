package com.visakha.txmdmext.idmc;

/** Default: does nothing. Vamsi runs the publish job himself until txmdmext.idmc.enabled=true. */
public class DisabledPublishTrigger implements IdmcPublishTrigger {

  @Override
  public String triggerPublish() {
    return "IDMC publish trigger is disabled (txmdmext.idmc.enabled=false)";
  }
}
