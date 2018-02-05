/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark.metrics;

import com.appdynamics.extensions.yml.YmlReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Created by aditya.jagtiani on 5/16/17.
 */

public class RDDMetricsTest {
    private String applicationName;
    private List<JsonNode> rddFromApplication;
    private List<Map> rddMetricsFromCfg;

    @Before
    public void setup() throws IOException {
        applicationName = "App1";
        ObjectMapper mapper = new ObjectMapper();
        rddFromApplication = Lists.newArrayList();
        rddFromApplication.add(mapper.readValue(new File("src/test/resources/rdd.json"), JsonNode.class));
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/conf/config.yml"));
        Map allMetrics = (Map) config.get("metrics");
        rddMetricsFromCfg = (List) allMetrics.get("rdd");
    }

    @Test
    public void populateMetricsTest() throws IOException {
        RDDMetrics rddMetrics = new RDDMetrics(applicationName, rddFromApplication, rddMetricsFromCfg);
        Map<String, BigDecimal> metrics = rddMetrics.populateMetrics();
        Assert.assertTrue(metrics.size() == 2);
        Assert.assertTrue(metrics.containsKey("Applications|App1|Jobs|rdd1|numPartitions"));
        Assert.assertTrue(metrics.containsKey("Applications|App1|Jobs|rdd1|numCachedPartitions"));
        Assert.assertTrue(metrics.get("Applications|App1|Jobs|rdd1|numPartitions").equals(new BigDecimal(2)));
        Assert.assertTrue(metrics.get("Applications|App1|Jobs|rdd1|numCachedPartitions").equals(new BigDecimal(1)));
    }
}

