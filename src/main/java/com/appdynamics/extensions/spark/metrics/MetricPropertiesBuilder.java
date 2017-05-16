package com.appdynamics.extensions.spark.metrics;

import com.appdynamics.extensions.NumberUtils;
import com.google.common.collect.Maps;

import java.util.Map;

import static com.appdynamics.extensions.spark.helpers.Constants.*;

/**
 * Created by aditya.jagtiani on 5/15/17.
 */

public class MetricPropertiesBuilder {
    static Map<String, MetricProperties> metricPropertiesMap = Maps.newHashMap();

    static void buildMetricPropsMap(Map metricDetailsFromCfg, String metricName, String metricPath) {
        if (metricDetailsFromCfg == null || metricDetailsFromCfg.isEmpty()) {
            return;
        }
        MetricProperties metricProperties = new MetricProperties();
        Map<String, String> propsFromCfg = (Map) metricDetailsFromCfg.get(metricName);
        metricProperties.setMetricName(metricName);
        metricProperties.setMetricPath(metricPath);
        metricProperties.setMultiplier(validateMultiplier(propsFromCfg));
        metricProperties.setAlias(validateAlias(propsFromCfg, metricName));
        metricProperties.setAggregationType(validateAggregationType(propsFromCfg));
        metricProperties.setClusterRollupType(validateClusterRollupType(propsFromCfg));
        metricProperties.setTimeRollupType(validateTimeRollupType(propsFromCfg));
        metricPropertiesMap.put(metricPath + metricName, metricProperties);
    }

    private static String validateClusterRollupType(Map<String, String> propsFromCfg) {
        return !VALID_CLUSTER_ROLLUP_TYPES.contains(propsFromCfg.get("cluster")) ?
                DEFAULT_CLUSTER_ROLLUP_TYPE : propsFromCfg.get("cluster");
    }

    private static String validateAggregationType(Map<String, String> propsFromCfg) {
        return !VALID_AGGREGATION_TYPES.contains(propsFromCfg.get("aggregation")) ?
                DEFAULT_AGGREGATION_TYPE : propsFromCfg.get("aggregation");
    }

    private static String validateTimeRollupType(Map<String, String> propsFromCfg) {
        return !VALID_TIME_ROLLUP_TYPES.contains(propsFromCfg.get("time")) ?
                DEFAULT_TIME_ROLLUP_TYPE : propsFromCfg.get("time");
    }

    private static String validateAlias(Map<String, String> propsFromCfg, String metricName) {
        return (propsFromCfg.get("alias") == null || propsFromCfg.get("alias").isEmpty()) ?
                metricName : propsFromCfg.get("alias");
    }

    private static String validateMultiplier(Map<String, String> propsFromCfg) {
        return (propsFromCfg.get("multiplier") == null || propsFromCfg.get("multiplier").isEmpty() ||
                !NumberUtils.isNumber(propsFromCfg.get("multiplier"))) ? DEFAULT_MULTIPLIER : propsFromCfg.get("multiplier");
    }

    public static Map<String, MetricProperties> getMetricPropsMap() {
        return metricPropertiesMap;
    }
}
