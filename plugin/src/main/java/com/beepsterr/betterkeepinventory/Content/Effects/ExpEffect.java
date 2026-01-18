package com.beepsterr.betterkeepinventory.Content.Effects;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.beepsterr.betterkeepinventory.api.Effect;
import com.beepsterr.betterkeepinventory.api.LoggerInterface;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ExpEffect implements Effect {

    public enum Mode {
        SIMPLE, PERCENTAGE, ALL
    }

    public enum How {
        DELETE, DROP
    }

    private final Mode mode;
    private final How how;
    private final float min;
    private final float max;

    public ExpEffect(ConfigurationSection config) {
        this.mode = Mode.valueOf(config.getString("mode", "PERCENTAGE").toUpperCase());
        this.how = How.valueOf(config.getString("how", "DROP").toUpperCase());
        this.min = (float) config.getDouble("min", 0.0);
        this.max = (float) config.getDouble("max", 0.0);
    }

    @Override
    public void onRespawn(Player ply, PlayerRespawnEvent event, LoggerInterface logger) {
        // Nothing on respawn
    }

    @Override
    public void onDeath(Player ply, PlayerDeathEvent event, LoggerInterface logger) {
        logger.child("Effect: Experience Loss");
        BetterKeepInventory plugin = BetterKeepInventory.getInstance();
        Random rng = plugin.rng;

        int playerExpLevel = ply.getLevel();
        logger.log("Player current level: " + playerExpLevel);
        logger.log("Mode: " + mode + ", How: " + how + ", Min: " + min + ", Max: " + max);

        int levelsToLose = switch (mode) {
            case SIMPLE -> (int) (min + (max - min) * rng.nextDouble());
            case PERCENTAGE -> (int) (playerExpLevel * ((min + (max - min) * rng.nextDouble()) / 100.0));
            case ALL -> playerExpLevel;
        };

        logger.log("Calculated levels to lose: " + levelsToLose);

        Map<String, String> replacements = new HashMap<>();
        replacements.put("amount", String.valueOf(Math.min(levelsToLose, playerExpLevel)));

        if(levelsToLose < 1){
            logger.log("No levels to lose, skipping");
            logger.parent();
            return;
        }

        switch (how) {
            case DELETE -> {
                ply.setLevel(playerExpLevel - levelsToLose);
                ply.setExp(0);
                logger.log("Deleted " + levelsToLose + " levels (new level: " + ply.getLevel() + ")");
                plugin.config.sendMessage(ply, "effects.exp_loss", replacements);
            }
            case DROP -> {
                float expToDrop;
                if (playerExpLevel <= levelsToLose) {
                    expToDrop = getExpAtLevel(playerExpLevel) + ply.getExp();
                    ply.setLevel(0);
                    ply.setExp(0);
                } else {
                    expToDrop = getExpAtLevel(playerExpLevel) - getExpAtLevel(playerExpLevel - levelsToLose);
                    ply.setLevel(playerExpLevel - levelsToLose);
                    ply.setExp(0);
                }

                logger.log("Dropping " + (int)Math.round(expToDrop) + " experience points (new level: " + ply.getLevel() + ")");

                ExperienceOrb orb = ply.getWorld().spawn(ply.getLocation(), ExperienceOrb.class);
                orb.setExperience((int) Math.round(expToDrop));
                plugin.config.sendMessage(ply, "effects.exp_dropped", replacements);
            }
        }
        logger.parent();
    }

    private int getExpAtLevel(int level) {
        if (level <= 16) return level * level + 6 * level;
        if (level <= 31) return (int) (2.5 * level * level - 40.5 * level + 360);
        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }
}
