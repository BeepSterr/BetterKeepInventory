package com.beepsterr.betterkeepinventory.api.Types;

import com.beepsterr.betterkeepinventory.api.Exceptions.TypeError;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MaterialList {

    List<Material> allowedMaterials = new ArrayList<>();

    public MaterialList(List<String> materials){
        this.parse(materials);
    }

    public void parse(List<String> materials){
        for(String matStr : materials){
            this.parse(matStr);
        }
    }

    /**
     * Parse a single material string and add it to the allowed materials if valid
     * - * = all materials
     * - ! = exclude material
     * - G: = material group
     */
    public void parse(String material){

        if (material == null) return;
        String s = material.trim();
        if (s.isEmpty()) return;

        if (s.equals("*")) {
            allowedMaterials.clear();
            allowedMaterials.addAll(
                    Arrays.stream(Material.values())
                            .filter(m -> !m.isLegacy())
                            .toList()
            );
            return;
        }

        boolean exclude = s.startsWith("!");
        if (exclude) {
            s = s.substring(1).trim();
            if (s.isEmpty()) return;
        }

        try{
            Material mat = Material.valueOf(material.toUpperCase());
            if(!allowedMaterials.contains(mat)){
                allowedMaterials.add(mat);
            }
        } catch (IllegalArgumentException e){
            // bazinga nation - not a material, fall through to other parsing
        }

        if(s.startsWith("G:")){
            try {
                s = s.substring(2).trim();
                List<Material> group = MaterialGroups.parse(s);
                if (exclude) {
                    allowedMaterials.removeAll(group);
                } else {
                    for (Material m : group) {
                        if (!allowedMaterials.contains(m)) {
                            allowedMaterials.add(m);
                        }
                    }
                }
                return;
            } catch (TypeError ignored) {
                // invalid group - cry about it
            }
        }


        Material mat = Material.matchMaterial(s);
        if (mat == null) {
            // unknown material - sucks to suck
            return;
        }

        if (exclude) {
            allowedMaterials.remove(mat);
        } else {
            if (!allowedMaterials.contains(mat)) {
                allowedMaterials.add(mat);
            }
        }

    }

    public void add(Material material){
        if (!allowedMaterials.contains(material)) {
            allowedMaterials.add(material);
        }
    }

    public void remove(Material material){
        allowedMaterials.remove(material);
    }

    public List<Material> getMaterials(){
        return allowedMaterials;
    }

    // TODO: When dropping 1.19, migrate to use Material#GetTranslationKey()
    public static String GetName(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        String displayName;

        if (meta != null && meta.hasDisplayName()) {
            displayName = meta.getDisplayName(); // Use the custom name if present
        } else {
            // Fallback to a readable name from the enum
            displayName = Arrays.stream(item.getType().toString().split("_"))
                    .map(part -> part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase())
                    .collect(Collectors.joining(" "));
        }
        return displayName;
    }

}
