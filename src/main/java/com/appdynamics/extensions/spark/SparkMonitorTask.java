/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark;

/**
 * abhishek.saxena on 7/8/20.
 */

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.spark.metrics.*;
import com.google.common.collect.Lists;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.spark.helpers.Constants.*;

public class SparkMonitorTask implements AMonitorTaskRunnable {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(SparkMonitorTask.class);

    private MetricWriteHelper metricWriteHelper;
    private MonitorContextConfiguration contextConfiguration;
    private Map server;
    private String metricPrefix;
    private String serverURL;
    private BigInteger heartbeatvalue = BigInteger.ZERO;


    public SparkMonitorTask(MetricWriteHelper metricWriteHelper, MonitorContextConfiguration contextConfiguration, Map server){
        this.metricWriteHelper = metricWriteHelper;
        this.contextConfiguration = contextConfiguration;
        this.server = server;
        this.serverURL = UrlBuilder.fromYmlServerConfig(server).build();
        this.metricPrefix = contextConfiguration.getMetricPrefix()+METRIC_SEPARATOR+this.server.get(DISPLAY_NAME)+ METRIC_SEPARATOR;
    }


    public void onTaskComplete() {
        logger.info("Completed spark monitor task for server "+server.get(DISPLAY_NAME));
    }


    public void run() {
        List<Metric> sparkMetrics = Lists.newArrayList();
        try {
            sparkMetrics = populateMetrics();
        } catch (Exception ex) {
            logger.error("Error while running the task", ex);
        }finally{
            sparkMetrics.add(new Metric(HEARTBEAT, heartbeatvalue.toString(), metricPrefix + HEARTBEAT));
            metricWriteHelper.transformAndPrintMetrics(sparkMetrics);
        }
    }

    private List<Metric> populateMetrics() {
        List<Metric> sparkMetrics = Lists.newArrayList();
        CloseableHttpClient httpClient = contextConfiguration.getContext().getHttpClient();
        Map<String,?> config = contextConfiguration.getConfigYml();
        ApplicationMetricHandler applicationMetricHandler = new ApplicationMetricHandler(serverURL,httpClient,metricPrefix);
        sparkMetrics.addAll(applicationMetricHandler.populateStats((Map) config.get(METRICS)));
        heartbeatvalue = applicationMetricHandler.getHeartbeatValue();
        return sparkMetrics;
    }
}
