package com.appdynamics.extensions.spark.metrics;

import com.appdynamics.extensions.spark.helpers.Constants;
import com.appdynamics.extensions.spark.helpers.HttpHelper;
import com.appdynamics.extensions.spark.helpers.SparkUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.spark.helpers.Constants.*;


/**
 * Created by aditya.jagtiani on 5/9/17.
 */

public class ApplicationMetricHandler {
    private CloseableHttpClient httpClient;
    private String serverUrl;

    public ApplicationMetricHandler(String serverUrl, CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
        this.serverUrl = serverUrl;
    }

    public Map<String, BigDecimal> populateStats(Map<String, Map> includedMetrics) throws IOException {
        Map<String, BigDecimal> appMetrics = Maps.newHashMap();
        String baseUrl = buildUrl(serverUrl, Constants.CONTEXT_ROOT);
        List<JsonNode> applications = fetchSparkEntity(baseUrl);

        for (JsonNode application : applications) {
            String applicationName = application.findValue("name").asText();
            String applicationId = application.findValue("id").asText();

            List<JsonNode> jobsForCurrentApp = fetchSparkEntity(buildUrl(baseUrl, applicationId + JOBS_ENDPOINT));
            List<JsonNode> stagesForCurrentApp = fetchSparkEntity(buildUrl(baseUrl, applicationId + STAGES_ENDPOINT));
            List<JsonNode> executorsForCurrentApp = fetchSparkEntity(buildUrl(baseUrl, applicationId + EXECUTOR_ENDPOINT));
            List<JsonNode> rddForCurrentApp = fetchSparkEntity(buildUrl(baseUrl, applicationId + RDD_ENDPOINT));

            appMetrics.putAll(new JobMetrics(applicationName, jobsForCurrentApp, (List) includedMetrics.get("jobs")).populateMetrics());
            appMetrics.putAll(new ExecutorMetrics(applicationName, executorsForCurrentApp, (List) includedMetrics.get("executors")).populateMetrics());
            appMetrics.putAll(new StageMetrics(applicationName, stagesForCurrentApp, (List) includedMetrics.get("stages")).populateMetrics());
            appMetrics.putAll(new RDDMetrics(applicationName, rddForCurrentApp, (List) includedMetrics.get("rdd")).populateMetrics());
        }
        return appMetrics;
    }

    private String buildUrl(String serverUrl, String suffix) {
        StringBuilder url = new StringBuilder(serverUrl);
        return url.append(suffix).toString();
    }

    private List<JsonNode> fetchSparkEntity(String url) throws IOException {
        CloseableHttpResponse response = HttpHelper.doGet(httpClient, url);
        List<JsonNode> entities = Lists.newArrayList();
        JsonNode jsonNode = SparkUtils.getJsonNode(response);
        for (JsonNode node : jsonNode) {
            entities.add(node);
        }
        return entities;
    }
}
