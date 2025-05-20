package net.runelite.client.plugins.microbot.test;


import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

import java.util.Collections;
import java.util.List;

@ConfigGroup("test")
public interface TestConfig extends Config {
    @ConfigItem(
        keyName = "Token",
        name = "Token",
        description = "Client Token",
        position = 0
    )
    default List<String> Token()
    {
        return Collections.singletonList("ABC123");
    }
}
