package net.runelite.client.plugins.microbot.test;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.GameTick;
import net.runelite.client.RuneLite;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.microbot.Microbot;
import net.runelite.client.plugins.microbot.accountselector.AutoLoginPlugin;
import net.runelite.client.plugins.microbot.zerozero.birdhunter.BirdHunterPlugin;
import net.runelite.client.ui.overlay.OverlayManager;



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
        if (overlayManager != null) {
            overlayManager.add(testOverlay);
        }

        this.wl = new WebsocketListener(this);

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

    public void handleWebSocketMessage(String string)
    {
        System.out.println("Handle websocket thread: " + Thread.currentThread().getName());
        System.out.println(string);
        if (string.equals("stop")) {
            this.stopPlugin();
        }
        if (string.equals("start")) {
            this.startPlugin();
        }
    }

    public void stopPlugin(){
        Microbot.getClientThread().invoke(()->Microbot.stopPlugin(Microbot.getPlugin(BirdHunterPlugin.class.getName())));
        //Microbot.stopPlugin(Microbot.getPlugin(BirdHunterPlugin.class.getName()));
    }

    public void startPlugin(){
        Microbot.getClientThread().runOnSeperateThread(()-> {
            System.out.println("start plugin thread: " + Thread.currentThread().getName());
            Microbot.startPlugin(Microbot.getPlugin(BirdHunterPlugin.class.getName()));
            return true;
        });
        //Microbot.startPlugin(Microbot.getPlugin(BirdHunterPlugin.class.getName()));
    }

    public void quitClient(){

    }

    class jsonMessage{
        public String message;

        jsonMessage(String message){
            this.message = message;
        }


    }

}
