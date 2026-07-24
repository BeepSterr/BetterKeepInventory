package com.beepsterr.betterkeepinventory.Content.Effects;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.beepsterr.betterkeepinventory.support.NoopLogger;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * ExpEffect removes XP levels on death. These cover the DELETE path (deterministic level math)
 * across the SIMPLE / PERCENTAGE / ALL modes; min==max makes the RNG term drop out.
 */
class ExpEffectTest {

    private ServerMock server;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        server.addSimpleWorld("world");
        MockBukkit.load(BetterKeepInventory.class);
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static ExpEffect effect(String mode, String how, double min, double max) {
        MemoryConfiguration cfg = new MemoryConfiguration();
        cfg.set("mode", mode);
        cfg.set("how", how);
        cfg.set("min", min);
        cfg.set("max", max);
        return new ExpEffect(cfg);
    }

    @Test
    void allModeDeletesEveryLevel() {
        player.setLevel(30);
        effect("ALL", "DELETE", 0, 0).onDeath(player, null, new NoopLogger());
        assertEquals(0, player.getLevel());
    }

    @Test
    void percentageModeDeletesShare() {
        player.setLevel(30);
        effect("PERCENTAGE", "DELETE", 50, 50).onDeath(player, null, new NoopLogger()); // lose 50% of 30 = 15
        assertEquals(15, player.getLevel());
    }

    @Test
    void simpleModeDeletesFixedAmount() {
        player.setLevel(10);
        effect("SIMPLE", "DELETE", 5, 5).onDeath(player, null, new NoopLogger()); // lose exactly 5
        assertEquals(5, player.getLevel());
    }

    @Test
    void noLevelsToLoseLeavesPlayerUnchanged() {
        player.setLevel(0);
        effect("ALL", "DELETE", 0, 0).onDeath(player, null, new NoopLogger()); // 0 levels -> early return
        assertEquals(0, player.getLevel());
    }
}
