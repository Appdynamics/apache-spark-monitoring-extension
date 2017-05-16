package com.appdynamics.extensions.spark.helpers;

import java.util.Arrays;
import java.util.List;

/**
 * Created by aditya.jagtiani on 5/16/17.
 */

public class Constants {
    public static final String METRIC_SEPARATOR = "|";
    public static final String CONTEXT_ROOT = "/api/v1/applications/";
    public static final String JOBS_ENDPOINT = "/jobs/";
    public static final String EXECUTOR_ENDPOINT = "/executors/";
    public static final String STAGES_ENDPOINT = "/stages/";
    public static final String RDD_ENDPOINT = "/storage/rdd/";
    public static final List<String> VALID_CLUSTER_ROLLUP_TYPES = Arrays.asList("INDIVIDUAL", "COLLECTIVE");
    public static final List<String> VALID_AGGREGATION_TYPES = Arrays.asList("AVERAGE", "SUM", "OBSERVATION");
    public static final List<String> VALID_TIME_ROLLUP_TYPES = Arrays.asList("AVERAGE", "SUM", "CURRENT");
    public static final String DEFAULT_CLUSTER_ROLLUP_TYPE = "INDIVIDUAL";
    public static final String DEFAULT_TIME_ROLLUP_TYPE = "AVERAGE";
    public static final String DEFAULT_AGGREGATION_TYPE = "AVERAGE";
    public static final String DEFAULT_MULTIPLIER = "1.0";
}
