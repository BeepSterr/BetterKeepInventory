package com.beepsterr.betterkeepinventory.Content.Conditions.PlaceholderAPI;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.beepsterr.betterkeepinventory.Library.PlaceholderItem;
import com.beepsterr.betterkeepinventory.api.Condition;
import com.beepsterr.betterkeepinventory.api.Exceptions.ConditionParseError;
import com.beepsterr.betterkeepinventory.api.LoggerInterface;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.ArrayList;
import java.util.List;

public class PlaceholderCondition implements Condition {

    private final List<PlaceholderItem> placeholderConditions;

    public PlaceholderCondition(ConfigurationSection config) throws ConditionParseError {
        this.placeholderConditions = new ArrayList<>();
        if (config != null) {
            for (String key : config.getKeys(false)) {
                ConfigurationSection inner = config.getConfigurationSection(key);
                if (inner != null) {
                    this.placeholderConditions.add(new PlaceholderItem(inner));
                }
            }
        }
    }

    @Override
    public boolean check(Player ply, PlayerDeathEvent deathEvent, PlayerRespawnEvent respawnEvent, LoggerInterface logger) {
        logger.child("Condition: PlaceholderAPI");
        logger.log("Testing " + this.placeholderConditions.size() + " placeholder condition(s)");

        for (PlaceholderItem item : this.placeholderConditions) {
            boolean testResult = item.test(ply);
            logger.log("Placeholder: " + item.toString() + " = " + (testResult ? "MATCHED" : "NOT MATCHED"));
            if (testResult) {
                logger.log("Result: MATCHED");
                logger.parent();
                return true;
            }
        }

        logger.log("Result: NOT MATCHED (no placeholders matched)");
        logger.parent();
        return false;
    }
}
