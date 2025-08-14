package com.mb.frontendService.config;

import feign.RetryableException;
import feign.Retryer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomRetryer implements Retryer {

    private static final Logger logger = LoggerFactory.getLogger(CustomRetryer.class);

    private final int maxAttempts;
    private final long period;
    private final long maxPeriod;
    int attempt;

    public CustomRetryer() {
        this(100, 1000, 3); // period, maxPeriod, maxAttempts
    }

    public CustomRetryer(long period, long maxPeriod, int maxAttempts) {
        this.period = period;
        this.maxPeriod = maxPeriod;
        this.maxAttempts = maxAttempts;
        this.attempt = 1;
    }

    @Override
    public void continueOrPropagate(RetryableException e) {
        if (attempt++ >= maxAttempts) {
            logger.error("Max retry attempts ({}) reached for service call", maxAttempts);
            throw e;
        }

        logger.warn("Retrying service call, attempt {}/{}", attempt, maxAttempts);

        try {
            long sleepTime = Math.min(period * attempt, maxPeriod);
            Thread.sleep(sleepTime);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    @Override
    public Retryer clone() {
        return new CustomRetryer(period, maxPeriod, maxAttempts);
    }
}
