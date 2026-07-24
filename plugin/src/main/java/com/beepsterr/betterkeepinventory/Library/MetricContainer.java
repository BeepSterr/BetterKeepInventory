package com.beepsterr.betterkeepinventory.Library;

import com.beepsterr.betterkeepinventory.BetterKeepInventory;
import org.bstats.charts.SingleLineChart;
import org.bstats.bukkit.Metrics;

import java.util.concurrent.Callable;

public class MetricContainer {

    public int deathsProcessed = 0;
    public int durabilityPointsLost = 0;

    Metrics metrics;
    public MetricContainer(){
        // bStats reporting is best-effort: if it can't initialize (e.g. the class
        // isn't relocated yet under tests, or bStats is disabled), keep the plugin
        // and the in-memory counters working instead of failing onEnable.
        try {
            metrics = new Metrics(BetterKeepInventory.getInstance(), 11596);

            metrics.addCustomChart(new SingleLineChart("deaths_processed", new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    int amount = deathsProcessed;
                    deathsProcessed = 0;
                    return amount;
                }
            }));

            metrics.addCustomChart(new SingleLineChart("durability_points_lost", new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    int amount = durabilityPointsLost;
                    durabilityPointsLost = 0;
                    return amount;
                }
            }));
        } catch (Throwable t) {
            BetterKeepInventory.getInstance().getLogger().warning("bStats metrics disabled: " + t.getMessage());
        }

    }
}
