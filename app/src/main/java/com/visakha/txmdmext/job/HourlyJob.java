package com.visakha.txmdmext.job;

import com.visakha.txmdmext.config.AppProperties;
import com.visakha.txmdmext.idmc.IdmcPublishTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Plain @Scheduled job (not Spring Batch). Each pass: start the publish job if enabled, then load
 * what is in the event store. Events from this publish are picked up on the next pass.
 */
@Component
public class HourlyJob implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(HourlyJob.class);

  private final IdmcPublishTrigger trigger;
  private final EventProcessor processor;
  private final AppProperties props;
  private final ApplicationContext context;

  public HourlyJob(
      IdmcPublishTrigger trigger,
      EventProcessor processor,
      AppProperties props,
      ApplicationContext context) {
    this.trigger = trigger;
    this.processor = processor;
    this.props = props;
    this.context = context;
  }

  @Scheduled(cron = "${txmdmext.schedule.cron}")
  public void hourly() {
    if (!props.runOnce()) {
      pass();
    }
  }

  /** txmdmext.run-once=true: one pass, then exit. Handy for a test on the laptop. */
  @Override
  public void run(ApplicationArguments args) {
    if (props.runOnce()) {
      pass();
      System.exit(SpringApplication.exit(context, () -> 0));
    }
  }

  synchronized void pass() {
    LOG.info("pass start");
    LOG.info(trigger.triggerPublish());
    BatchResult result = processor.runOnce();
    LOG.info("pass done: {}", result);
  }
}
