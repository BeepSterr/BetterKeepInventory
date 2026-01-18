package com.beepsterr.betterkeepinventory.Content.Effects;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.beepsterr.betterkeepinventory.api.Effect;
import com.beepsterr.betterkeepinventory.api.LoggerInterface;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HungerEffect implements Effect {

    private final int min;
    private final int amount;
    public static final Map<UUID, Integer> hungerMap = new HashMap<>();

    public HungerEffect(ConfigurationSection config) {
        this.min = config.getInt("min", 0);
        this.amount = config.getInt("amount", 0);
    }

    @Override
    public void onDeath(Player ply, PlayerDeathEvent event, LoggerInterface logger) {
        logger.child("Effect: Hunger");
        int currentHunger = ply.getFoodLevel();
        int newHunger = Math.max(currentHunger - amount, min);

        logger.log("Current hunger: " + currentHunger + ", Amount to remove: " + amount + ", Min: " + min);
        logger.log("New hunger after death: " + newHunger);
        logger.log("Saving for respawn");

        hungerMap.put(ply.getUniqueId(), newHunger);
        logger.parent();
    }

    @Override
    public void onRespawn(Player ply, PlayerRespawnEvent event, LoggerInterface logger) {
        logger.child("Effect: Hunger (Respawn)");
        logger.log("Scheduling hunger restoration after 5 ticks");

        Bukkit.getScheduler().runTaskLater(BetterKeepInventory.getInstance(), () -> {
            Integer saved = hungerMap.remove(ply.getUniqueId());
            if (saved != null) {
                ply.setFoodLevel(saved);
                logger.log("Restored hunger to " + saved);
            } else {
                logger.log("No saved hunger found for player");
            }
        }, 5L);

        logger.parent();
    }
}