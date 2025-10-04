package com.beepsterr.betterkeepinventory.Library;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import net.md_5.bungee.api.ChatColor;

import java.util.logging.Level;

public class NestedLogBuilder {

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

    public void SetDepth(int d){
        depth = d;
    }

    public void child(String title){

        // if we haven't created a root log entry yet there is no point in creating a child.
        if(depth == -1){
            logger.severe(title);
            return;
        }

        String msg = getPrefix(this.level).replace(LOG_ENTRY, LOG_CHILD) + " ";
        if(title == null || title.isEmpty()){
            msg = msg.replace(LOG_CHILD, LOG_CHILD_NO_TEXT);
        }else{
            msg += title;
        }
        logger.log(this.level, msg);
        depth++;

    }

    public void parent(){
        if(depth > 0){
            String msg = getPrefix().replace(LOG_ENTRY, LOG_END);
            logger.log(this.level, msg);
            depth--;
        }
    }

    public void log(Level level, String message){
        String msg = getPrefix() + " " + message;
        logger.log(level, msg);
    }

    public void log(String message){
        String msg = getPrefix() + " " + message;
        logger.log(this.level, msg);
    }

    public void cont(Level level, String message){
        String msg = getPrefix().replace(LOG_ENTRY, LOG_SPACER) + " " + message;
        logger.log(level, msg);
    }

    public void cont(String message){
        String msg = getPrefix().replace(LOG_ENTRY, LOG_SPACER) + " " + message;
        logger.log(this.level, msg);
    }

    public void spacer(){
        String msg = getPrefix().replace(LOG_ENTRY, LOG_SPACER) + " ";
        logger.log(this.level, msg);
    }

    public void end(){
        while(depth > 0){
            parent();
        }
        String msg = getPrefix().replace(LOG_ENTRY, LOG_END);
        logger.log(this.level, msg);
    }

}
