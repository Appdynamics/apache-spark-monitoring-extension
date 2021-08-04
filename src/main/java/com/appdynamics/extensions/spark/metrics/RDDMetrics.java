/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark.metrics;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.spark.helpers.SparkUtils;
import com.appdynamics.extensions.util.MetricPathUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.spark.helpers.Constants.*;

/**
 * Created by aditya.jagtiani on 5/9/17, abhishek.saxena on 7/8/20.
 */

class RDDMetrics {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(RDDMetrics.class);
    private String applicationName;
    private List<JsonNode> rddFromApplication;
    private List<Map> rddMetricsFromConfig;
    private String metricPrefix;
    private List<MetricProperties> sparkRddMetric;
    private ObjectMapper objectMapper = new ObjectMapper();

    RDDMetrics(String applicationName, List<JsonNode> rddFromApplication, List<Map> rddMetricsFromConfig, String metricPrefix) {
        this.applicationName = applicationName;
        this.rddFromApplication = rddFromApplication;
        this.rddMetricsFromConfig = rddMetricsFromConfig;
        this.metricPrefix = metricPrefix;
    }

    List<Metric> populateMetrics() {
        List<Metric> rddMetrics = Lists.newArrayList();
        if (SparkUtils.validateEntityInApplication(applicationName, rddFromApplication, RDD)) {
            try {
                String metricName;
                sparkRddMetric = Arrays.asList(objectMapper.convertValue(rddMetricsFromConfig, MetricProperties[].class));
                for (JsonNode rdd : rddFromApplication) {
                    String rddId = rdd.findValue("id").asText();
                    String currentRDDMetricPath = MetricPathUtils.buildMetricPath(metricPrefix, APPLICATIONS, applicationName, RDD, rddId);
                    logger.info("Fetching metrics for RDD: " + rddId + " in application: " + applicationName);
                    for (MetricProperties metrics : sparkRddMetric) {
                        metricName = metrics.getName();
                        if (rdd.findValue(metricName) != null) {
                            rddMetrics.add(new Metric(metricName, String.valueOf(rdd.findValue(metricName)), currentRDDMetricPath + METRIC_SEPARATOR + metrics.getAlias(), objectMapper.convertValue(metrics, Map.class)));
                        } else {
                            logger.debug("Metric :" + metricName + " not found for RDD : " + rddId + ". Please verify whether correct metric names have been entered in the config.yml");
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Error occurred while fetching metrics for rdd", ex);
            }
        }
        return rddMetrics;
    }
}

