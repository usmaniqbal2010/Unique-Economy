package com.uniqueeconomy.tasks;

import com.uniqueeconomy.UniqueEconomy;
import com.uniqueeconomy.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class MonthlyBankFeeTask extends BukkitRunnable {
    private final UniqueEconomy plugin;
    private int lastProcessedDay = -1;

    public MonthlyBankFeeTask(UniqueEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int dayOfMonth = plugin.getConfigManager().getConfig().getInt("fees.monthly-fee.day-of-month", 1);
        int currentDay = LocalDate.now().getDayOfMonth();

        if (currentDay == dayOfMonth && lastProcessedDay != currentDay) {
            lastProcessedDay = currentDay;
            processMonthlyFees();
        }
    }

    private void processMonthlyFees() {
        double feeAmount = plugin.getConfigManager().getConfig().getDouble("fees.monthly-fee.amount", 10.0);
        String currency = plugin.getConfigManager().getConfig().getString("fees.monthly-fee.currency", "USD");

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            PlayerData data = plugin.getDataManager().getPlayerData(uuid);

            BigDecimal fee = new BigDecimal(feeAmount);
            BigDecimal bankBalance = plugin.getBankManager().getBankBalance(uuid, currency);

            if (bankBalance.compareTo(fee) >= 0) {
                plugin.getBankManager().withdraw(uuid, currency, fee);
                
                String message = plugin.getConfigManager().getMessage("monthly-fee-applied")
                    .replace("{amount}", String.format("%.2f", fee.doubleValue()))
                    .replace("{currency}", currency);
                player.sendMessage(message);
            }
        }
    }
}