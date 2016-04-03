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
    private ConsumerConnector connector;
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
        this.connector = createJavaConsumerConnector(new ConsumerConfig(consumerProperties));

        Map<String, Integer> topicCountMap = new HashMap<>();
        topicCountMap.put(topic, 1);
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = connector.createMessageStreams(topicCountMap);

        final List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);
        for (KafkaStream<byte[], byte[]> stream : streams) {
            executorService.submit(new KafkaConsumerTask(stream, remoteEndpoint));
        }
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
    }

    static public class KafkaConsumerTask implements Runnable {
        private KafkaStream stream;
        private Async remoteEndpoint;

        public KafkaConsumerTask(KafkaStream stream, Async remoteEndpoint) {
            this.stream = stream;
            this.remoteEndpoint = remoteEndpoint;
        }

        @Override
        public void run() {
            ConsumerIterator<byte[], byte[]> iterator = stream.iterator();
            while(iterator.hasNext()) {
                sendText(iterator.next().message());
            }
        }

        private void sendText(byte[] message) {
            System.out.println(new String(message, Charset.forName("UTF-8")));
            remoteEndpoint.sendText(new String(message, Charset.forName("UTF-8")));
        }

    }

}
