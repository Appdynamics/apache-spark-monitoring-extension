package com.appdynamics.extensions.spark.metrics;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Created by aditya.jagtiani on 5/15/17.
 */
public class MetricPropertiesBuilder {

    static Map<String, MetricProperties> metricPropertiesMap = Maps.newHashMap();

    public static void buildMetricPropsMap(Map metric, MetricProperties metricProperties) {
        if (metric == null || metric.isEmpty()) {
            return;
        }

        String metricName = metricProperties.getMetricName();
        String metricPath = metricProperties.getMetricPath();
        Map<String, String> propsFromCfg = (Map) metric.get(metricName);
        metricProperties.setAlias(propsFromCfg.get("alias"));
        metricProperties.setMultiplier(propsFromCfg.get("multiplier"));
        metricProperties.setClusterRollupType(propsFromCfg.get("cluster"));
        metricProperties.setAggregationType(propsFromCfg.get("aggregation"));
        metricProperties.setTimeRollupType(propsFromCfg.get("time"));
        metricPropertiesMap.put(metricPath + metricName, metricProperties);
    }

    public static Map<String, MetricProperties> getMetricPropsMap() {
        return metricPropertiesMap;
    }
}
