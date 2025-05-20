package net.runelite.client.plugins.microbot.test;

import jakarta.websocket.*;

import jakarta.websocket.Session;
import java.util.concurrent.CountDownLatch;

import javax.swing.*;
import java.net.URI;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

@ClientEndpoint
public class WebsocketListener {
    private final TestPlugin plugin;
    private Session session;
    private WebSocketContainer container;

    public WebsocketListener(TestPlugin plugin) {
        this.plugin = plugin;
        try {
            this.container = ContainerProvider.getWebSocketContainer();
            this.container.connectToServer(this, new URI("ws://192.168.5.15:8765"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        System.out.println("Connected!");
        session.getAsyncRemote().sendText("Hello, WebSocket!\n");
    }

    @OnMessage
    public void onMessage(String message) {
        plugin.handleWebSocketMessage(message);
    }

    @OnClose
    public void onClose(Session session){
        System.out.println("Disconnected");
    }

    @OnError
    public void onError(Throwable throwable){
        throwable.printStackTrace();
    }


}
