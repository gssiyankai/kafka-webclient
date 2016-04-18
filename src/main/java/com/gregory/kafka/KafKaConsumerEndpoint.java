package com.gregory.kafka;

import kafka.consumer.ConsumerConfig;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;

import javax.websocket.*;
import javax.websocket.RemoteEndpoint.Async;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.gregory.kafka.Utils.loadProperties;
import static kafka.consumer.Consumer.createJavaConsumerConnector;

@ServerEndpoint(value = "/kafka/consumer")
public final class KafKaConsumerEndpoint {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private final Properties consumerProperties;
    private Session session;
    private Async remoteEndpoint;

    public KafKaConsumerEndpoint() throws IOException {
        this.consumerProperties = loadProperties("conf/consumer.properties");
    }

    @OnOpen
    public void start(Session session) throws Exception {
        this.session = session;
        this.remoteEndpoint = session.getAsyncRemote();
    }

    @OnClose
    public void end() {
    }

    @OnMessage
    public void incoming(String topic) throws Exception {
        System.out.println(topic);
        executorService.submit(new KafkaConsumerTask(topic, remoteEndpoint, consumerProperties));
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
    }

    static public class KafkaConsumerTask implements Runnable {
        private final String topic;
        private final Async remoteEndpoint;
        private final Properties consumerProperties;

        public KafkaConsumerTask(String topic, Async remoteEndpoint, Properties consumerProperties) {
            this.topic = topic;
            this.remoteEndpoint = remoteEndpoint;
            this.consumerProperties = consumerProperties;
        }

        @Override
        public void run() {
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
        }

        private void sendText(byte[] message) {
            System.out.println(new String(message, Charset.forName("UTF-8")));
            remoteEndpoint.sendText(new String(message, Charset.forName("UTF-8")));
        }

    }

}
