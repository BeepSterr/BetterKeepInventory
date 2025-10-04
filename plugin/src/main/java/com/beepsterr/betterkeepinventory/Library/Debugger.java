package com.beepsterr.betterkeepinventory.Library;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import org.bukkit.Bukkit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Debugger {

    private static final int MAX_MESSAGES = 1000;
    private String[] messages = new String[MAX_MESSAGES];
    private int messageCount = 0;
    private int nextIndex = 0;

    public Debugger() {
        reset();
    }

    private String getHeader(){
        String header = "=== SYSTEM INFO";
        header += "\nBukkit Version: " + Bukkit.getVersion();
        header += "\nMinecraft Version: " + Bukkit.getBukkitVersion();
        header += "\nPlugin Version: " + BetterKeepInventory.getInstance().getDescription().getVersion();
        header += "\nJava Version: " + System.getProperty("java.version");
        header += "\nOS: " + System.getProperty("os.name") + " "+ System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")";
        header += "\nCPU Cores: " + Runtime.getRuntime().availableProcessors();
        return header;
    }

    private String getConfig() {
        String prefix = "=== CONFIGURATION\n";
        File configFile = new File(BetterKeepInventory.instance.getDataFolder(), "config.yml");
        if(!configFile.exists()){
            return prefix + "No config file found.";
        }
        try {
            return prefix + java.nio.file.Files.readString(configFile.toPath());
        } catch (IOException e) {
            return prefix + "Error reading config file: " + e.getMessage();
        }
    }

    public void reset() {
        this.messages = new String[100];
    }

    public String TriggerUpload(){
        write();
        return upload();
    }

    private String getContent(){
        String log = "BetterKeepInventory DDF 1.0\n";
        log += getHeader() + "\n\n";
        log += getConfig() + "\n\n";
        log += "=== LOGS\n";

        int start = (messageCount == MAX_MESSAGES) ? nextIndex : 0;
        int count = messageCount;
        for (int i = 0; i < count; i++) {
            int idx = (start + i) % MAX_MESSAGES;
            log += messages[idx] + "\n";
        }
        return log;
    }

    private void write() {

        String log = getContent();

        File dataFolder = BetterKeepInventory.instance.getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File file = new File(dataFolder, "debugger.txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(log);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String upload(){
        String log = getContent();
        try {
            java.net.URL url = new java.net.URL("https://bin.lunega.dev/post");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
            conn.setDoOutput(true);
            try (java.io.OutputStream os = conn.getOutputStream()) {
                byte[] input = log.getBytes("UTF-8");
                os.write(input, 0, input.length);
            }
            int code = conn.getResponseCode();
            if (code == 200 || code == 201) {
                try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    JsonObject obj = JsonParser.parseReader(br).getAsJsonObject();
                    if (obj.has("key")) {
                        String key = obj.get("key").getAsString();
                        BetterKeepInventory.getInstance().getLogger().info("Debugger log uploaded: https://bin.lunega.dev/" + key);
                        return key;
                    } else {
                        BetterKeepInventory.getInstance().getLogger().warning("Debugger upload succeeded but response did not contain a bin key.");
                    }
                }
            } else {
                BetterKeepInventory.getInstance().getLogger().warning("Debugger upload failed with HTTP code: " + code);
            }
        } catch (Exception e) {
            BetterKeepInventory.getInstance().getLogger().severe("Debugger upload error: " + e.getMessage());
        }

        return null;

    }

}
