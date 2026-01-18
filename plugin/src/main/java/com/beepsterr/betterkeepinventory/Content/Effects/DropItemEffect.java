package com.beepsterr.betterkeepinventory.Content.Effects;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.beepsterr.betterkeepinventory.Library.Utilities;
import com.beepsterr.betterkeepinventory.api.LoggerInterface;
import com.beepsterr.betterkeepinventory.api.Types.MaterialType;
import com.beepsterr.betterkeepinventory.api.Effect;
import com.beepsterr.betterkeepinventory.api.Types.SlotType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class DropItemEffect implements Effect {

    public enum Mode {
        SIMPLE, PERCENTAGE, ALL
    }

    private final Mode mode;
    private final float min;
    private final float max;
    private List<String> nameFilters = List.of();
    private List<String> loreFilters = List.of();
    private SlotType slots = new SlotType(List.of());
    private MaterialType items = new MaterialType(List.of());

    public DropItemEffect(ConfigurationSection config) {
        this.mode = Mode.valueOf(config.getString("mode", "SIMPLE").toUpperCase());
        this.min = (float) config.getDouble("min", 0.0);
        this.max = (float) config.getDouble("max", 0.0);

        ConfigurationSection filters = config.getConfigurationSection("filters");
        if(filters != null) {
            this.slots = new SlotType(Utilities.ConfigList(filters, "slots"));
            this.items = new MaterialType(Utilities.ConfigList(filters, "items"));
            this.nameFilters = Utilities.ConfigList(filters, "name");
            this.loreFilters = Utilities.ConfigList(filters, "lore");
        }
    }

    @Override
    public void onRespawn(Player ply, PlayerRespawnEvent event, LoggerInterface logger) {
        // Nothing on respawn
    }

    @Override
    public void onDeath(Player ply, PlayerDeathEvent event, LoggerInterface logger) {
        logger.child("Effect: Drop Items");
        BetterKeepInventory plugin = BetterKeepInventory.getInstance();
        Random rng = plugin.rng;

        List<Integer> dropSlots = this.slots.getSlotIds();
        List<Material> dropItems = items.getMaterials();

        logger.log("Mode: " + mode + ", Min: " + min + ", Max: " + max);
        logger.log("Filters - Slots: " + (!dropSlots.isEmpty() ? dropSlots.size() : "none") +
                  ", Items: " + (!dropItems.isEmpty() ? dropItems.size() : "none") +
                  ", Name: " + (!nameFilters.isEmpty() ? nameFilters.size() : "none") +
                  ", Lore: " + (!loreFilters.isEmpty() ? loreFilters.size() : "none"));

        int itemsProcessed = 0;
        int totalDropped = 0;

        for (int i = 0; i < ply.getInventory().getSize(); i++) {

            var item = ply.getInventory().getItem(i);
            if(item == null) continue;

            var meta = item.getItemMeta();

            // Check the filters
            if (!dropItems.isEmpty() && !this.items.isIncludeAll() && !dropItems.contains(item.getType())){
                logger.log("Skip slot " + i + ": item filter (" + item.getType() + ")");
                continue;
            };
            if (!dropSlots.isEmpty() && !dropSlots.contains(i)){
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

            itemsProcessed++;

            // Drop all
            if (mode == Mode.ALL) {
                logger.log("Slot " + i + " (" + item.getType() + "): dropping ALL (" + item.getAmount() + ")");
                ply.getWorld().dropItemNaturally(ply.getLocation(), item);
                ply.getInventory().setItem(i, null);
                totalDropped += item.getAmount();
                continue;
            }

            int inventoryCount = item.getAmount();
            int removalCount = 0;

            switch (mode) {
                case SIMPLE -> removalCount = (int) (min + (max - min) * rng.nextDouble());
                case PERCENTAGE -> {
                    double percentage = min + (max - min) * rng.nextDouble();
                    removalCount = (int) (inventoryCount * (percentage / 100.0));
                }
            }

            if (removalCount <= 0) {
                logger.log("Slot " + i + " (" + item.getType() + "): calculated drop=" + removalCount + ", skipping");
                continue;
            }

            if (inventoryCount - removalCount < 0) {
                removalCount = inventoryCount;
            }

            if (removalCount == 0) continue;

            logger.log("Slot " + i + " (" + item.getType() + "): dropping " + removalCount + "/" + inventoryCount);

            Map<String, String> replacements = new HashMap<>();
            replacements.put("amount", String.valueOf(removalCount));
            replacements.put("item", MaterialType.GetName(item));
            plugin.config.sendMessage(ply, "effects.drop", replacements);

            var itemClone = item.clone();
            itemClone.setAmount(removalCount);
            ply.getWorld().dropItemNaturally(ply.getLocation(), itemClone);
            totalDropped += removalCount;

            if (inventoryCount - removalCount == 0) {
                ply.getInventory().setItem(i, null);
            } else {
                item.setAmount(inventoryCount - removalCount);
                ply.getInventory().setItem(i, item);
            }
        }

        logger.log("Summary: " + itemsProcessed + " stacks processed, " + totalDropped + " items dropped");
        logger.parent();
    }
}
