package com.uniqueeconomy.economy;

import com.uniqueeconomy.UniqueEconomy;
import com.uniqueeconomy.data.PlayerData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public class EconomyManager {
    private final UniqueEconomy plugin;

    public EconomyManager(UniqueEconomy plugin) {
        this.plugin = plugin;
    }

    public BigDecimal getBalance(UUID uuid, String currency) {
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        return data.getWalletBalance(currency);
    }

    public void setBalance(UUID uuid, String currency, BigDecimal amount) {
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        data.setWalletBalance(currency, round(amount));
        plugin.getDataManager().logTransaction(uuid, "SET", currency, amount);
    }

    public void addBalance(UUID uuid, String currency, BigDecimal amount) {
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        BigDecimal current = data.getWalletBalance(currency);
        data.setWalletBalance(currency, round(current.add(amount)));
        plugin.getDataManager().logTransaction(uuid, "ADD", currency, amount);
    }

    public boolean removeBalance(UUID uuid, String currency, BigDecimal amount) {
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        BigDecimal current = data.getWalletBalance(currency);
        
        if (current.compareTo(amount) < 0) {
            return false;
        }
        
        data.setWalletBalance(currency, round(current.subtract(amount)));
        plugin.getDataManager().logTransaction(uuid, "REMOVE", currency, amount);
        return true;
    }

    public boolean transfer(UUID from, UUID to, String currency, BigDecimal amount) {
        if (removeBalance(from, currency, amount)) {
            addBalance(to, currency, amount);
            plugin.getDataManager().logTransaction(from, "TRANSFER_OUT", currency, amount);
            plugin.getDataManager().logTransaction(to, "TRANSFER_IN", currency, amount);
            return true;
        }
        return false;
    }

    public BigDecimal getPrimaryBalance(UUID uuid) {
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        return getBalance(uuid, data.getPrimaryCurrency());
    }

    private BigDecimal round(BigDecimal value) {
        String mode = plugin.getConfigManager().getConfig().getString("exchange.rounding-mode", "HALF_UP");
        return value.setScale(2, RoundingMode.valueOf(mode));
    }
}