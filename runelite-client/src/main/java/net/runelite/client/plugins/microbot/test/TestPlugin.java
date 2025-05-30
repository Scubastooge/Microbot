package net.runelite.client.plugins.microbot.test;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NpcID;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginManager;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.accountselector.AutoLoginPlugin;
import net.runelite.client.plugins.microbot.util.npc.Rs2Npc;
import net.runelite.client.plugins.microbot.util.walker.Rs2Walker;
import net.runelite.client.plugins.microbot.zerozero.birdhunter.BirdHunterPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;



import jakarta.websocket.*;
import java.net.URI;

import javax.inject.Inject;
import javax.swing.*;
//import java.awt.*;



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
    private boolean start;

    @Inject
    private OverlayManager overlayManager;
    @Inject
    private TestOverlay testOverlay;

    @Inject
    TestScript testScript;

    private Session session;
    private WebsocketListener wl;

    public static String getAccountName() {
        Properties properties = new Properties();
        try {
            FileInputStream input = new FileInputStream(System.getProperty("user.home") + "/.runelite/credentials.properties");
            properties.load(input);
            return properties.getProperty("JX_DISPLAY_NAME");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected void startUp() throws AWTException {
        this.start = false;
        System.out.println(AutoLoginPlugin.class.getName());
        boolean socketSuccess = false;
        if (overlayManager != null) {
            overlayManager.add(testOverlay);
        }
        try {
            String username = getAccountName();
            URI uri = new URI(String.format("%s/ws/bot/%s/", config.websocket(), username));

            this.wl = new WebsocketListener(uri, this);
            socketSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
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
        /*if (this.start) {
            Microbot.getClientThread().invokeLater(() -> {
                System.out.println("microbot startplugin thread: " + Thread.currentThread().getName());
                System.out.println("microbot EDT: " + SwingUtilities.isEventDispatchThread());
                Microbot.startPlugin(this.plugin);
                //result.set(!Microbot.getPluginManager().isPluginEnabled(Microbot.getPlugin(messageMap.get("data").toString())));
                //return true;
            });
            this.start = false;
        }*/
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
                    break;
                case "start":
                    this.startPlugin(messageMap);
                    break;
                case "quit":
                    this.quitClient();
                    break;
                case "configure":
                    this.configPlugin(messageMap);
                    break;
                case "walk":
                    this.walkTo();
                    break;
            }
        }
        catch (Exception e){e.printStackTrace();}
    }

    public void stopPlugin(Map<String, Object> messageMap){
        System.out.println(messageMap.get("data").toString());
        Plugin plugin = Microbot.getPlugin(messageMap.get("data").toString());
        if (plugin == null) {
            System.out.println("Plugin was null");
        }
        else {
            Microbot.stopPlugin(plugin);
            try {
                Thread.sleep(100);
                boolean result = (!Microbot.isPluginEnabled(plugin.getClass()));
                this.session.getAsyncRemote().sendText("{\"message\": \"" + result + "\"}");
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Microbot.stopPlugin(Microbot.getPlugin(BirdHunterPlugin.class.getName()));
        }
    }
    public void walkTo()
    {
        Rs2Walker.walkTo(Rs2Npc.getNpc("Bob").getWorldLocation());
    }

    public void startPlugin(Map<String, Object> messageMap){
        System.out.println(messageMap.get("data").toString());
        Plugin plugin = Microbot.getPlugin(messageMap.get("data").toString());

        if (plugin == null) {
            System.out.println("Plugin was null");
        }
        else {
            try {
                Microbot.startPlugin(plugin);
            } catch (Exception e) {
                //e.printStackTrace();
                //Hiding the EDT error.. Seems meaningless
            }

            try {
                Thread.sleep(100);
                boolean result = Microbot.isPluginEnabled(plugin.getClass());
                this.session.getAsyncRemote().sendText("{\"message\": \"" + result + "\"}");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Microbot.startPlugin(Microbot.getPlugin(BirdHunterPlugin.class.getName()));

    }

    public void quitClient(){
        System.exit(0);
        //test
    }

    public void configPlugin(Map<String, Object> messageMap)
    {
        Object configsObj = messageMap.get("configs");
        java.util.List<Map<String, Object>> configs = (java.util.List<Map<String, Object>>) configsObj;
        for (Map<String, Object> config : configs) {
            //if (config.get("valueType").toString())
            Microbot.getConfigManager().setConfiguration(config.get("configGroup").toString(), config.get("itemKey").toString(), config.get("itemValue"));
        }
    }


    class jsonMessage{
        public String message;

        jsonMessage(String message){
            this.message = message;
        }


    }

}
