package com.beepsterr.betterkeepinventory.api;

public interface LoggerInterface {

    public void SetDepth(int d);
    public void child(String title);
    public void parent();
    public void log(String message);
    public void log(java.util.logging.Level level, String message);
    public void cont(String message);

}
