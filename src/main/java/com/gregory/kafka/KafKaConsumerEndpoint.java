package com.gregory.kafka;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.gregory.kafka.Utils.loadProperties;
import static kafka.consumer.Consumer.createJavaConsumerConnector;

@ServerEndpoint(value = "/kafka/consumer")
public final class KafKaConsumerEndpoint {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Properties consumerProperties;
    private Session session;

    public KafKaConsumerEndpoint() throws IOException {
        this.consumerProperties = loadProperties("conf/consumer.properties");
        this.consumerProperties.put("group.id", "kafka-websocket" + System.currentTimeMillis());
    }

    @OnOpen
    public void start(Session session) throws Exception {
        this.session = session;
    }

    @OnClose
    public void end() {
    }

    @OnMessage
    public void incoming(String topic) throws Exception {
        System.out.println(topic);
        executorService.submit(new KafkaConsumerTask(topic, session, consumerProperties));
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
    }

    static public class KafkaConsumerTask implements Runnable {
        private final Session session;
        private final String topic;
        private final Properties consumerProperties;

        public KafkaConsumerTask(String topic, Session session, Properties consumerProperties) {
            this.session = session;
            this.topic = topic;
            this.consumerProperties = consumerProperties;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    ConsumerConnector connector = createJavaConsumerConnector(new ConsumerConfig(consumerProperties));

                    Map<String, Integer> topicCountMap = new HashMap<>();
                    topicCountMap.put(topic, 1);
                    Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = connector.createMessageStreams(topicCountMap);

                    KafkaStream<byte[], byte[]> stream = consumerMap.get(topic).get(0);
                    ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
                    while (iterator.hasNext()) {
                        sendText(iterator.next().message());
                    }
                }
            } catch (Throwable t) {
                System.out.println(t);
            }
        }

        private void sendText(byte[] message) {
            System.out.println(new String(message, Charset.forName("UTF-8")));
            session.getAsyncRemote().sendText(new String(message, Charset.forName("UTF-8")));
        }

    }

}
