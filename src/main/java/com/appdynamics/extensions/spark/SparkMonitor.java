/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */


package com.appdynamics.extensions.spark;

/**
 * Created by aditya.jagtiani on 5/3/17.
 */

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class SparkMonitor  extends AManagedMonitor {
    private static final Logger logger = LoggerFactory.getLogger(SparkMonitor.class);
    private MonitorConfiguration configuration;

    public SparkMonitor() {
        logger.info("Using Spark Monitor Version [" + getImplementationVersion() + "]");
    }

    protected void initialize(Map<String, String> argsMap) {
        if (configuration == null) {
            MetricWriteHelper metricWriter = MetricWriteHelperFactory.create(this);
            MonitorConfiguration conf = new MonitorConfiguration("Custom Metrics|Spark|", new TaskRunner(), metricWriter);
            final String configFilePath = argsMap.get("config-file");
            conf.setConfigYml(configFilePath);
            conf.checkIfInitialized(MonitorConfiguration.ConfItem.METRIC_PREFIX, MonitorConfiguration.ConfItem.CONFIG_YML, MonitorConfiguration.ConfItem.HTTP_CLIENT
                    , MonitorConfiguration.ConfItem.EXECUTOR_SERVICE);
            this.configuration = conf;
        }
    }

    public TaskOutput execute(Map<String, String> map, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
        logger.debug("The raw arguments are {}", map);
        try {
            initialize(map);
            configuration.executeTask();
        } catch (Exception ex) {
            if (configuration != null && configuration.getMetricWriter() != null) {
                configuration.getMetricWriter().registerError(ex.getMessage(), ex);
            }
        }
        return null;
    }

    private class TaskRunner implements Runnable {

        public void run() {
            Map<String, ?> config = configuration.getConfigYml();
            List<Map> servers = (List) config.get("servers");
            if (servers != null && !servers.isEmpty()) {
                for (Map server : servers) {
                    SparkMonitorTask task = new SparkMonitorTask(configuration, server);
                    configuration.getExecutorService().execute(task);
                }
            } else {
                logger.error("Error encountered while running the Spark Monitoring task");
            }
        }
    }

    private static String getImplementationVersion() {
        return SparkMonitor.class.getPackage().getImplementationVersion();
    }
}
