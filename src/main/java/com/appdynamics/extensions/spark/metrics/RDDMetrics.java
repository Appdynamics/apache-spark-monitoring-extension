/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

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

class RDDMetrics {

    private static final Logger logger = LoggerFactory.getLogger(RDDMetrics.class);
    private static final String ENTITY_TYPE = "RDD";
    private String applicationName;
    private List<JsonNode> rddFromApplication;
    private List<Map> rddMetricsFromConfig;

    RDDMetrics(String applicationName, List<JsonNode> rddFromApplication, List<Map> rddMetricsFromConfig) {
        this.applicationName = applicationName;
        this.rddFromApplication = rddFromApplication;
        this.rddMetricsFromConfig = rddMetricsFromConfig;
    }

    Map<String, BigDecimal> populateMetrics() throws IOException {
        if (!SparkUtils.isValidationSuccessful(rddMetricsFromConfig, rddFromApplication, ENTITY_TYPE)) {
            return Maps.newHashMap();
        }
        Map<String, BigDecimal> rddMetrics = Maps.newHashMap();
        for (JsonNode rdd : rddFromApplication) {
            String rddId = rdd.findValue("id").asText();
            String currentRDDMetricPath = "Applications" + METRIC_SEPARATOR + applicationName + METRIC_SEPARATOR + "Jobs" + METRIC_SEPARATOR + rddId + METRIC_SEPARATOR;
            logger.info("Fetching metrics for RDD: " + rddId + " in application: " + applicationName);
            for (Map metric : rddMetricsFromConfig) {
                Map.Entry<String, String> entry = (Map.Entry) metric.entrySet().iterator().next();
                String metricName = entry.getKey();
                if (rdd.findValue(metricName) != null) {
                    rddMetrics.put(currentRDDMetricPath + metricName, SparkUtils.convertDoubleToBigDecimal(rdd.findValue(metricName).asDouble()));
                    if (entry.getValue() != null) {
                        MetricPropertiesBuilder.buildMetricPropsMap(metric, metricName, currentRDDMetricPath);
                    }
                } else {
                    logger.debug("Metric :" + metricName + " not found for RDD : " + rddId + ". Please verify whether correct metric names have been entered in the config.yml");
                }
            }
        }
        return rddMetrics;
    }
}

