package com.uniqueeconomy.tasks;

import com.uniqueeconomy.UniqueEconomy;
import com.uniqueeconomy.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.util.UUID;

public class DailyDeductionTask extends BukkitRunnable {
    private final UniqueEconomy plugin;

    public DailyDeductionTask(UniqueEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        double penaltyAmount = plugin.getConfigManager().getConfig().getDouble("fees.daily-penalty.amount", 5.0);
        String currency = plugin.getConfigManager().getConfig().getString("fees.daily-penalty.currency", "USD");
        int daysInactive = plugin.getConfigManager().getConfig().getInt("fees.daily-penalty.days-inactive", 7);

        long inactiveThreshold = System.currentTimeMillis() - (daysInactive * 24L * 60 * 60 * 1000);

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            PlayerData data = plugin.getDataManager().getPlayerData(uuid);

            if (data.getLastLogin() < inactiveThreshold) {
                BigDecimal penalty = new BigDecimal(penaltyAmount);
                
                if (plugin.getEconomyManager().removeBalance(uuid, currency, penalty)) {
                    String message = plugin.getConfigManager().getMessage("daily-penalty-applied")
                        .replace("{amount}", String.format("%.2f", penalty.doubleValue()))
                        .replace("{currency}", currency);
                    player.sendMessage(message);
                }
            }
        }
    }
}