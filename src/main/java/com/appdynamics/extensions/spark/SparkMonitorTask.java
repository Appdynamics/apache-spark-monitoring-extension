/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Map;

public class SparkMonitorTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SparkMonitorTask.class);
    private MonitorConfiguration configuration;
    private Map server;

    public SparkMonitorTask(MonitorConfiguration configuration, Map server) {
        this.configuration = configuration;
        this.server = server;
    }

    public void run() {
        try {
            populateAndPrintStats();
            logger.info("Spark Metric Upload Complete");
        } catch (Exception ex) {
            configuration.getMetricWriter().registerError(ex.getMessage(), ex);
            logger.error("Error while running the task", ex);
        }
    }

    private void populateAndPrintStats() {
        try {
            SparkStats sparkStats = new SparkStats(configuration, server);
            Map<String, BigDecimal> sparkMetrics = sparkStats.populateMetrics();
            sparkStats.printMetrics(sparkMetrics);
            logger.info("Successfully completed the Spark Monitoring Task for " + server.get("name").toString());
        } catch (Exception ex) {
            logger.error("Spark Monitoring Task Failed", ex.getMessage());
        }
    }
}
