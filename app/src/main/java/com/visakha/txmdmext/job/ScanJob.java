package com.visakha.txmdmext.job;

import com.visakha.txmdmext.config.AppProperties;
import com.visakha.txmdmext.idmc.IdmcPublishTrigger;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

/**
 * Plain scheduled job (not Spring Batch). Every txmdmext.scan.interval: start the publish job if
 * enabled, then load what is in the event store. New events land on the next pass.
 */
@Component
public class ScanJob implements SchedulingConfigurer, ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(ScanJob.class);

  private final IdmcPublishTrigger trigger;
  private final EventProcessor processor;
  private final AppProperties props;
  private final ApplicationContext context;

  public ScanJob(
      IdmcPublishTrigger trigger,
      EventProcessor processor,
      AppProperties props,
      ApplicationContext context) {
    this.trigger = trigger;
    this.processor = processor;
    this.props = props;
    this.context = context;
  }

  /** Validates and returns txmdmext.scan.interval. Fails fast with a plain message. */
  public static Duration interval(AppProperties props) {
    Duration interval = props.scan() == null ? null : props.scan().interval();
    if (interval == null || interval.isZero() || interval.isNegative()) {
      throw new IllegalStateException(
          "txmdmext.scan.interval is missing or not positive in application.properties"
              + " (examples: 1m, 5m, 10m, 1h, 2h)");
    }
    return interval;
  }

  @Override
  public void configureTasks(ScheduledTaskRegistrar registrar) {
    if (props.runOnce()) {
      return;
    }
    Duration interval = interval(props);
    LOG.info("scan interval {}", interval);
    // Fixed delay: the next pass starts one interval after the previous one ENDED.
    registrar.addFixedDelayTask(this::pass, interval);
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
