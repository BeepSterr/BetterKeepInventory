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

public class KickEffect implements Effect {

    private final String message;

    public KickEffect(ConfigurationSection config) {
        this.message = config.getString("message", "You died!");
    }

    @Override
    public void onDeath(Player ply, PlayerDeathEvent event, LoggerInterface logger) {
        logger.child("Effect: Kick");
        logger.log("Kick message: " + message);
        logger.log("Scheduling kick after 1 tick to prevent duplication issues");

        // Delaying the kick by 1 tick to prevent item dupe issue if used with drop effect.
        BetterKeepInventory.getScheduler().getScheduler().runAtEntityLater( ply, () -> {
            logger.log("Kicking player: " + ply.getName());
            ply.kickPlayer(message);
        }, 1L);

        logger.parent();
    }

    @Override
    public void onRespawn(Player ply, PlayerRespawnEvent event, LoggerInterface logger) {
        // No action needed on respawn for this effect
    }
}