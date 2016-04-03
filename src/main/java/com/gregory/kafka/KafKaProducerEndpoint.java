package com.gregory.kafka;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/kafka/producer")
public final class KafKaProducerEndpoint {

    private Session session;

    @OnOpen
    public void start(Session session) throws Exception {
        this.session = session;
    }

    @OnClose
    public void end() {
    }

    @OnMessage
    public void incoming(String message) throws Exception {
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
    }

}
