# Script to start Spark Streaming on wins topic

java -Xmx512m -Xms512m -XX:+UseParallelGC -cp /opt/ZeroMQ-Spark-Streaming/spark/spark-2.4.6-bin-hadoop2.7/jars/*:/opt/ZeroMQ-Spark-Streaming/lib/*:/opt/ZeroMQ-Spark-Streaming/xrtb-spark-streaming_2.11-0.1.jar com.analytics.streams.SparkZMQStreamClient /opt/ZeroMQ-Spark-Streaming/configs/publisher.config > /opt/ZeroMQ-Spark-Streaming/logs/publisher.log &
