/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */


package com.appdynamics.extensions.spark;

/**
 * Created by aditya.jagtiani on 5/3/17, abhishek.saxena on 7/8/20.
 */

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.util.AssertUtils;
import com.google.common.collect.Maps;

import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.spark.helpers.Constants.*;


public class SparkMonitor  extends ABaseMonitor {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(SparkMonitor.class);

    private static final String METRIC_PREFIX = DEFAULT_METRIC_PREFIX;

    private MonitorContextConfiguration contextConfiguration;
    private Map<String,?> configYml = Maps.newHashMap();

    @Override
    protected String getDefaultMetricPrefix() {
        return METRIC_PREFIX;
    }


    @Override
    public String getMonitorName() {
        return MONITOR_NAME;
    }

    @Override
    protected void initializeMoreStuff(Map<String, String> args) {
        contextConfiguration = getContextConfiguration();
        configYml = contextConfiguration.getConfigYml();
        AssertUtils.assertNotNull(configYml,"The config.yml is not available");
    }

    @Override
    protected void doRun(TasksExecutionServiceProvider tasksExecutionServiceProvider) {
        List<Map<String,?>> servers = (List<Map<String, ?>>) configYml.get(SERVERS);

        for (Map server: servers){
            String name = (String) server.get(DISPLAY_NAME);
            AssertUtils.assertNotNull(server, "The server arguments cannot be empty");
            AssertUtils.assertNotNull(server.get(DISPLAY_NAME),"Display name cannot be empty");
            logger.info("Starting monitoring task for server "+name);
            SparkMonitorTask task = new SparkMonitorTask(tasksExecutionServiceProvider.getMetricWriteHelper(), contextConfiguration, server);
            tasksExecutionServiceProvider.submit(name,task);
        }
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        return (List<Map<String, ?>>) configYml.get(SERVERS);
    }
}
