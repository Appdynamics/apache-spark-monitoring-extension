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

class ExecutorMetrics {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ExecutorMetrics.class);
    private String applicationName;
    private List<JsonNode> executorsFromApplication;
    private List<Map> executorMetricsFromConfig;
    private String metricPrefix;
    private List<MetricProperties> sparkExecutorMetric;
    private ObjectMapper objectMapper = new ObjectMapper();

    ExecutorMetrics(String applicationName, List<JsonNode> executorsFromApplication, List<Map> executorMetricsFromConfig, String metricPrefix) {
        this.applicationName = applicationName;
        this.executorsFromApplication = executorsFromApplication;
        this.executorMetricsFromConfig = executorMetricsFromConfig;
        this.metricPrefix = metricPrefix;

    }

    protected List<Metric> populateMetrics() {
        List<Metric> executorMetrics = Lists.newArrayList();
        if (SparkUtils.validateEntityInApplication(applicationName, executorsFromApplication, EXECUTORS)) {
            try {
                String metricName;
                sparkExecutorMetric = Arrays.asList(objectMapper.convertValue(executorMetricsFromConfig, MetricProperties[].class));
                for (JsonNode executor : executorsFromApplication) {
                    String executorId = executor.findValue("id").asText();
                    String currentExecutorMetricPath = MetricPathUtils.buildMetricPath(metricPrefix, APPLICATIONS, applicationName, EXECUTORS, executorId);
                    logger.info("Fetching metrics for executor :" + executorId + " in application: " + applicationName);
                    for (MetricProperties metrics : sparkExecutorMetric) {
                        metricName = metrics.getName();
                        if (executor.findValue(metricName) != null) {
                            executorMetrics.add(new Metric(metricName, String.valueOf(executor.findValue(metricName)), currentExecutorMetricPath + METRIC_SEPARATOR + metrics.getAlias(), objectMapper.convertValue(metrics, Map.class)));
                        } else {
                            logger.debug("Metric :" + metricName + " not found for executor : " + executorId + ". Please verify whether correct metric names have been entered in the config.yml");
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Error occurred while fetching metrics for Executors", ex);
            }
        }
        return executorMetrics;
    }
}

