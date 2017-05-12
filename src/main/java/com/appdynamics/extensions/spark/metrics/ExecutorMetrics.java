package com.appdynamics.extensions.spark.metrics;

import com.appdynamics.extensions.spark.helpers.SparkUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by aditya.jagtiani on 5/9/17.
 */

class ExecutorMetrics {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorMetrics.class);
    private static final String METRIC_SEPARATOR = "|";
    private String applicationName;
    private List<JsonNode> executorsFromApplication;
    private List<Map> executorMetricsFromConfig;

    ExecutorMetrics(String applicationName, List<JsonNode> executorsFromApplication, List<Map> executorMetricsFromConfig) {
        this.applicationName = applicationName;
        this.executorsFromApplication = executorsFromApplication;
        this.executorMetricsFromConfig = executorMetricsFromConfig;
    }

    Map<String, BigDecimal> populateMetrics() throws IOException {
        if (!isValidationSuccessful()) {
            return null;
        }

        Map<String, BigDecimal> executorMetrics = Maps.newHashMap();
        for (JsonNode executor : executorsFromApplication) {
            String executorId = executor.findValue("id").asText();
            String baseExecutorMetricPath = METRIC_SEPARATOR + "Applications" + METRIC_SEPARATOR + applicationName + METRIC_SEPARATOR + "Executors" + METRIC_SEPARATOR + executorId + METRIC_SEPARATOR;
            logger.info("Fetching metrics for executor :" + executorId + " in application: " + applicationName);
            for (Map metric : executorMetricsFromConfig) {
                Map.Entry<String, String> entry = (Map.Entry) metric.entrySet().iterator().next();
                if (executor.has(entry.getKey())) {
                    executorMetrics.put(baseExecutorMetricPath + entry.getValue(), SparkUtils.convertDoubleToBigDecimal(executor.findValue(entry.getKey()).asDouble()));
                } else {
                    logger.debug("Metric :" + entry.getKey() + " not found for job : " + executorId + ". Please verify whether correct metric names have been entered in the config.yml");
                }
            }
        }
        return executorMetrics;
    }

    private boolean isValidationSuccessful() {
        if (executorMetricsFromConfig == null || executorMetricsFromConfig.isEmpty()) {
            logger.error("No executor metrics configured in config.yml");
            return false;
        } else if (executorsFromApplication == null || executorsFromApplication.isEmpty()) {
            logger.error("No executors found for the current application");
            return false;
        }
        return true;
    }
}

