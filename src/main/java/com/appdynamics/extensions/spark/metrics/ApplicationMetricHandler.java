/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark.metrics;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

import static com.appdynamics.extensions.spark.helpers.Constants.*;
import static com.appdynamics.extensions.spark.helpers.SparkUtils.*;


/**
 * Created by aditya.jagtiani on 5/9/17, abhishek.saxena on 7/8/20.
 */

public class ApplicationMetricHandler implements Runnable {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ApplicationMetricHandler.class);
    private MetricWriteHelper metricWriteHelper;
    private CloseableHttpClient httpClient;
    private Map includedMetricsFromConfig;
    private String metricPrefix;
    private String serverUrl;
    private String applicationName;
    private String applicationId;
    private Phaser phaser;
    private BigInteger heartbeatvalue = BigInteger.ZERO;

    public ApplicationMetricHandler(MetricWriteHelper metricWriteHelper, CloseableHttpClient httpClient, Map includedMetricsFromConfig, String metricPrefix, String serverUrl, String applicationName, String applicationId, Phaser phaser) {
        this.metricWriteHelper = metricWriteHelper;
        this.httpClient = httpClient;
        this.includedMetricsFromConfig = includedMetricsFromConfig;
        this.metricPrefix = metricPrefix;
        this.serverUrl = serverUrl;
        this.applicationName = applicationName;
        this.applicationId = applicationId;
        this.phaser = phaser;
        this.phaser.register();
    }

    @Override
    public void run() {
        try{
            populateStats();
        } catch(Exception e){
            logger.error("Error occurred while fetching metrics for Application "+applicationName,e);
        } finally{
            logger.debug("ApplicationMetricHandler phaser arrived for application "+applicationName+" for server ");
            phaser.arriveAndDeregister();
        }
    }

    public void populateStats() {
        List<Metric> appMetrics = Lists.newArrayList();
            if(validateMetricsInConfig((List) includedMetricsFromConfig.get("jobs"),JOBS)){
                List<JsonNode> jobsForCurrentApp = fetchSparkEntity(httpClient, buildUrl(serverUrl, applicationId + JOBS_ENDPOINT));
                appMetrics.addAll(new JobMetrics(applicationName, jobsForCurrentApp, (List) includedMetricsFromConfig.get("jobs"),metricPrefix).populateMetrics());
            }
            if(validateMetricsInConfig((List) includedMetricsFromConfig.get("executors"),EXECUTORS)){
                List<JsonNode> executorsForCurrentApp = fetchSparkEntity(httpClient, buildUrl(serverUrl, applicationId + EXECUTOR_ENDPOINT));
                appMetrics.addAll(new ExecutorMetrics(applicationName, executorsForCurrentApp, (List) includedMetricsFromConfig.get("executors"), metricPrefix).populateMetrics());
            }
            if(validateMetricsInConfig((List) includedMetricsFromConfig.get("stages"),STAGES)){
                List<JsonNode> stagesForCurrentApp = fetchSparkEntity(httpClient, buildUrl(serverUrl, applicationId + STAGES_ENDPOINT));
                appMetrics.addAll(new StageMetrics(applicationName, stagesForCurrentApp, (List) includedMetricsFromConfig.get("stages"),metricPrefix).populateMetrics());
            }
            if(validateMetricsInConfig((List) includedMetricsFromConfig.get("rdd"),RDD)){
                List<JsonNode> rddForCurrentApp = fetchSparkEntity(httpClient, buildUrl(serverUrl, applicationId + RDD_ENDPOINT));
                appMetrics.addAll(new RDDMetrics(applicationName, rddForCurrentApp, (List) includedMetricsFromConfig.get("rdd"),metricPrefix).populateMetrics());
            }
        metricWriteHelper.transformAndPrintMetrics(appMetrics);
    }
}
