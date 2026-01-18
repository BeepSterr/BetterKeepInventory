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
        logger.child("Condition: Permissions");
        logger.log("Checking " + permissions.size() + " permission node(s)");

        for (String perm : permissions) {
            boolean negated = perm.startsWith("!");
            String actual = negated ? perm.substring(1) : perm;
            boolean hasPermission = ply.hasPermission(actual);

            logger.log("Node: " + perm + " (negated=" + negated + ", has=" + hasPermission + ")");

            if (negated != hasPermission) {
                logger.log("Result: MATCHED");
                logger.parent();
                return true;
            }
        }

        logger.log("Result: NOT MATCHED (no permissions matched)");
        logger.parent();
        return false;
    }
}
