package com.beepsterr.betterkeepinventory.Content.Conditions.Vault;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.beepsterr.betterkeepinventory.Depends.Vault;
import com.beepsterr.betterkeepinventory.api.Condition;
import com.beepsterr.betterkeepinventory.api.LoggerInterface;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class VaultCondition implements Condition {

    private final double minBalance;

    public VaultCondition(ConfigurationSection config) {
        this.minBalance = config.getDouble("min_balance", 0.0);
    }

    @Override
    public boolean check(Player ply, PlayerDeathEvent deathEvent, PlayerRespawnEvent respawnEvent, LoggerInterface logger) {
        logger.child("Condition: Vault Economy");
        Vault vault = new Vault(BetterKeepInventory.getInstance());
        double playerBalance = vault.getPlayerBalance(ply);

        logger.log("Minimum balance required: " + vault.format(minBalance));
        logger.log("Player current balance: " + vault.format(playerBalance));

        boolean result = playerBalance >= minBalance;
        logger.log("Result: " + (result ? "MATCHED" : "NOT MATCHED") + " (player " + (result ? "has enough" : "lacks") + " funds)");
        logger.parent();
        return result;
    }
}
