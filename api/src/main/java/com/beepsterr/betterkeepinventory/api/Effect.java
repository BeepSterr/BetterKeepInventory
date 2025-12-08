package com.beepsterr.betterkeepinventory.api;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public interface Effect {
    void onDeath(Player player, PlayerDeathEvent event, LoggerInterface logger);
    void onRespawn(Player player, PlayerRespawnEvent event, LoggerInterface logger);
}
