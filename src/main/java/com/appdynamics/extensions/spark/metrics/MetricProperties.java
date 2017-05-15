package com.appdynamics.extensions.spark.metrics;

/**
 * Created by aditya.jagtiani on 5/15/17.
 */
public class MetricProperties {
    private String metricName;
    private String alias;
    private String aggregationType;
    private String timeRollupType;
    private String clusterRollupType;
    private String multiplier;
    private String metricPath;

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public String getAggregationType() {
        return aggregationType;
    }

    public void setAggregationType(String aggregationType) {
        this.aggregationType = aggregationType;
    }

    public String getTimeRollupType() {
        return timeRollupType;
    }

    public void setTimeRollupType(String timeRollupType) {
        this.timeRollupType = timeRollupType;
    }

    public String getClusterRollupType() {
        return clusterRollupType;
    }

    public void setClusterRollupType(String clusterRollupType) {
        this.clusterRollupType = clusterRollupType;
    }

    public String getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(String multiplier) {
        this.multiplier = multiplier;
    }

    public void setMetricPath(String metricPath) {
        this.metricPath = metricPath;
    }
    public String getMetricPath() {
        return metricPath;
    }
}
