package com.uniqueeconomy.economy;

import com.uniqueeconomy.UniqueEconomy;
import com.uniqueeconomy.data.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class PlaceholderHook extends PlaceholderExpansion {
    private final UniqueEconomy plugin;

    public PlaceholderHook(UniqueEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "uniqueeco";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Usman Iqbal";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return "";
        }

        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());

        if (params.equals("balance")) {
            BigDecimal balance = plugin.getEconomyManager().getPrimaryBalance(player.getUniqueId());
            return String.format("%.2f", balance.doubleValue());
        }

        if (params.equals("currency")) {
            return data.getPrimaryCurrency();
        }

        if (params.equals("bank_balance")) {
            BigDecimal balance = plugin.getBankManager().getBankBalance(player.getUniqueId(), data.getPrimaryCurrency());
            return String.format("%.2f", balance.doubleValue());
        }

        if (params.startsWith("balance_")) {
            String currency = params.substring(8).toUpperCase();
            BigDecimal balance = plugin.getEconomyManager().getBalance(player.getUniqueId(), currency);
            return String.format("%.2f", balance.doubleValue());
        }

        return null;
    }
}