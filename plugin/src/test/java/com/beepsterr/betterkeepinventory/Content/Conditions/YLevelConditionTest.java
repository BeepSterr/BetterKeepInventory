package com.beepsterr.betterkeepinventory.Content.Conditions;

import com.beepsterr.betterkeepinventory.support.NoopLogger;
import org.bukkit.Location;
import org.bukkit.configuration.MemoryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** YLevelCondition tests the death Y against a NumberRange — mock server, no plugin. */
class YLevelConditionTest {

    private ServerMock server;
    private WorldMock world;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    private static YLevelCondition condition(String range) {
        MemoryConfiguration cfg = new MemoryConfiguration();
        if (range != null) cfg.set("range", range);
        return new YLevelCondition(cfg);
    }

    private PlayerMock playerAtY(double y) {
        PlayerMock player = server.addPlayer();
        player.teleport(new Location(world, 0, y, 0));
        return player;
    }

    @Test
    void insideRangeMatches() {
        assertTrue(condition("50..150").check(playerAtY(100), null, null, new NoopLogger()));
    }

    @Test
    void outsideRangeDoesNotMatch() {
        assertFalse(condition("0..10").check(playerAtY(100), null, null, new NoopLogger()));
    }

    @Test
    void comparisonExpressions() {
        assertTrue(condition("> 90").check(playerAtY(100), null, null, new NoopLogger()));
        assertFalse(condition("< 0").check(playerAtY(100), null, null, new NoopLogger()));
    }

    @Test
    void belowZeroVoidDeath() {
        assertTrue(condition("< 0").check(playerAtY(-20), null, null, new NoopLogger()));
    }

    @Test
    void defaultRangeIsFullBuildHeight() {
        // default "0..320" when no range configured
        assertTrue(condition(null).check(playerAtY(100), null, null, new NoopLogger()));
        assertFalse(condition(null).check(playerAtY(400), null, null, new NoopLogger()));
    }
}
