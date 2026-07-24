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
 * HungerEffect stores a reduced hunger on death and re-applies it a few ticks after respawn,
 * so this loads the plugin (for the scheduler) and advances ticks.
 */
class HungerEffectTest {

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

    private static HungerEffect effect(int min, int amount) {
        MemoryConfiguration cfg = new MemoryConfiguration();
        cfg.set("min", min);
        cfg.set("amount", amount);
        return new HungerEffect(cfg);
    }

    @Test
    void restoresReducedHungerAfterRespawn() {
        player.setFoodLevel(20);
        HungerEffect effect = effect(0, 6);

        effect.onDeath(player, null, new NoopLogger());
        effect.onRespawn(player, null, new NoopLogger());
        server.getScheduler().performTicks(6); // respawn re-applies on a delayed task

        assertEquals(14, player.getFoodLevel());
    }

    @Test
    void neverDropsBelowConfiguredMinimum() {
        player.setFoodLevel(5);
        HungerEffect effect = effect(2, 6); // 5 - 6 = -1, floored to min 2

        effect.onDeath(player, null, new NoopLogger());
        effect.onRespawn(player, null, new NoopLogger());
        server.getScheduler().performTicks(6);

        assertEquals(2, player.getFoodLevel());
    }
}
