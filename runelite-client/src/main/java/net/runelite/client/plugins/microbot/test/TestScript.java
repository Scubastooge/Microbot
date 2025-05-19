package net.runelite.client.plugins.microbot.test;

import jakarta.websocket.*;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;


import java.net.URI;
import java.util.concurrent.TimeUnit;

@ClientEndpoint
public class TestScript extends Script {

    public static boolean test = false;
    Session session;
    public boolean run(TestConfig config) {
        Microbot.enableAutoRunOn = false;

        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            String uri = "ws://192.168.5.15:8765"; // Or your own WebSocket URL
            container.connectToServer(this.getClass(), URI.create(uri));
        } catch (Exception e) {
            e.printStackTrace();
        }

        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();

                this.session.getAsyncRemote().sendText("test");

                long endTime = System.currentTimeMillis();
                long totalTime = endTime - startTime;
                System.out.println("Total time for loop " + totalTime);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, 0, 1000, TimeUnit.MILLISECONDS);
        return true;
    }
    @Override
    public void shutdown() {
        super.shutdown();
    }

    @OnOpen
    public void onOpen(Session session){
        this.session = session;
        System.out.println("Connected");
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.println("Received response: " + message);
    }
}



