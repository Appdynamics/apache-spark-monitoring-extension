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

class RDDMetrics {

    private static final Logger logger = LoggerFactory.getLogger(RDDMetrics.class);
    private static final String METRIC_SEPARATOR = "|";
    private String applicationName;
    private List<JsonNode> rddFromApplication;
    private List<Map> rddMetricsFromConfig;

    RDDMetrics(String applicationName, List<JsonNode> rddFromApplication, List<Map> rddMetricsFromConfig) {
        this.applicationName = applicationName;
        this.rddFromApplication = rddFromApplication;
        this.rddMetricsFromConfig = rddMetricsFromConfig;
    }

    Map<String, BigDecimal> populateMetrics() throws IOException {
        if (!isValidationSuccessful()) {
            return null;
        }

        Map<String, BigDecimal> jobMetrics = Maps.newHashMap();
        for (JsonNode rdd : rddFromApplication) {
            String rddId = rdd.findValue("id").asText();
            String baseJobMetricPath = METRIC_SEPARATOR + "Applications" + METRIC_SEPARATOR + applicationName + METRIC_SEPARATOR + "Jobs" + METRIC_SEPARATOR + rddId + METRIC_SEPARATOR;
            logger.info("Fetching metrics for job " + rddId + ": " + rddId + " in application: " + applicationName);
            for (Map metric : rddMetricsFromConfig) {
                Map.Entry<String, String> entry = (Map.Entry) metric.entrySet().iterator().next();
                if (rdd.has(entry.getKey())) {
                    jobMetrics.put(baseJobMetricPath + entry.getValue(), SparkUtils.convertDoubleToBigDecimal(rdd.findValue(entry.getKey()).asDouble()));
                } else {
                    logger.debug("Metric :" + entry.getKey() + " not found for rdd : " + rddId + ". Please verify whether correct metric names have been entered in the config.yml");
                }
            }
        }
        return jobMetrics;
    }

    private boolean isValidationSuccessful() {
        if (rddMetricsFromConfig == null || rddMetricsFromConfig.isEmpty()) {
            logger.error("No rdd metrics configured in config.yml");
            return false;
        } else if (rddFromApplication == null || rddFromApplication.isEmpty()) {
            logger.error("No rdd found for the current application");
            return false;
        }
        return true;
    }
}

