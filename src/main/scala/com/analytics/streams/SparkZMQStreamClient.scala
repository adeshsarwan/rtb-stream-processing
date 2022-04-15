package com.analytics.streams


import java.io.File
import java.util.{Date, InputMismatchException}

import com.analytics.transformers.Transfomer
import com.analytics.utils.RTBUtils
import com.typesafe.config.ConfigFactory
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.zeromq.ZeroMQUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.elasticsearch.spark.rdd.EsSpark

  object SparkZMQStreamClient {
  def main(args: Array[String]): Unit = {
    if (args.length == 0 )
      throw new InputMismatchException("Please provide the config file as argument")

    val jobConfig = ConfigFactory.parseFile(new File(args(0)))

    // read the Job Configs
    val cores = jobConfig.getInt("parameters.spark.cores")
    var numOfPartitions = 0
    if (jobConfig.hasPath("parameters.spark.num.partitions"))
      numOfPartitions = jobConfig.getInt("parameters.spark.num.partitions")
    else
      numOfPartitions = cores

    // read the ES Env Config File
    val esEnvConfigs = ConfigFactory.parseFile(new File(jobConfig.getString("es.env.config.file")))

    // get the configs
    val indexName = jobConfig.getString("index.name")
    val topicName = jobConfig.getString("topic.name")
    val topicUrl = jobConfig.getString("topic.url")
    val indexIdFieldName = jobConfig.getString("index.id.field.name")
    val appName = jobConfig.getString("parameters.spark.app.name")
    val streamIntervalSeconds = jobConfig.getLong("parameters.spark.stream.interval.seconds")
    val checkPointLocation = jobConfig.getString("stream.es.checkpoint.path")
    val transformerName = jobConfig.getString("transform.className")

    val spark = SparkSession.builder()
      .appName(appName)
      .master(s"local[$cores]").getOrCreate()
    spark.sparkContext.setLogLevel("ERROR")

   // Create the context and set the batch size.
    val ssc = new StreamingContext(spark.sparkContext, Seconds(streamIntervalSeconds))

    val lines = ZeroMQUtils.createTextStream(
      ssc, topicUrl, true, Seq(topicName.getBytes)
    )

    //to ES
    var esConfigs = scala.collection.mutable.Map.empty[String, String]
    esConfigs += ("es.port" -> esEnvConfigs.getString("env.es.port"))
    esConfigs += ("es.net.ssl" -> esEnvConfigs.getString("env.es.net.ssl"))
    esConfigs += ("es.nodes" -> esEnvConfigs.getString("env.es.nodes"))
    esConfigs += ("es.net.http.auth.user" -> esEnvConfigs.getString("env.es.net.http.auth.user"))
    esConfigs += ("es.net.http.auth.pass" -> esEnvConfigs.getString("env.es.net.http.auth.pass"))
    esConfigs += ("checkpointLocation" -> checkPointLocation)
    esConfigs += ("es.write.operation" -> "upsert")
    esConfigs += ("es.mapping.id" -> indexIdFieldName)

    lines.foreachRDD(rdd => {
      val rddCount = rdd.count()
      if (rddCount > 0) {
        val resultRdd = rdd.mapPartitions(partitions => {
          val transformer = Class.forName(transformerName).newInstance()
            .asInstanceOf[Transfomer]
          transformer.setRTBUtils(new RTBUtils)
          val esConfig =  esEnvConfigs
          val jConfig = jobConfig
          val resultPartition = partitions.map(record => {
            try {
              transformer.transform(record, esConfig, jConfig)
            } catch {
              case ex:Exception =>{
                ex.printStackTrace()
              }
            }
          })
          resultPartition
        })
        EsSpark.saveJsonToEs(resultRdd, indexName, esConfigs)
        println(new Date() +" :: "+resultRdd.count()+" Records sent to ES Index "+indexName)
      } else {
        println(new Date() +" :: 0 Records ")
      }
    })

    ssc.start()
    println(new Date() +" :: Streaming Started...")
    ssc.awaitTermination()
  }
}
