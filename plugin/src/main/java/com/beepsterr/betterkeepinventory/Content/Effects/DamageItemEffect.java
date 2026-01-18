package com.beepsterr.betterkeepinventory.Content.Effects;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.beepsterr.betterkeepinventory.Library.MetricContainer;
import com.beepsterr.betterkeepinventory.Library.Utilities;
import com.beepsterr.betterkeepinventory.api.LoggerInterface;
import com.beepsterr.betterkeepinventory.api.Types.MaterialType;
import com.beepsterr.betterkeepinventory.api.Types.SlotType;
import com.beepsterr.betterkeepinventory.api.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.regex.Pattern;

public class DamageItemEffect implements Effect {

    public enum Mode {
        PERCENTAGE, PERCENTAGE_REMAINING, SIMPLE
    }

    private final Mode mode;
    private final float min;
    private final float max;
    private final boolean useEnchantments;
    private final boolean dontBreak;
    private List<String> nameFilters = List.of();
    private List<String> loreFilters = List.of();
    private SlotType slots = new SlotType(List.of());
    private MaterialType items = new MaterialType(List.of());

    public DamageItemEffect(ConfigurationSection config) {
        this.mode = Mode.valueOf(config.getString("mode", "PERCENTAGE").toUpperCase());
        this.min = (float) config.getDouble("min", 0.0);
        this.max = (float) config.getDouble("max", 0.0);
        this.useEnchantments = config.getBoolean("use_enchantments", false);
        this.dontBreak = config.getBoolean("dont_break", false);

        ConfigurationSection filters = config.getConfigurationSection("filters");
        if(filters != null) {
            this.slots = new SlotType(Utilities.ConfigList(filters, "slots"));
            this.items = new MaterialType(Utilities.ConfigList(filters, "items"));
            this.nameFilters = Utilities.ConfigList(filters, "name");
            this.loreFilters = Utilities.ConfigList(filters, "lore");
        }

    }

    @Override
    public void onRespawn(Player player, PlayerRespawnEvent event, LoggerInterface logger) {
        // This effect doesn't do anything on respawn (yet)
    }

    @Override
    public void onDeath(Player ply, PlayerDeathEvent event, LoggerInterface logger) {
        logger.child("Effect: Damage Items");
        BetterKeepInventory plugin = BetterKeepInventory.getInstance();
        Random rng = plugin.rng;

        List<Integer> slots = this.slots.getSlotIds();
        List<Material> items = this.items.getMaterials();

        logger.log("Mode: " + mode + ", Min: " + min + ", Max: " + max);
        logger.log("Use enchantments: " + useEnchantments + ", Don't break: " + dontBreak);
        logger.log("Filters - Slots: " + (!slots.isEmpty() ? slots.size() : "none") +
                  ", Items: " + (!items.isEmpty() ? items.size() : "none") +
                  ", Name: " + (!nameFilters.isEmpty() ? nameFilters.size() : "none") +
                  ", Lore: " + (!loreFilters.isEmpty() ? loreFilters.size() : "none"));

        int itemsProcessed = 0;
        int itemsDamaged = 0;
        int itemsBroken = 0;

        for (int i = 0; i < ply.getInventory().getSize(); i++) {

            var item = ply.getInventory().getItem(i);
            if(item == null) continue;

            var meta = item.getItemMeta();

            // Check the filters
            if (!items.isEmpty() && !this.items.isIncludeAll() && !items.contains(item.getType())){
                logger.log("Skip slot " + i + ": item filter (" + item.getType() + ")");
                continue;
            };
            if (!slots.isEmpty() && !slots.contains(i)){
                logger.log("Skip slot " + i + ": slot filter");
                continue;
            };

            if(meta != null){
                if (!nameFilters.isEmpty() && !Utilities.advancedStringCompare(meta.getDisplayName(), nameFilters)){
                    logger.log("Skip slot " + i + ": name filter (" + meta.getDisplayName() + ")");
                    continue;
                };
                if(meta.getLore() != null){
                    boolean loreFilterMatched = false;
                    for( String lore : meta.getLore()){
                        if (!loreFilters.isEmpty() && !Utilities.advancedStringCompare(lore, loreFilters)) {
                            loreFilterMatched = true;
                        }
                    }
                    if(loreFilterMatched){
                        logger.log("Skip slot " + i + ": lore filter");
                        continue;
                    }
                }
            }

            if (!(meta instanceof Damageable damageableMeta)){
                logger.log("Skip slot " + i + ": not damageable (" + item.getType() + ")");
                continue;
            };

            itemsProcessed++;

            int currentDamageTaken = damageableMeta.getDamage();
            int maxDurability = item.getType().getMaxDurability();
            int damageToTake = calculateDamage(rng, currentDamageTaken, maxDurability);

            if (damageToTake < 0) continue;

            int originalDamage = damageToTake;
            damageToTake = applyUnbreaking(item, damageToTake);

            logger.log("Slot " + i + " (" + item.getType() + "): durability=" + currentDamageTaken + "/" + maxDurability +
                      ", damage=" + damageToTake + (originalDamage != damageToTake ? " (reduced from " + originalDamage + " by unbreaking)" : ""));

            Map<String, String> replacements = new HashMap<>();
            replacements.put("amount", String.valueOf(damageToTake));
            replacements.put("item", MaterialType.GetName(item));

            plugin.metrics.durabilityPointsLost += damageToTake;

            if (maxDurability - currentDamageTaken - damageToTake < 0) {
                if (dontBreak || item.getType() == Material.ELYTRA) { // elytra is special, it doesn't break
                    damageableMeta.setDamage(maxDurability);
                    item.setItemMeta(damageableMeta);
                    logger.log("  → Saved from breaking (dont_break=" + dontBreak + ", elytra=" + (item.getType() == Material.ELYTRA) + ")");
                    plugin.config.sendMessage(ply, "effects.damage", replacements);
                } else {
                    item.setAmount(item.getAmount() - 1);
                    damageableMeta.setDamage(0);
                    item.setItemMeta(meta);
                    ply.getWorld().playSound(ply.getLocation(), Sound.ENTITY_ITEM_BREAK, 0.8f, 0.8f);
                    logger.log("  → Item BROKE");
                    itemsBroken++;
                    plugin.config.sendMessage(ply, "effects.damage_break", replacements);
                }
            } else {
                damageableMeta.setDamage(currentDamageTaken + damageToTake);
                item.setItemMeta(meta);
                itemsDamaged++;
                plugin.config.sendMessage(ply, "effects.damage", replacements);
            }
        }

        logger.log("Summary: " + itemsProcessed + " items processed, " + itemsDamaged + " damaged, " + itemsBroken + " broken");
        logger.parent();
    }

    private int calculateDamage(Random rng, int currentDamageTaken, int maxDurability) {
        return switch (mode) {
            case SIMPLE -> (int) (min + (max - min) * rng.nextDouble());
            case PERCENTAGE -> (int) (maxDurability * ((min + (max - min) * rng.nextDouble()) / 100.0));
            case PERCENTAGE_REMAINING -> (int) ((maxDurability - currentDamageTaken) * ((min + (max - min) * rng.nextDouble()) / 100.0));
        };
    }

    private int applyUnbreaking(ItemStack item, int damageToTake) {
        if (!useEnchantments) return damageToTake;
        if (!item.getEnchantments().containsKey(Enchantment.DURABILITY)) return damageToTake;

        int level = item.getEnchantmentLevel(Enchantment.DURABILITY);
        if (level > 9) return 0; // interpreted as unbreakable
        return (int) (damageToTake * (1.0 - (0.33 * level)));
    }

}
