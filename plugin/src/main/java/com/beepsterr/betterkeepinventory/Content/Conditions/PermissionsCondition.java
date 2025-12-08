package com.beepsterr.betterkeepinventory.Content.Conditions;

import com.beepsterr.betterkeepinventory.api.Condition;
import com.beepsterr.betterkeepinventory.api.LoggerInterface;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;

public class PermissionsCondition implements Condition {

    private final List<String> permissions;

    public PermissionsCondition(ConfigurationSection config) {
        this.permissions = config.getStringList("nodes");
    }

    @Override
    public boolean check(Player ply, PlayerDeathEvent deathEvent, PlayerRespawnEvent respawnEvent, LoggerInterface logger) {
        for (String perm : permissions) {
            boolean negated = perm.startsWith("!");
            String actual = negated ? perm.substring(1) : perm;

            if (negated != ply.hasPermission(actual)) {
                return true;
            }
        }
        return false;
    }
}
