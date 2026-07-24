package com.beepsterr.betterkeepinventory.support;

import com.beepsterr.betterkeepinventory.api.LoggerInterface;

import java.util.logging.Level;

/**
 * A do-nothing {@link LoggerInterface} for tests, so effects/conditions can be
 * invoked directly without wiring up a real {@code NestedLogBuilder} (which
 * needs the plugin singleton). The production code paths under test don't rely
 * on the logger's output, only that calls don't NPE.
 */
public class NoopLogger implements LoggerInterface {
    @Override public void SetDepth(int d) { }
    @Override public void child(String title) { }
    @Override public void parent() { }
    @Override public void log(String message) { }
    @Override public void log(Level level, String message) { }
    @Override public void cont(String message) { }
}
