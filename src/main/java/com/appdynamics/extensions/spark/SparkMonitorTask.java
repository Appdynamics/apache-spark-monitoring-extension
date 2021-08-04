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
import com.appdynamics.extensions.spark.metrics.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

import static com.appdynamics.extensions.spark.helpers.Constants.*;
import static com.appdynamics.extensions.spark.helpers.SparkUtils.*;

public class SparkMonitorTask implements AMonitorTaskRunnable {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(SparkMonitorTask.class);

    private MetricWriteHelper metricWriteHelper;
    private MonitorContextConfiguration contextConfiguration;
    private Map server;
    private String metricPrefix;
    private String serverUrl;
    private BigInteger heartbeatValue = BigInteger.ZERO;


    public SparkMonitorTask(MetricWriteHelper metricWriteHelper, MonitorContextConfiguration contextConfiguration, Map server){
        this.metricWriteHelper = metricWriteHelper;
        this.contextConfiguration = contextConfiguration;
        this.server = server;
        this.serverUrl = UrlBuilder.fromYmlServerConfig(server).build();
        this.metricPrefix = contextConfiguration.getMetricPrefix()+METRIC_SEPARATOR+this.server.get(DISPLAY_NAME)+ METRIC_SEPARATOR;
    }


    public void run() {
        try {
            Phaser phaser = new Phaser();
            phaser.register();
            String baseUrl = buildUrl(serverUrl, CONTEXT_ROOT);
            List<JsonNode> applications = fetchSparkEntity(contextConfiguration.getContext().getHttpClient(), baseUrl);
            if(validateAppResponse(applications)){
                heartbeatValue = BigInteger.ONE;
                for(JsonNode application: applications){
                    String applicationName = application.findValue("name").asText();
                    String applicationId = application.findValue("id").asText();
                    ApplicationMetricHandler applicationMetricHandlerTask = new ApplicationMetricHandler(metricWriteHelper, contextConfiguration.getContext().getHttpClient(), (Map) contextConfiguration.getConfigYml().get(METRICS), metricPrefix, baseUrl, applicationName, applicationId, phaser);
                    contextConfiguration.getContext().getExecutorService().execute("ApplicationMetricHandlerTask",applicationMetricHandlerTask);
                }
            }
            phaser.arriveAndAwaitAdvance();
            logger.debug("SparkMonitorTask completed for server "+server.get(DISPLAY_NAME));
        } catch (Exception ex) {
            logger.error("Error while running the task for server "+server.get(DISPLAY_NAME), ex);
        }finally{
            metricWriteHelper.printMetric(metricPrefix + HEARTBEAT, heartbeatValue.toString(), "AVERAGE", "AVERAGE", "INDIVIDUAL" );
        }
    }

    public void onTaskComplete() {
        logger.info("Completed spark monitor task for server "+server.get(DISPLAY_NAME));
    }
}
