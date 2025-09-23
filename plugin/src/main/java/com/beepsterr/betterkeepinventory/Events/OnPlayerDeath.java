package com.beepsterr.betterkeepinventory.Events;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.beepsterr.betterkeepinventory.Library.ConfigRule;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.io.IOException;

public class OnPlayerDeath  implements Listener {

    BetterKeepInventory plugin;


    public OnPlayerDeath(BetterKeepInventory main){
        plugin = main;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {

        Player ply = event.getEntity();

        // Set base level of keepinv
        switch(plugin.config.getDefaultBehavior()){
            case KEEP:
                // these are needed to prevent dupes!!
                event.getDrops().clear();
                event.setDroppedExp(0);

                event.setKeepLevel(true);
                event.setKeepInventory(true);
                break;
            case DROP:
                event.setKeepLevel(false);
                event.setKeepInventory(false);
                break;
            // No case needed for INHERIT, as it will default to the world/other plugins behavior
        }

        BetterKeepInventory.instance.metrics.deathsProcessed +=1;

        // Time to process the top level rules
        for(ConfigRule rule : plugin.config.getRules()){
            rule.trigger(ply, event, null);
        }

    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent event) {

        // Time to process the top level rules
        for(ConfigRule rule : plugin.config.getRules()){
            rule.trigger(event.getPlayer(), null, event);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player ply = event.getPlayer();
        if(ply.hasPermission("betterkeepinventory.version.notify")){
            BetterKeepInventory.getScheduler().getScheduler().runAsync((consumer) -> {

                try{
                    // Yes, We're using Thread.sleep.
                    // I Don't think FoliaLib has a way to schedule delayed tasks at this point.
                    // I didn't want to spend a lot of time figuring out a way to do it the right way
                    // And since this is a thread that only gets spawned when "admin" players join
                    // It's a non-issue for now.
                    Thread.sleep(1000*5);
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                }


                if(plugin.versionChecker != null && plugin.versionChecker.IsUpdateAvailable()) {
                    // Send a message to the player
                    ply.sendMessage(ChatColor.YELLOW + "A new version of BetterKeepInventory is available!");
                    ply.sendMessage(ChatColor.GREEN + plugin.versionChecker.foundVersion.toString() + ChatColor.YELLOW + " (Installed: " + plugin.version.toString() + ")");
                }
            });

        }

    }


}
