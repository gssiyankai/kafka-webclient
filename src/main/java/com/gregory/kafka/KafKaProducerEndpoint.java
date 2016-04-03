package com.gregory.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Properties;

import static com.gregory.kafka.Utils.loadProperties;

@ServerEndpoint(value = "/kafka/producer")
public final class KafKaProducerEndpoint {

    private final Properties producerProperties;
    private Session session;

    public KafKaProducerEndpoint() throws IOException {
        this.producerProperties = loadProperties("conf/producer.properties");
    }

    @OnOpen
    public void start(Session session) throws Exception {
        this.session = session;
    }

    @OnClose
    public void end() {
    }

    @OnMessage
    public void incoming(String message) throws Exception {
        System.out.println(message);
        String topic = message.substring(0, message.indexOf(';'));
        String quote = message.substring(message.indexOf(';')+1);
        KafkaProducer<String, byte[]> producer = new KafkaProducer<>(producerProperties, new StringSerializer(), new ByteArraySerializer());
        final ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, quote.getBytes());
        producer.send(record);
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
    }

}
