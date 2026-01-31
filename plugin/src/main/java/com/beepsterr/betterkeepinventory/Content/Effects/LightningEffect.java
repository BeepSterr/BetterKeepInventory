package com.beepsterr.betterkeepinventory.Content.Effects;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.beepsterr.betterkeepinventory.api.Effect;
import com.beepsterr.betterkeepinventory.api.LoggerInterface;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Effect that strikes lightning at the death and/or respawn location.
 *
 * Configuration options:
 * - on_death: Strike lightning at death location (default true)
 * - on_respawn: Strike lightning at respawn location (default false)
 * - damage: If true, uses real lightning that deals damage. If false, uses effect-only lightning. (default false)
 */
public class LightningEffect implements Effect {

    private final boolean onDeath;
    private final boolean onRespawn;
    private final boolean damage;

    private static final Map<UUID, Location> deathLocations = new HashMap<>();

    public LightningEffect(ConfigurationSection config) {
        this.onDeath = config.getBoolean("on_death", true);
        this.onRespawn = config.getBoolean("on_respawn", false);
        this.damage = config.getBoolean("damage", false);
    }

    @Override
    public void onDeath(Player ply, PlayerDeathEvent event, LoggerInterface logger) {
        deathLocations.put(ply.getUniqueId(), ply.getLocation().clone());

        if (onDeath) {
            strikeLightning(ply.getLocation());
        }
    }

    @Override
    public void onRespawn(Player ply, PlayerRespawnEvent event, LoggerInterface logger) {
        deathLocations.remove(ply.getUniqueId());

        if (onRespawn) {
            // Delay slightly to ensure player has respawned
            BetterKeepInventory.getScheduler().getScheduler().runAtEntityLater(ply, () -> {
                strikeLightning(ply.getLocation());
            }, 5L);
        }
    }

    private void strikeLightning(Location loc) {
        if (damage) {
            loc.getWorld().strikeLightning(loc);
        } else {
            loc.getWorld().strikeLightningEffect(loc);
        }
    }
}
