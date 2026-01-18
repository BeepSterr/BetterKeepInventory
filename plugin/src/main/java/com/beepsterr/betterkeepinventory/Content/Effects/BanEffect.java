package com.beepsterr.betterkeepinventory.Content.Effects;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.beepsterr.betterkeepinventory.api.Effect;
import com.beepsterr.betterkeepinventory.api.LoggerInterface;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class BanEffect implements Effect {

    private final String message;
    private Duration expiration = null;

    public BanEffect(ConfigurationSection config) {
        this.message = config.getString("message", "You died!");
        String durationString = config.getString("duration", "5m").toLowerCase();

        if(durationString.equals("permanent")) {
            this.expiration = null;
        }

        if(durationString.endsWith("s")){
            long seconds = Long.parseLong(durationString.replace("s", ""));
            this.expiration = Duration.ofSeconds(seconds);
        } else if(durationString.endsWith("m")){
            long minutes = Long.parseLong(durationString.replace("m", ""));
            this.expiration = Duration.ofMinutes(minutes);
        } else if(durationString.endsWith("h")){
            long hours = Long.parseLong(durationString.replace("h", ""));
            this.expiration = Duration.ofHours(hours);
        } else if(durationString.endsWith("d")){
            long days = Long.parseLong(durationString.replace("d", ""));
            this.expiration = Duration.ofDays(days);
        } else {
            // Default to minutes if no suffix is provided
            long minutes = Long.parseLong(durationString);
            this.expiration = Duration.ofMinutes(minutes);
        }

    }

    @Override
    public void onDeath(Player ply, PlayerDeathEvent event, LoggerInterface logger) {
        logger.child("Effect: Ban");
        logger.log("Ban message: " + message);
        logger.log("Ban duration: " + (expiration != null ? expiration.toString() : "PERMANENT"));
        logger.log("Scheduling ban after 1 tick to prevent duplication issues");

        // Delaying the ban and kick by 1 tick to prevent item dupe issue if used with drop effect.
        BetterKeepInventory.getScheduler().getScheduler().runAtEntityLater( ply, () -> {

            Date expires = null;
            if (this.expiration != null) {
                expires = Date.from(Instant.now().plus(this.expiration));
                logger.log("Ban will expire at: " + expires);
            } else {
                logger.log("Ban is permanent");
            }

            Bukkit.getBanList(BanList.Type.NAME).addBan(ply.getName(), this.message, expires, String.valueOf(ply.getUniqueId()));
            logger.log("Banned and kicking player: " + ply.getName());
            ply.kickPlayer(this.message);

        }, 1L);

        logger.parent();
    }

    @Override
    public void onRespawn(Player ply, PlayerRespawnEvent event, LoggerInterface logger) {
        // No action needed on respawn for this effect
    }
}