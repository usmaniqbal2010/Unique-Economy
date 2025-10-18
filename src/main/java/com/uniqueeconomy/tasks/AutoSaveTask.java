package com.uniqueeconomy.tasks;

import com.uniqueeconomy.UniqueEconomy;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoSaveTask extends BukkitRunnable {
    private final UniqueEconomy plugin;

    public AutoSaveTask(UniqueEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        plugin.getDataManager().saveAll();
    }
}