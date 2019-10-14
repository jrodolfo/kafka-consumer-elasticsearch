# Kafka Consumer ElasticSearch

In this Maven Application we have a Consumer consuming messages from a Kafka Cluster,
and sending them to a ElasticSearch, our Target System hosted at Bonsai.io.

The majority of the code of this app is based on what I learned from this course:
                                                    
    Apache Kafka Series - Learn Apache Kafka for Beginners v2, by Stephane Maarek
    https://www.udemy.com/course/apache-kafka/

The course is great - I highly recommend it.

See the instructions from doc/kafka-on-windows.txt to learn 
how to install Kafka on Windows and make it work, so that you
will be able to test this app.

This app works with the app Kafka Producer Twitter. If you want to check the offsets, run this command:

    C:\>kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --group kafka-demo-elasticsearch --describe

You can reset the consumers offsets to zero if you want to replay the data:

    C:\>kafka-consumer-groups --bootstrap-server 127.0.0.1:9092 --group kafka-demo-elasticsearch --reset-offsets --execute --to-earliest --topic twitter_tweets 

You will need a developer account at Bonsai.io so that you can enter the hostname, port, user and password
at resources/app.properties.

