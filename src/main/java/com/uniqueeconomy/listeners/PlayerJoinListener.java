package com.uniqueeconomy.listeners;

import com.uniqueeconomy.UniqueEconomy;
import com.uniqueeconomy.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.math.BigDecimal;
import java.util.Map;

public class PlayerJoinListener implements Listener {
    private final UniqueEconomy plugin;

    public PlayerJoinListener(UniqueEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());

        data.setName(player.getName());
        data.setLastSeenIp(player.getAddress().getAddress().getHostAddress());
        data.setLastLogin(System.currentTimeMillis());

        if (data.getPrimaryCurrency() == null || data.getPrimaryCurrency().isEmpty()) {
            String currency = detectCurrency(player);
            data.setPrimaryCurrency(currency);

            double startingAmount = plugin.getConfigManager().getConfig().getDouble("starting-balance.amount", 100.0);
            String startingCurrency = plugin.getConfigManager().getConfig().getString("starting-balance.currency", "USD");

            BigDecimal amount;
            if (startingCurrency.equals(currency)) {
                amount = new BigDecimal(startingAmount);
            } else {
                amount = plugin.getCurrencyConverter().convert(
                    new BigDecimal(startingAmount),
                    startingCurrency,
                    currency
                );
            }

            plugin.getEconomyManager().setBalance(player.getUniqueId(), currency, amount);

            String message = plugin.getConfigManager().getMessage("welcome-balance")
                .replace("{amount}", String.format("%.2f", amount.doubleValue()))
                .replace("{currency}", currency);
            player.sendMessage(message);
        }

        plugin.getDataManager().savePlayerData(player.getUniqueId());
    }

    private String detectCurrency(Player player) {
        String geoipMode = plugin.getConfigManager().getConfig().getString("geoip.mode", "server");
        String fallbackCurrency = plugin.getConfigManager().getConfig().getString("geoip.fallback-currency", "USD");

        Map<String, String> countryMap = plugin.getConfigManager().getCountryCurrencyMap();

        if (geoipMode.equalsIgnoreCase("player")) {
            String ip = player.getAddress().getAddress().getHostAddress();
            String country = detectCountryFromIP(ip);
            return countryMap.getOrDefault(country, fallbackCurrency);
        }

        return fallbackCurrency;
    }

    private String detectCountryFromIP(String ip) {
        return "US";
    }
}