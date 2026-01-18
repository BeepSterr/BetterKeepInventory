package com.beepsterr.betterkeepinventory.api;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public interface Condition {
    public boolean check(Player player, PlayerDeathEvent deathEvent, PlayerRespawnEvent respawnEvent, LoggerInterface logger);
}
