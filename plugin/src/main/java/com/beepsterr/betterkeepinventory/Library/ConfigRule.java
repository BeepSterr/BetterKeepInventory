package com.beepsterr.betterkeepinventory.Library;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.beepsterr.betterkeepinventory.api.BetterKeepInventoryAPI;
import com.beepsterr.betterkeepinventory.api.Condition;
import com.beepsterr.betterkeepinventory.api.Effect;
import com.beepsterr.betterkeepinventory.api.Exceptions.ConditionParseError;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.ArrayList;
import java.util.List;

public class ConfigRule {

    private final String name;
    private final boolean enabled;
    private final ConfigRule parent;

    private List<Condition> conditions = new ArrayList<>();
    private final List<Effect> effects = new ArrayList<>();

    private final List<ConfigRule> children = new ArrayList<>();

    public ConfigRule(ConfigurationSection config, ConfigRule parent) {

        this.parent = parent;
        this.name = config.getString("name", "Unnamed Rule");
        this.enabled = config.getBoolean("enabled", false);

        var api = Bukkit.getServer().getServicesManager().load(BetterKeepInventoryAPI.class);
        if(api == null){
            throw new RuntimeException("BetterKeepInventory API not loaded (?)");
        }

        // Parse conditions
        if (config.isConfigurationSection("conditions")) {
            var condSection = config.getConfigurationSection("conditions");
            assert condSection != null;
            for (String key : condSection.getKeys(false))
            {
                BetterKeepInventory.getInstance().debug("Trying Condition '" + key + "'");

                if (!api.conditionRegistry().has(key)) {
                    BetterKeepInventory.getInstance().debug("Condition '" + key + "' is not registered. Triggered in rule '" + name + ". Skipping.'");
                    continue;
                }

                ConfigurationSection section = condSection.getConfigurationSection(key);
                if (section == null) {
                    BetterKeepInventory.getInstance().debug("Condition '" + key + "' is missing configuration section in rule '" + name + "'. Skipping.");
                    continue;
                }

                try{
                    Condition cond = api.conditionRegistry().get(key).create(section);
                    conditions.add(cond);
                }catch(ConditionParseError e){
                    BetterKeepInventory.getInstance().getLogger().warning("Condition '" + key + "' could not be parsed");
                    BetterKeepInventory.getInstance().getLogger().warning("Error: " + e.getMessage());
                }
            }
        }


        // Parse effects
        ConfigurationSection effectSection = config.getConfigurationSection("effects");
        if (effectSection != null) {
            for (String key : effectSection.getKeys(false)) {
                ConfigurationSection effConfig = effectSection.getConfigurationSection(key);
                if (effConfig == null) continue;

                Effect effect = api.effectRegistry().create(key, effConfig);
                if (effect == null){
                    BetterKeepInventory.getInstance().getLogger().warning("Effect '" + key + "' is not registered. Triggered in rule '" + name + "'!!!");
                    continue;
                }

                effects.add(effect);
            }
        }

        // Parse children
        ConfigurationSection childrenSection = config.getConfigurationSection("children");
        if (childrenSection != null) {
            for (String childKey : childrenSection.getKeys(false)) {
                ConfigurationSection childConfig = childrenSection.getConfigurationSection(childKey);
                if (childConfig != null) {
                    children.add(new ConfigRule(childConfig, this));
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void trigger(Player ply, PlayerDeathEvent deathEvent, PlayerRespawnEvent respawnEvent) {
        BetterKeepInventory plugin = BetterKeepInventory.getInstance();

        if (!enabled) {
            plugin.debug(ply, "Rule " + this + " was skipped (not enabled)");
            return;
        }

        // log all conditions that are to be checked
        plugin.debug(ply, "Rule " + this + " checking conditions: " + conditions.stream().map(Condition::getClass));

        if (conditions.isEmpty() || conditions.stream().allMatch(c -> c.check(ply, deathEvent, respawnEvent))) {
            plugin.debug(ply, "Rule " + this + " met conditions, running effects!");

            if (deathEvent != null) {
                for (Effect effect : effects) {
                    plugin.debug(ply, "Running effect (D): " + effect.toString());
                    effect.onDeath(ply, deathEvent);
                }
            }

            if (respawnEvent != null) {
                for (Effect effect : effects) {
                    plugin.debug(ply, "Running effect (R): " + effect.toString());
                    effect.onRespawn(ply, respawnEvent);
                }
            }

            for (ConfigRule child : children) {
                child.trigger(ply, deathEvent, respawnEvent);
            }

        } else {
            plugin.debug(ply, "Rule " + this + " was skipped (conditions not met)");
        }
    }

    @Override
    public String toString() {
        return parent != null ? "ConfigRule{" + parent.getName() + " > " + name + "}" : "ConfigRule{" + name + "}";
    }
}
