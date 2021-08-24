## Apache Spark Monitoring Extension

## Use Case

Apache Spark is a fast and general purpose cluster computing system. It provides high level APIs in Java, Scala, Python & R as well as an optimized engine that supports general execution graphs. It also supports a tool called Spark SQL for SQL and relational data processing. 

The AppDynamics Spark Extension can monitor multiple Spark clusters and worker nodes and extracts metrics from every running and completed Spark application, more specifically the jobs, executors, stages and storage RDDs within these applications. The metrics reported by the extension can be configured by users. We have developed this extension using the latest version of Spark (2.1.0). 

## Prerequisites

1. Before the extension is installed, the prerequisites mentioned [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Prerequisites-Guide/ta-p/35213) need to be met. Please do not proceed with the extension installation if the specified prerequisites are not met 
2. This extension will fetch metrics from Spark applications running in a cluster setup. Spark application metrics persist only as long as the application is alive, which makes it essential to have a repository or metric dump which stores these metrics even after the application has been terminated. 
3. Spark offers a number of metric dumps - REST, JMX, CSV etc. This extension uses a REST dump in the form of a Spark History Server. Please refer to the next section for instructions on how to configure and use the History Server. 
4. The extension needs to be able to connect to spark history server in order to collect and send metrics. To do this, you will have to either establish a remote connection in between the extension and the product, or have an agent on the same machine running the product in order for the extension to collect and send the metrics.  
5. More general Spark related information can be found on the Spark homepage - http://spark.apache.org/docs/3.0.0/ 

## Installation
1. Run "mvn clean install"
1. Unzip the contents of SparkMonitor-\<version\>.zip file (&lt;ApacheSparkRepo&gt; / targets) and copy the directory to `<your-machine-agent-dir>/monitors`.
2. Edit config.yml file and provide the required configuration (see Configuration section)
3. Restart the Machine Agent.

Please place the extension in the **"monitors"** directory of your **Machine Agent** installation directory. Do not place the extension in the **"extensions"** directory of your **Machine Agent** installation directory.

#### Configuring spark for metric persistence

As mentioned above, the Spark History Server is essential for metrics to persist even when applications have been killed or completed. To use the history server, the following needs to be done: 

1. Make a copy of SPARK_HOME/conf/spark-defaults.conf.template and name it as spark-defaults.conf. This file can be used to specify properties for your Spark applications. By default, the .template file is used. 
2. Uncomment/add the following properties to your spark-defaults.conf file: 
  <pre>
  spark.eventLog.enabled                true
  spark.eventLog.dir                    file:/tmp/spark-events #Can be modified to your preference. Create a directory called spark-events in your /tmp/ folder if you wish to use this exact same configuration.
  spark.history.provider	        org.apache.spark.deploy.history.FsHistoryProvider
  spark.history.fs.logDirectory		file:/tmp/spark-events #Can be modified to your preference
  spark.history.fs.update.interval	10s #Can be modified to your preference
  spark.history.retainedApplications	50 #Can be modified to your preference
  spark.history.ui.maxApplications	5000 #Can be modified to your preference
  spark.history.ui.port 	        18080 #Can be modified to your preference
  spark.history.kerberos.enabled	false
  </pre>
 3. Start the history server on each node. 
 <pre>./sbin/start-history-server.sh</pre>
 4. Use the property **--properties-file path_to_spark_defaults_conf/spark-defaults.conf** along with your spark-submit script to submit your apps.
  <pre>./bin/spark-submit &lt;xyz&gt; --properties-file path_to_spark_defaults_conf/spark-defaults.conf </pre> 
 Replace &lt;xyz&gt; placeholder with details of your application that you wish to submit.

 For e.g.
   <pre>./bin/spark-submit --class org.apache.spark.examples.JavaWordCount /path/to/examples/jars/spark-examples_2.12-3.0.0.jar /path/of/file/to/perform/wordcount/mytext.txt --properties-file /path/to/conf/spark-defaults.conf</pre>

## Configuration

### Config.yml

#### Configure metric prefix
Please follow section 2.1 of the [Document](https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695) to set up metric prefix.
<pre>
#Metric prefix used when SIM is enabled for your machine agent
#metricPrefix: "Custom Metrics|Spark Monitor|"

#This will publish metrics to specific tier
#Instructions on how to retrieve the Component ID can be found in the Metric Prefix section of https://community.appdynamics.com/t5/Knowledge-Base/How-do-I-troubleshoot-missing-custom-metrics-or-extensions/ta-p/28695
metricPrefix: "Server|Component:&lt;COMPONENT OR TIER ID&gt;|Custom Metrics|Spark Monitor|"
</pre>

#### Spark history server configuration
<pre>
#Add your Spark History Servers here.
servers:
   - host: "localhost"
     port: 18080
     name: "Myspark"
</pre>
- name: Display name for your spark history server which will be displayed in metric path. It should be unique for all servers
- host: Host address of your spark history server
- port: Spark history server port

You can configure multiple spark history servers like below:
<pre>
servers:
   - host: "localhost"
     port: 18080
     name: "Myspark 1"
   - host: "localhost"
     port: 18081
     name: "Myspark 2"
</pre>

#### Number of threads
Always include: (one thread per server + Number of applications you are monitoring in that server) + 1 (to run main task).

For example, if you have 2 spark history servers configured and 2 application in one server and 3 application in another server, then number of threads required are 8 ((1 + 2) for first server + (1 + 3) for second server + 1 thread to run main task)

#### Configure metric section

The metrics shown in the file are customizable. You can choose to remove metrics or an entire section (jobs, stages etc) and they won't be reported. You can also add properties to individual metrics. The following properties can be added: 
1. alias: The actual name of the metric as you would see it in the metric browser
2. multiplier: Used to transform the metric value, particularly for cases where memory is reported in bytes. 1.0 by default. 
3. delta: Used to display a 'delta' or a difference between metrics that have an increasing value every minute. False by default.
4. clusterRollUpType: The cluster-rollup qualifier specifies how the Controller aggregates metric values in a tier (a cluster of nodes). The value is an enumerated type. Valid values are **INDIVIDUAL** (default) or **COLLECTIVE**. 
5. aggregationType: The aggregator qualifier specifies how the Machine Agent aggregates the values reported during a one-minute period. Valid values are **AVERAGE** (default) or **SUM** or **OBSERVATION**. 
6. timeRollUpType: The time-rollup qualifier specifies how the Controller rolls up the values when it converts from one-minute granularity tables to 10-minute granularity and 60-minute granularity tables over time. Valid values are **AVERAGE** (default) or **SUM** or **CURRENT**. 
7. convert: Used to report a metric that is reporting text value by converting the value to its mapped integer

More details around this can be found [here](https://community.appdynamics.com/t5/Knowledge-Base/Extensions-Commons-Library-Metric-Transformers/ta-p/35413)
<pre>
## This section can be used to configure metrics published by the extension. You have the ability to add multipliers & modify the metric qualifiers for each metric.
## Valid 'cluster' rollup values: INDIVIDUAL, COLLECTIVE
## Valid 'aggregation' types: AVERAGE, SUM, OBSERVATION
## Valid 'time' rollup types: AVERAGE, SUM, CURRENT
## You can choose to not add any or all of these fields to any metric and the default values for each of the above will be used (INDIVIDUAL, AVERAGE & AVERAGE for cluster, aggregation & time respectively).
metrics:
     jobs:
        - name: "numTasks"
          alias: "Num Tasks"  #Number of tasks in the application
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "numCompletedTasks"
          alias: "Num Completed Tasks"  #Number of completed tasks in the application
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "numSkippedTasks"
          alias: "Num Skipped Tasks"  #Number of completed tasks in the application"
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "numFailedTasks"
          alias: "Num Failed Tasks"  #Number of completed tasks in the application
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "numActiveStages"
          alias: "Num Active Stages"  #Number of active stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "numCompletedStages"
          alias: "Num Completed Stages"  #Number of completed stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "numSkippedStages"
          alias: "Num Skipped Stages"  #Number of completed stages"
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "numFailedStages"
          alias: "Num Failed Stages"  #Number of failed stages"
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

     stages:
        - name: "numActiveTasks"
          alias: "Num Active Tasks"  #Number of active tasks in the application's stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "numCompleteTasks"
          alias: "Num Complete Tasks"  #Number of complete tasks in the application's stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "numFailedTasks"
          alias: "Num Failed Tasks"  #Number of failed tasks in the application's stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "executorRunTime"
          alias: "Executor Run Time"  #Time spent by executor in the application's stages (ms)
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "inputBytes"
          alias: "Input Bytes"  #Input bytes in the application's stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "inputRecords"
          alias: "Input Records"  #Input records in the application's stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "outputBytes"
          alias: "Output Bytes"  #Output bytes in the application's stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "outputRecords"
          alias: "Output Records"  #Output records in the application's stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "shuffleReadBytes"
          alias: "Shuffle Read Bytes"  #Number of bytes read during a shuffle in the application's stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "shuffleReadRecords"
          alias: "Shuffle Read Records"  #Number of records read during a shuffle in the application's stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "shuffleWriteBytes"
          alias: "Shuffle Write Bytes"  #Number of bytes written during a shuffle in an application's stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "shuffleWriteRecords"
          alias: "Shuffle Write Records"  #Number of shuffled records in the application's stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "memoryBytesSpilled"
          alias: "Memory Bytes Spilled"  #Number of bytes spilled to disk in the application's stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "diskBytesSpilled"
          alias: "Disk Bytes Spilled"  #Max size on disk of the spilled bytes in the application's stages
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

     executors:
        - name: "rddBlocks"
          alias: "Rdd Blocks"  #Number of persisted RDD blocks in the application's executors
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "memoryUsed"
          alias: "Memory Used" #Amount of memory used for cached RDDs in the application's executors
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "diskUsed"
          alias: "Disk Used" #Amount of disk space used by persisted RDDs in the application's executors
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "activeTasks"
          alias: "Active Tasks" #Number of active tasks in the application's executors
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "failedTasks"
          alias: "Failed Tasks" #Number of failed tasks in the application's executors
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "completedTasks"
          alias: "Completed Tasks" #Number of completed tasks in the application's executors
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "totalTasks"
          alias: "Total Tasks" #Total number of tasks in the application's executors
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "totalDuration"
          alias: "Total Duration" #Time spent by the application's executors executing tasks (ms)
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "totalInputBytes"
          alias: "totalInputBytes" #Total number of input bytes in the application's executors
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "totalShuffleRead"
          alias: "Total Shuffle Read" #Total number of bytes read during a shuffle in the application's executors
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "totalShuffleWrite"
          alias: "Total Shuffle Write"  #Total number of shuffled bytes in the application's executors
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "maxMemory"
          alias: "Max Memory" #Maximum memory available for caching RDD blocks in the application's executors
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

     rdd:
        - name: "numPartitions"
          alias: "Num Partitions" #Number of persisted RDD partitions in the application/second
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "numCachedPartitions"
          alias: "Num Cached Partitions" #Number of in-memory cached RDD partitions in the application/second
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "memoryUsed"
          alias: "Memory Used" #Amount of memory used in the application's persisted RDDs
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"

        - name: "diskUsed"
          alias: "Disk Used"  #Amount of disk space used by persisted RDDs in the application
          multiplier: "1"
          aggregationType: "AVERAGE"
          timeRollUpType: "AVERAGE"
          clusterRollUpType: "INDIVIDUAL"
          delta: "false"
</pre>

#### Yml Validation
Please copy all the contents of the config.yml file and go to http://www.yamllint.com/ . On reaching the website, paste the contents and press the “Go” button on the bottom left.

## Workbench

Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130)

## Troubleshooting

1. Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension.

2. Check if curl command is returning successful response for any endpoint (Job, Executor, Stage, RDD)
```
   curl -v http://<host>:<port>/api/v1/applications/<application-id>/jobs
   curl -v http://<host>:<port>/api/v1/applications/<application-id>/executors
   curl -v http://<host>:<port>/api/v1/applications/<application-id>/stages
   curl -v http://<host>:<port>/api/v1/applications/<application-id>/storage/rdd
```
For e.g.
```
>curl -v http://192.168.1.14:18080/api/v1/applications/local-1627642328917/jobs


{
  "jobId" : 0,
  "name" : "collect at JavaWordCount.java:53",
  "submissionTime" : "2021-07-30T10:52:12.343GMT",
  "completionTime" : "2021-07-30T10:52:13.415GMT",
  "stageIds" : [ 0, 1 ],
  "status" : "SUCCEEDED",
  "numTasks" : 2,
  "numActiveTasks" : 0,
  "numCompletedTasks" : 2,
  "numSkippedTasks" : 0,
  "numFailedTasks" : 0,
  "numKilledTasks" : 0,
  "numCompletedIndices" : 2,
  "numActiveStages" : 0,
  "numCompletedStages" : 2,
  "numSkippedStages" : 0,
  "numFailedStages" : 0,
  "killedTasksSummary" : { }

```

## Contributing

Always feel free to fork and contribute any changes directly via [GitHub](https://github.com/Appdynamics/apache-spark-monitoring-extension).

## Version

|          Name            |  Version   |
|--------------------------|------------|
|Extension Version         |1.2.0       |
|Controller Compatibility  |4.5 or Later|
|Machine Agent Version     |4.5.13+     |
|Product Tested on         |3.0.0       |
|Last Update               |04/08/2021  |
