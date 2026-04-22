package com.beepsterr.betterkeepinventory.Library;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.beepsterr.betterkeepinventory.api.LoggerInterface;
import net.md_5.bungee.api.ChatColor;

import java.util.logging.Level;

public class NestedLogBuilder implements LoggerInterface {

    public static final String LOG_ROOT             = "┌";
    public static final String LOG_SPACER           = "│";
    public static final String LOG_ENTRY            = "├";
    public static final String LOG_CHILD            = "├─┬";
    public static final String LOG_CHILD_NO_TEXT    = "├─┐";
    public static final String LOG_END              = "╵";

    private int depth = -1;
    private Level level = Level.INFO;
    private final java.util.logging.Logger logger;

    public NestedLogBuilder(Level level){
        this.logger = BetterKeepInventory.getInstance().getLogger();
        this.level = level != null ? level : Level.INFO;
    }
    public NestedLogBuilder(){
        this.logger = BetterKeepInventory.getInstance().getLogger();
    }

    private String getPrefix(){
        if(depth == -1){
            depth = 0;
            return LOG_ROOT;
        }

        StringBuilder prefix = new StringBuilder(LOG_ENTRY);
        for(int i = 0; i < depth; i++){
            prefix.insert(0, LOG_SPACER + " ");
        }

        return prefix.toString();
    }

    // INFO and above always reach the console; sub-INFO (FINE/FINER/FINEST) only when debug is on.
    // Before Config finishes loading (startup), fall back to "print" so early failures are visible.
    private boolean shouldPrint(Level level){
        if(level.intValue() >= Level.INFO.intValue()){
            return true;
        }
        Config cfg = Config.getInstance();
        return cfg == null || cfg.isDebug();
    }

    private void print(Level level, String msg){
        if(shouldPrint(level)){
            // Bukkit's plugin logger inherits the root INFO threshold, so emit sub-INFO
            // messages at INFO when debug is enabled — otherwise JUL would drop them silently.
            Level emitAt = level.intValue() < Level.INFO.intValue() ? Level.INFO : level;
            logger.log(emitAt, msg);
        }
        BetterKeepInventory.getInstance().debugger.AddLine(msg);
    }

    public void SetDepth(int d){
        depth = d;
    }

    public void child(String title){

        // if we haven't created a root log entry yet there is no point in creating a child.
        if(depth == -1){
            logger.severe(title);
            return;
        }

        String msg = getPrefix().replace(LOG_ENTRY, LOG_CHILD) + " ";
        if(title == null || title.isEmpty()){
            msg = msg.replace(LOG_CHILD, LOG_CHILD_NO_TEXT);
        }else{
            msg += title;
        }
        print(this.level, msg);
        depth++;

    }

    public void parent(){
        if(depth > 0){
            String msg = getPrefix().replace(LOG_ENTRY, LOG_END);
            print(this.level, msg);
            depth--;
        }
    }

    public void log(Level level, String message){
        String msg = getPrefix() + " " + message;
        print(level, msg);
    }

    public void log(String message){
        String msg = getPrefix() + " " + message;
        print(this.level, msg);
    }

    public void cont(Level level, String message){
        String msg = getPrefix().replace(LOG_ENTRY, LOG_SPACER) + " " + message;
        print(level, msg);
    }

    public void cont(String message){
        String msg = getPrefix().replace(LOG_ENTRY, LOG_SPACER) + " " + message;
        print(this.level, msg);
    }

    public void spacer(){
        String msg = getPrefix().replace(LOG_ENTRY, LOG_SPACER) + " ";
        print(this.level, msg);
    }

    public void end(){
        while(depth > 0){
            parent();
        }
        String msg = getPrefix().replace(LOG_ENTRY, LOG_END);
        print(this.level, msg);
    }

}
