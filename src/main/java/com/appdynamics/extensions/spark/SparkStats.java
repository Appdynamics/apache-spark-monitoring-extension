package com.appdynamics.extensions.spark;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.spark.metrics.ApplicationMetricHandler;
import com.google.common.collect.Maps;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by aditya.jagtiani on 5/9/17.
 */

class SparkStats {
    private MonitorConfiguration configuration;
    private String serverUrl;

    SparkStats(MonitorConfiguration configuration, Map server) {
        this.configuration = configuration;
        this.serverUrl = UrlBuilder.fromYmlServerConfig(server).build();
    }

    Map<String, BigDecimal> populateMetrics() throws IOException {
        Map<String, BigDecimal> sparkMetrics = Maps.newHashMap();
        CloseableHttpClient httpClient = configuration.getHttpClient();
        Map<String, ?> config = configuration.getConfigYml();
        ApplicationMetricHandler applicationMetricHandler = new ApplicationMetricHandler(serverUrl, httpClient);
        sparkMetrics.putAll(applicationMetricHandler.populateStats((Map) config.get("metrics")));
        return sparkMetrics;
    }
}
