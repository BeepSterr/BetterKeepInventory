package com.beepsterr.betterkeepinventory.Content.Conditions;

import com.beepsterr.betterkeepinventory.Library.Utilities;
import com.beepsterr.betterkeepinventory.api.Condition;
import com.beepsterr.betterkeepinventory.api.LoggerInterface;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;
import java.util.regex.Pattern;

public class WorldsCondition implements Condition {

    private final List<String> worlds;

    public WorldsCondition(ConfigurationSection config) {
        this.worlds = config.getStringList("nodes");
    }

    @Override
    public boolean check(Player ply, PlayerDeathEvent deathEvent, PlayerRespawnEvent respawnEvent, LoggerInterface logger) {
        logger.child("Condition: Worlds");
        String worldName = ply.getWorld().getName();
        logger.log("Current world: " + worldName);
        logger.log("Checking against " + worlds.size() + " world pattern(s): " + worlds);

        boolean result = Utilities.advancedStringCompare(worldName, worlds);
        logger.log("Result: " + (result ? "MATCHED" : "NOT MATCHED"));
        logger.parent();
        return result;
    }
}
