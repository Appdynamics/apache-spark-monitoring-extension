/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.spark.metrics.MetricPropertiesBuilder;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Created by aditya.jagtiani on 5/22/17.
 */
public class SparkStatsTest {
    private Map server;
    private MonitorConfiguration configuration = mock(MonitorConfiguration.class);
    private MetricWriteHelper metricWriter = mock(MetricWriteHelper.class);
    private Map<String, BigDecimal> sparkMetrics = Maps.newHashMap();
    private SparkStats sparkStats;

    @Before
    public void setup() {
        buildMetricPropsMapForTest();
        when(configuration.getMetricPrefix()).thenReturn("metricPrefix");
        when(configuration.getMetricWriter()).thenReturn(metricWriter);
        sparkStats = new SparkStats(configuration, server);
    }

    @Test
    public void printMetricsTest_whenDeltaIsTrue() {
        buildMetricMapForDelta(new BigDecimal(5));
        sparkStats.printMetrics(sparkMetrics);
        verify(metricWriter, never()).printMetric(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        buildMetricMapForDelta(new BigDecimal(10));
        sparkStats.printMetrics(sparkMetrics);
        verify(metricWriter, times(1)).printMetric(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    private void buildMetricPropsMapForTest() {
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/conf/config_delta.yml"));
        Map allMetrics = (Map) config.get("metrics");
        List<Map> servers = (List) config.get("servers");
        server = servers.get(0);
        List<Map> jobMetricsFromCfg = (List) allMetrics.get("jobs");
        MetricPropertiesBuilder.buildMetricPropsMap(jobMetricsFromCfg.get(0), "numTasks", "metricPrefix|");
    }

    private void buildMetricMapForDelta(BigDecimal value) {
        sparkMetrics.put("metricPrefix|numTasks", value);
    }
}
