package com.beepsterr.betterkeepinventory;

import com.beepsterr.betterkeepinventory.Commands.MainCommand;
import com.beepsterr.betterkeepinventory.Content.Conditions.PermissionsCondition;
import com.beepsterr.betterkeepinventory.Content.Conditions.PlaceholderAPI.PlaceholderCondition;
import com.beepsterr.betterkeepinventory.Content.Conditions.Vault.VaultCondition;
import com.beepsterr.betterkeepinventory.Content.Conditions.WorldsCondition;
import com.beepsterr.betterkeepinventory.Content.Effects.DamageItemEffect;
import com.beepsterr.betterkeepinventory.Content.Effects.DropItemEffect;
import com.beepsterr.betterkeepinventory.Content.Effects.ExpEffect;
import com.beepsterr.betterkeepinventory.Content.Effects.HungerEffect;
import com.beepsterr.betterkeepinventory.Content.Effects.Vault.VaultEffect;
import com.beepsterr.betterkeepinventory.Depends.BetterKeepInventoryPlaceholderExpansion;
import com.beepsterr.betterkeepinventory.Events.OnPlayerDeath;
import com.beepsterr.betterkeepinventory.Exceptions.UnloadableConfiguration;
import com.beepsterr.betterkeepinventory.Library.Config;
import com.beepsterr.betterkeepinventory.Library.MetricContainer;
import com.beepsterr.betterkeepinventory.Library.Versions.Version;
import com.beepsterr.betterkeepinventory.Library.Versions.VersionChannel;
import com.beepsterr.betterkeepinventory.Library.Versions.VersionChecker;
import com.beepsterr.betterkeepinventory.Registries.PluginConditionRegistry;
import com.beepsterr.betterkeepinventory.Registries.PluginEffectRegistry;
import com.beepsterr.betterkeepinventory.api.BetterKeepInventoryAPI;
import com.beepsterr.betterkeepinventory.api.BetterKeepInventoryAPIImpl;
import com.tcoded.folialib.FoliaLib;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.Random;
import java.util.logging.Level;

public final class BetterKeepInventory extends JavaPlugin implements Listener {

    public Config config;
    static public BetterKeepInventory instance;
    private FoliaLib foliaLib;

    public Version version = new Version(getDescription().getVersion());
    public VersionChecker versionChecker;

    public Random rng = new Random();
    public MetricContainer metrics;

    // Plugin Registries
    private final PluginConditionRegistry conditionRegistry = new PluginConditionRegistry();
    private final PluginEffectRegistry effectRegistry = new PluginEffectRegistry();


    @Override
    public void onEnable() {

        instance = this;
        foliaLib = new FoliaLib(BetterKeepInventory.getInstance());

        // Initialize API
        BetterKeepInventoryAPI api = new BetterKeepInventoryAPIImpl(conditionRegistry, effectRegistry);
        getServer().getServicesManager().register(BetterKeepInventoryAPI.class, api, this, ServicePriority.Highest);

        this.registerConditions(api);
        this.registerEffects(api);

        try {
            config = new Config(getConfig());
        }catch (UnloadableConfiguration e){
            CrashAndDisable("Configuration failed to load!\n" + e.getMessage());
            return;
        }

        // event handlers
        getServer().getPluginManager().registerEvents(new OnPlayerDeath(this), this);

        // Command registration
        Objects.requireNonNull(this.getCommand("betterkeepinventory")).setExecutor(new MainCommand());
        Objects.requireNonNull(this.getCommand("betterkeepinventory")).setTabCompleter(new MainCommand());

        // misc
        getServer().getPluginManager().registerEvents(this, this);
        metrics = new MetricContainer();

        // Enable PAPI Integration
        if(checkDependency("PlaceholderAPI")){
            log("Hello PlaceholderAPI!");
            new BetterKeepInventoryPlaceholderExpansion().register();
        }

        if(config.getNotifyChannel() != VersionChannel.NONE){
            versionChecker = new VersionChecker(config.getNotifyChannel());
        }

    }

    @Override
    public void onDisable() {

        getServer().getServicesManager().unregister(BetterKeepInventoryAPI.class, this);

        // Cancel version checks (not sure if needed in onDisable? but can't hurt. (hopefully))
        if(versionChecker != null){
            versionChecker.CancelCheck();
        }
    }

    private void registerEffects(BetterKeepInventoryAPI api) {

        // register built-in effects
        api.effectRegistry().register(this, "damage", DamageItemEffect::new);
        api.effectRegistry().register(this, "drop", DropItemEffect::new);
        api.effectRegistry().register(this, "exp", ExpEffect::new);
        api.effectRegistry().register(this, "hunger", HungerEffect::new);

        if(checkDependency("Vault")){
            api.effectRegistry().register(this, "vault", VaultEffect::new);
        }

    }

    private void registerConditions(BetterKeepInventoryAPI api) {

        // register built-in conditions
        api.conditionRegistry().register(this, "worlds", WorldsCondition::new);
        api.conditionRegistry().register(this, "permissions", PermissionsCondition::new);

        if(checkDependency("Vault")){
            api.conditionRegistry().register(this, "vault", VaultCondition::new);
        }
        if(checkDependency("PlaceholderAPI")){
            api.conditionRegistry().register(this, "placeholders", PlaceholderCondition::new);
        }

    }

    public static BetterKeepInventory getInstance(){
        return instance;
    }

    public boolean checkDependency(String dep){
        return Bukkit.getServer().getPluginManager().getPlugin(dep) != null;
    }

    public void CrashAndDisable(String message) {
        String alert = "\n" +
                ChatColor.DARK_RED + "=====================[ CRITICAL ERROR ]=====================\n"
                + ChatColor.RED +  "BetterKeepInventory encountered a irrecoverable error:\n\n"
                + ChatColor.YELLOW +  message + "\n\n"
                + ChatColor.RED + "The plugin has been disabled, and deaths will be handled by vanilla (!!)\n"
                + ChatColor.RED + "You should fix the issue, and restart the server to re-enable the plugin\n"
                + ChatColor.DARK_RED + "============================================================\n";

        getServer().getPluginManager().disablePlugin(this);
        getLogger().log(Level.SEVERE, alert);
        getServer().getConsoleSender().sendMessage(alert);
        getLogger().log(Level.INFO, "Continuing with server start in 10 seconds...");

        try{
            // Intentionally introduces a large delay during startup to hopefully catch the administrator's attention
            // Because this plugin is critical to death handling, we want to make sure the admin sees the error
            // This method should only be called if the plugin CANNOT continue working.
            Thread.sleep(10000);
        }catch (InterruptedException e){
           // do nothing, we're crashing anyway
        }

        // Disable the plugin
        getServer().getPluginManager().disablePlugin(this);

    }

    public void debug(Player player, String message){
        if(config.isDebug()){
            log("[DEBUG] (" + player.getName() + ") " + message);
        }
    }

    public void debug(String message){
        if(config.isDebug()){
            getLogger().log(Level.INFO, "[DEBUG] " + message);
        }
    }

    public void log(String message){
        getLogger().log(Level.INFO, message);
    }

    public static FoliaLib getScheduler(){
        return instance.foliaLib;
    }

}
