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

import static com.appdynamics.extensions.spark.helpers.Constants.METRIC_SEPARATOR;

/**
 * Created by aditya.jagtiani on 5/9/17.
 */

class ExecutorMetrics {
    private static final Logger logger = LoggerFactory.getLogger(ExecutorMetrics.class);
    private static final String ENTITY_TYPE = "EXECUTORS";
    private String applicationName;
    private List<JsonNode> executorsFromApplication;
    private List<Map> executorMetricsFromConfig;

    ExecutorMetrics(String applicationName, List<JsonNode> executorsFromApplication, List<Map> executorMetricsFromConfig) {
        this.applicationName = applicationName;
        this.executorsFromApplication = executorsFromApplication;
        this.executorMetricsFromConfig = executorMetricsFromConfig;
    }

    Map<String, BigDecimal> populateMetrics() throws IOException {
        if (!SparkUtils.isValidationSuccessful(executorMetricsFromConfig, executorsFromApplication, ENTITY_TYPE)) {
            return Maps.newHashMap();
        }
        Map<String, BigDecimal> executorMetrics = Maps.newHashMap();
        for (JsonNode executor : executorsFromApplication) {
            String executorId = executor.findValue("id").asText();
            String currentExecutorMetricPath = "Applications" + METRIC_SEPARATOR + applicationName + METRIC_SEPARATOR + "Executors" + METRIC_SEPARATOR + executorId + METRIC_SEPARATOR;
            logger.info("Fetching metrics for executor :" + executorId + " in application: " + applicationName);
            for (Map metric : executorMetricsFromConfig) {
                Map.Entry<String, String> entry = (Map.Entry) metric.entrySet().iterator().next();
                String metricName = entry.getKey();
                if (executor.findValue(metricName) != null) {
                    executorMetrics.put(currentExecutorMetricPath + metricName, SparkUtils.convertDoubleToBigDecimal(executor.findValue(metricName).asDouble()));
                    if (entry.getValue() != null) {
                        MetricPropertiesBuilder.buildMetricPropsMap(metric, metricName, currentExecutorMetricPath);
                    }
                } else {
                    logger.debug("Metric :" + metricName + " not found for executor : " + executorId + ". Please verify whether correct metric names have been entered in the config.yml");
                }
            }
        }
        return executorMetrics;
    }
}

