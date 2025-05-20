package net.runelite.client.plugins.microbot.test;

import jakarta.websocket.*;

import jakarta.websocket.Session;
import net.runelite.client.plugins.microbot.Microbot;

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
        Microbot.getClientThread().invoke(() -> {plugin.handleWebSocketMessage(message);});
        System.out.println("OnMessage thread: " + Thread.currentThread().getName());
    }

    @OnClose
    public void onClose(Session session){
        System.out.println("Disconnected");
    }

    @OnError
    public void onError(Throwable throwable){
        throwable.printStackTrace();
    }

    public void closeWebSocket() {
        try {
            System.out.println("Websocket Closing");
            this.session.close();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
