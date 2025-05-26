package net.runelite.client.plugins.microbot.test;


import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.util.Collections;
import java.util.List;

@ConfigGroup("test")
public interface TestConfig extends Config {
    @ConfigItem(
        keyName = "websocket",
        name = "Websocket",
        description = "address",
        position = 0
    )
    default String websocket() {return "ws://192.168.5.15:8001/ws/bot/wingedPlover1/";}

    @ConfigItem(
            keyName = "retrys",
            name = "Retrys",
            description = "Number of retrys before quitting",
            position = 1
    )
    default int retrys(){return 5;}
}

