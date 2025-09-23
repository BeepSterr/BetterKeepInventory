package com.beepsterr.betterkeepinventory.Library.Versions;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tcoded.folialib.FoliaLib;
import org.bukkit.Bukkit;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionChecker {

    public static final String URL_STABLE = "https://raw.githubusercontent.com/BeepSterr/BetterKeepInventory/refs/heads/master/versions/stable.txt";
    public static final String URL_SNAPSHOT = "https://api.github.com/repos/beepsterr/BetterKeepInventory/actions/artifacts";
    public static final String URL_LATEST = "https://api.spigotmc.org/legacy/update.php?resource=93081";

    public Version foundVersion;
    public VersionChannel channel;
    public VersionChecker(VersionChannel channel) {
        this.channel = channel;

        BetterKeepInventory.getScheduler().getScheduler().runTimerAsync(() -> {
            try {
                foundVersion = getLatestVersion(channel);
            } catch (IOException e) {
                BetterKeepInventory.getInstance().log("Failed to check for updates: " + e.getMessage());
            }
        }, 0L, 20L * 14400); // Using every 4 hours
    }

    public boolean IsUpdateAvailable() {
        if (foundVersion == null) return false;
        Version current = BetterKeepInventory.getInstance().version;
        return foundVersion.compareTo(current) > 0;
    }

    private static Version getLatestFromUrl(String urlStr) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        conn.setRequestProperty("User-Agent", "BetterKeepInventory-VersionChecker");

        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line = in.readLine();
            BetterKeepInventory.getInstance().log("Update checker found version: " + line);
            return new Version(line);
        }
    }

    public static Version getLatestVersion(VersionChannel channel) throws IOException {
        return switch (channel) {
            case SNAPSHOT -> getLatestSnapshotVersion();
            case LATEST -> getLatestFromUrl(URL_LATEST);
            case STABLE -> getLatestFromUrl(URL_STABLE);
            default -> null;
        };
    }

    private static Version getLatestSnapshotVersion() throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(URL_SNAPSHOT).openConnection();
        conn.setRequestProperty("User-Agent", "BetterKeepInventory-VersionChecker");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray artifacts = obj.getAsJsonArray("artifacts");

            if (artifacts == null || artifacts.size() == 0) {
                BetterKeepInventory.getInstance().log("No artifacts found in snapshot response (?)");
                return null;
            }

            JsonObject latest = artifacts.get(0).getAsJsonObject();
            String name = latest.get("name").getAsString();
            String versionName = name.replaceFirst("BetterKeepInventory-", "");
            Version newVersion = new Version(versionName);

            if(newVersion.compareTo(BetterKeepInventory.getInstance().version) > 0) {
                BetterKeepInventory.getInstance().log("Update checker found new version: " + newVersion);
            }

            return newVersion;
        }
    }

    public void CancelCheck() {
        BetterKeepInventory.getScheduler().getScheduler().cancelAllTasks();

    }
}
