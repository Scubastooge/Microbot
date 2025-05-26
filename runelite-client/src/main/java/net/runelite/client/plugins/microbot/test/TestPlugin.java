package net.runelite.client.plugins.microbot.test;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.accountselector.AutoLoginPlugin;
import net.runelite.client.plugins.microbot.zerozero.birdhunter.BirdHunterPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;



import jakarta.websocket.*;
import java.net.URI;

import javax.inject.Inject;
import java.awt.*;



@PluginDescriptor(
        name = PluginDescriptor.Default + "Test",
        description = "Microbot test plugin",
        tags = {"test", "microbot"},
        enabledByDefault = true
)
@Slf4j
public class TestPlugin extends Plugin {

    @Inject
    private TestConfig config;
    @Provides
    TestConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(TestConfig.class);
    }

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private TestOverlay testOverlay;

    @Inject
    TestScript testScript;

    private Session session;
    private WebsocketListener wl;

    @Override
    protected void startUp() throws AWTException {
        boolean socketSuccess = false;
        if (overlayManager != null) {
            overlayManager.add(testOverlay);
        }
        while(true) {
            try {
                this.wl = new WebsocketListener(new URI(config.websocket()), this);
                socketSuccess = true;
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!socketSuccess)
        {
            System.exit(0);
        }
        testScript.run(config);
        System.out.println("Plugin startup thread: " + Thread.currentThread().getName());
        //Microbot.getClientThread().invoke(()->Microbot.startPlugin(Microbot.getPlugin(AutoLoginPlugin.class.getName())));
    }

    @Override
    protected void shutDown() {
        wl.closeWebSocket();
        testScript.shutdown();
        overlayManager.remove(testOverlay);

    }

    @Subscribe
    public void onGameTick(GameTick tick)
    {
        //System.out.println(getName().chars().mapToObj(i -> (char)(i + 3)).map(String::valueOf).collect(Collectors.joining()));
    }

    public void handleWebSocketMessage(String string, Session session)
    {
        System.out.println("Handle websocket thread: " + Thread.currentThread().getName());
        System.out.println(string);
        this.session = session;
        try{
            Map<String, Object> messageMap = new ObjectMapper().readValue(string, Map.class);

            switch (messageMap.get("command").toString()){
                case "stop":
                    this.stopPlugin(messageMap);
                case "start":
                    this.startPlugin(messageMap);
                case "quit":
                    this.quitClient();
            }
        }
        catch (Exception e){e.printStackTrace();}
    }

    public void stopPlugin(Map<String, Object> messageMap){
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean result = new AtomicBoolean(false);
        Microbot.getClientThread().invoke(()->{
            Microbot.stopPlugin(Microbot.getPlugin(messageMap.get("data").toString()));
            result.set(!Microbot.getPluginManager().isPluginEnabled(Microbot.getPlugin(messageMap.get("data").toString())));
            latch.countDown();
        });
        try {
            latch.await();
            this.session.getAsyncRemote().sendText("{\"result\": \""+result.toString()+"\"}");
        }
        catch (Exception e){e.printStackTrace();}
        //Microbot.stopPlugin(Microbot.getPlugin(BirdHunterPlugin.class.getName()));
    }

    public void startPlugin(Map<String, Object> messageMap){
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean result = new AtomicBoolean(false);
        Microbot.getClientThread().runOnSeperateThread(()-> {
            System.out.println("start plugin thread: " + Thread.currentThread().getName());
            Microbot.startPlugin(Microbot.getPlugin(messageMap.get("data").toString()));
            result.set(!Microbot.getPluginManager().isPluginEnabled(Microbot.getPlugin(messageMap.get("data").toString())));
            latch.countDown();
            return true;
        });
        try {
            latch.await();
            this.session.getAsyncRemote().sendText("{\"result\": \""+result.toString()+"\"}");
        }
        catch (Exception e){e.printStackTrace();}
        //Microbot.startPlugin(Microbot.getPlugin(BirdHunterPlugin.class.getName()));
    }

    public void quitClient(){
        System.exit(0);
    }

    class jsonMessage{
        public String message;

        jsonMessage(String message){
            this.message = message;
        }


    }

}
