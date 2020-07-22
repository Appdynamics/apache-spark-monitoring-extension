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

class StageMetrics {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(StageMetrics.class);
    private String applicationName;
    private List<JsonNode> stagesFromApplication;
    private List<Map> stageMetricsFromConfig;
    private String metricPrefix;
    private List<MetricProperties> sparkStageMetric;
    private ObjectMapper objectMapper = new ObjectMapper();

    StageMetrics(String applicationName, List<JsonNode> stagesFromApplication, List<Map> stageMetricsFromConfig, String metricPrefix) {
        this.applicationName = applicationName;
        this.stagesFromApplication = stagesFromApplication;
        this.stageMetricsFromConfig = stageMetricsFromConfig;
        this.metricPrefix = metricPrefix;
    }

    List<Metric> populateMetrics() {
        List<Metric> stageMetrics = Lists.newArrayList();
        if (SparkUtils.isValidationSuccessful(stageMetricsFromConfig, stagesFromApplication, STAGES)) {
            try {
                String metricName;
                sparkStageMetric = Arrays.asList(objectMapper.convertValue(stageMetricsFromConfig, MetricProperties[].class));
                for (JsonNode stage : stagesFromApplication) {
                    String stageId = stage.findValue("stageId").asText();
                    String stageName = stage.findValue("name").asText();
                    String currentStageMetricPath = MetricPathUtils.buildMetricPath(metricPrefix, APPLICATIONS, applicationName, STAGES, stageId, stageName);
                    logger.info("Fetching metrics for stage :" + stageId + " in application: " + applicationName);
                    for (MetricProperties metrics : sparkStageMetric) {
                        metricName = metrics.getName();
                        if (stage.findValue(metricName) != null) {
                            stageMetrics.add(new Metric(metricName, String.valueOf(stage.findValue(metricName)), currentStageMetricPath + METRIC_SEPARATOR + metrics.getAlias(), objectMapper.convertValue(metrics, Map.class)));
                        } else {
                            logger.debug("Metric :" + metricName + " not found for stage : " + stageId + ". Please verify whether correct metric names have been entered in the config.yml");
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error("Error occurred while fetching metrics for stages", ex);
            }
        }
        return stageMetrics;
    }
}

