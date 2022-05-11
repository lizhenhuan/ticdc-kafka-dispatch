package com.pingcap.ticdc.dispatch;

import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.KafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;

public class Dispatch {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dispatch.class);

    private static final Gson GSON = new Gson();

    private static final Map<String, KafkaProducer> PRODUCER_MAP = new HashMap<>();

    private static final Map<String, List> DATA_MAP = new HashMap<>();

    private static String SERVER = "";

    /**
     *
     * @param args bootstrap.servers topic groupId
     */
    public static void main(String[] args) {
        KafkaConsumer<String, String> kafkaConsumer = createConsumer(args);
        readMessage(kafkaConsumer);
    }
    private static void readMessage(KafkaConsumer<String, String> kafkaConsumer) {
        while (true) {
            Duration duration = Duration.ofMillis(1000);
            ConsumerRecords<String, String> records = kafkaConsumer.poll(duration);

            for (ConsumerRecord<String, String> record : records) {
                String key = record.key();
                String value = record.value();
                LOGGER.info(String.format("key is : %s \n value is %s", key, value));
                TiCDCMessage message = GSON.fromJson(value, TiCDCMessage.class);
                String topic = String.format("%s.%s", message.getDatabase().toLowerCase(), message.getTable().toLowerCase());
                List list = DATA_MAP.get(topic);
                if (list == null) {
                    list = new ArrayList<String>();
                    DATA_MAP.put(topic, list);
                }
                list.add(value);
            }
            DATA_MAP.forEach((topic,dataList) -> {
                sendMessage(topic, dataList);
            });
            kafkaConsumer.commitAsync();
        }
    }

//    private static void printOffsets(KafkaConsumer<String, String> consumer, TopicPartition topicPartition) {
//
//        OffsetAndMetadata committed = consumer
//                .committed(topicPartition);
//        consumer.position(topicPartition);
//        Long offset = committed.offset();
//        long position = consumer.position(topicPartition);
//        LOGGER.info("Committed: %s, current position %s%n", offset, position);
//    }
    private static KafkaConsumer<String, String> createConsumer(String[] args) {
        String servers = "", topic = "", groupId = "";
        if (args.length == 3) {
            servers = args[0];
            topic = args[1];
            groupId = args[2];
        } else {
            System.out.println("please input 3 args: bootstrap.servers topic groupId");
            System.exit(1);
        }
        SERVER = servers;

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("enable.auto.commit", "false");
        props.put("auto.commit.interval.ms", "1000");
        props.put("session.timeout.ms", "30000");
        KafkaConsumer<String, String> consumer = new KafkaConsumer< >(props);
        consumer.subscribe(Arrays.asList(topic));
        return consumer;
    }
    private static KafkaProducer<String, String> initProducer(String topic) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, topic);

        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer producer = new KafkaProducer<String, String>(props);
        producer.initTransactions();
        return producer;
    }
    private static KafkaProducer<String, String> getProducer(String topic) {
        KafkaProducer<String, String> producer = PRODUCER_MAP.get(topic);
        if (producer == null) {
            producer = initProducer(topic);
            PRODUCER_MAP.put(topic, producer);
        }
        return producer;
    }
    private static void sendMessage(String topic, List<String> dataList) {
        KafkaProducer<String, String> producer = getProducer(topic);
        String message = "";
        try {
            producer.beginTransaction();
            for (String msg : dataList) {
                message = msg;
                LOGGER.info(String.format("send message success ,topic : %s , message : %s ", topic, message));
                producer.send(new ProducerRecord<>(topic.toLowerCase(), 0, "", msg));
            }
            dataList.clear();
            producer.commitTransaction();
        } catch (KafkaException e) {
            LOGGER.error(String.format("send message failed ,topic : %s , message : %s ", topic, message), e);
            producer.abortTransaction();
        }
    }
}
