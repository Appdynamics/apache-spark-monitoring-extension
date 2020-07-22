/*
 * Copyright 2020. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.spark.metrics;

import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.spark.helpers.Constants;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.spark.helpers.Constants.*;


/**
 * Created by aditya.jagtiani on 5/9/17, abhishek.saxena on 7/8/20.
 */

public class ApplicationMetricHandler {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(ApplicationMetricHandler.class);
    private CloseableHttpClient httpClient;
    private String serverUrl;
    private String metricPrefix;
    private BigInteger heartbeatvalue = BigInteger.ZERO;

    public ApplicationMetricHandler(String serverUrl, CloseableHttpClient httpClient,String metricPrefix) {
        this.httpClient = httpClient;
        this.serverUrl = serverUrl;
        this.metricPrefix = metricPrefix;
    }

    public List<Metric> populateStats(Map<String, Map> includedMetrics) {
        List<Metric> appMetrics = Lists.newArrayList();
        String baseUrl = buildUrl(serverUrl, Constants.CONTEXT_ROOT);
        List<JsonNode> applications = fetchSparkEntity(baseUrl);
        if(applications != null && applications.size()>0){
            heartbeatvalue = BigInteger.ONE;
        }

        for (JsonNode application : applications) {
            String applicationName = application.findValue("name").asText();
            String applicationId = application.findValue("id").asText();

            List<JsonNode> jobsForCurrentApp = fetchSparkEntity(buildUrl(baseUrl, applicationId + JOBS_ENDPOINT));
            List<JsonNode> stagesForCurrentApp = fetchSparkEntity(buildUrl(baseUrl, applicationId + STAGES_ENDPOINT));
            List<JsonNode> executorsForCurrentApp = fetchSparkEntity(buildUrl(baseUrl, applicationId + EXECUTOR_ENDPOINT));
            List<JsonNode> rddForCurrentApp = fetchSparkEntity(buildUrl(baseUrl, applicationId + RDD_ENDPOINT));

            appMetrics.addAll(new JobMetrics(applicationName, jobsForCurrentApp, (List) includedMetrics.get("jobs"),metricPrefix).populateMetrics());
            appMetrics.addAll(new ExecutorMetrics(applicationName, executorsForCurrentApp, (List) includedMetrics.get("executors"), metricPrefix).populateMetrics());
            appMetrics.addAll(new StageMetrics(applicationName, stagesForCurrentApp, (List) includedMetrics.get("stages"),metricPrefix).populateMetrics());
            appMetrics.addAll(new RDDMetrics(applicationName, rddForCurrentApp, (List) includedMetrics.get("rdd"),metricPrefix).populateMetrics());
        }
        return appMetrics;
    }

    private String buildUrl(String serverUrl, String suffix) {
        StringBuilder url = new StringBuilder(serverUrl);
        return url.append(suffix).toString();
    }

    private List<JsonNode> fetchSparkEntity(String url) {
        List<JsonNode> entities = Lists.newArrayList();
        try {
            JsonNode jsonNode = HttpClientUtils.getResponseAsJson(httpClient,url,JsonNode.class);
            for (JsonNode node : jsonNode) {
                entities.add(node);
            }
        }
        catch(Exception ex) {
            logger.error("Error while fetching spark entity from url : " + url);
        }
        return entities;
    }

    public BigInteger getHeartbeatValue(){
        return heartbeatvalue;
    }

}
