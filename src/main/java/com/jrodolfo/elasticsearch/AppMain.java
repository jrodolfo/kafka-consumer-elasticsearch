package com.jrodolfo.elasticsearch;

import com.google.gson.JsonParser;
import com.jrodolfo.elasticsearch.util.PropertyUtil;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.time.Duration;
import java.util.Properties;


@Slf4j
public class AppMain {

    private ConsumerKafka consumerKafka;
    private TargetSystemElasticSearch targetSystemElasticSearch;

    private AppMain() {
        consumerKafka = new ConsumerKafka();
        targetSystemElasticSearch = new TargetSystemElasticSearch();
    }

    public static void main(String[] args) throws IOException {
        new AppMain().run();
    }

    private void run() throws IOException {

        log.info("Setting up...");

        // create elasticsearch client
        RestHighLevelClient client = targetSystemElasticSearch.createClient();


        Properties properties = PropertyUtil.getProperties();
        KafkaConsumer<String, String> consumer =
                consumerKafka.createKafkaConsumer(properties.getProperty("kafka.topic"));

        // poll for new data
        ConsumerRecords<String, String> records;
        while (true) {

            records = consumer.poll(Duration.ofMillis(100));
            String jsonString = null;
            int recordCount = records.count();
            log.info("\n\n\n\t\tReceived " + recordCount + " records");

            // using batching to be more efficient:
            BulkRequest bulkRequest = new BulkRequest();

            for (ConsumerRecord record : records) {
                log.info("\n\n\tProcessing new record:");
//                log.info("Key: " + record.key() + ", Value: " + record.value());
//                log.info("Partition: " + record.partition() + ", Offset: " + record.offset());

                // 2 strategies to create an id to make the consumer idempotent:
                // Strategy 1 for id creation) kafka generic id
                String idKafka = "topic-" + record.topic() + "__partition-" + record.partition() + "__offset-" + record.offset();
                log.info("idKafka: " + idKafka);

                // skipping bad data
                IndexRequest indexRequest = null;
                try {
                    // Strategy 2 for id creation) twitter feed specific id
                    String idTwitter = extractIdFromTweet(record.value().toString());
                    log.info("idTwitter: " + idTwitter);

                    // make sure index "twitter" exists by creating it at the Bonsai Console, using PUT and /twitter, then click on Run
                    jsonString = record.value().toString();
                    // we use id to make our request idempotent. We can use either idKafka or idTwitter.
                    indexRequest = new IndexRequest("twitter", "tweets", idTwitter)
                            .source(jsonString, XContentType.JSON);
                } catch (Exception e) {
                    log.error("skipping bad data:");
                    log.info("Key: " + record.key() + ", Value: " + record.value());
                    log.info("Partition: " + record.partition() + ", Offset: " + record.offset());
                    e.printStackTrace();
                }


                // If we want to send the requests in a batch, we add the indexRequest to our bulkRequest:
                if (indexRequest != null) bulkRequest.add(indexRequest);
                // Or if we want to send the requests one by one:
                /*
                IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
                String idElasticSearch = indexResponse.getId();
                log.info("idElasticSearch: " + idElasticSearch);
                try {
                    // introduce a small delay
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                */

                // close the client gracefully
                // client.close();
            }
            if (recordCount > 0) {
                log.info("\n\n\n\t\tSending the requests in a bulk...");
                // if we are using the bulk request, we can submit all requests now:
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
                log.info("bulkResponse.status: " + bulkResponse.status());
                log.info("bulkResponse.buildFailureMessage: " + bulkResponse.buildFailureMessage());

                log.info("Committing offsets...");
                consumer.commitSync();
                log.info("Offsets have been committed");
                try {
                    // introduce a small delay
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

//        log.info("End of application");
    }

    private String extractIdFromTweet(String tweetJson) {
        // gson library
        JsonParser jsonParser = new JsonParser();
        return jsonParser.parse(tweetJson).getAsJsonObject().get("id_str").getAsString();
    }
}
