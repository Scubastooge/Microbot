package net.runelite.client.plugins.microbot.test;

import jakarta.websocket.*;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.Script;


import java.net.URI;
import java.util.concurrent.TimeUnit;


public class TestScript extends Script {

    public static boolean test = false;
    Session session;
    public boolean run(TestConfig config) {
        Microbot.enableAutoRunOn = false;



        mainScheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                if (!Microbot.isLoggedIn()) return;
                if (!super.run()) return;
                long startTime = System.currentTimeMillis();


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

}



