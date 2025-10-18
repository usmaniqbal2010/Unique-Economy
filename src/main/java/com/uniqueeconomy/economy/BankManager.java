package com.uniqueeconomy.economy;

import com.uniqueeconomy.UniqueEconomy;
import com.uniqueeconomy.data.PlayerData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

public class BankManager {
    private final UniqueEconomy plugin;

    public BankManager(UniqueEconomy plugin) {
        this.plugin = plugin;
    }

    public BigDecimal getBankBalance(UUID uuid, String currency) {
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        return data.getBankBalance(currency);
    }

    public boolean deposit(UUID uuid, String currency, BigDecimal amount) {
        if (plugin.getEconomyManager().removeBalance(uuid, currency, amount)) {
            PlayerData data = plugin.getDataManager().getPlayerData(uuid);
            BigDecimal current = data.getBankBalance(currency);
            data.setBankBalance(currency, round(current.add(amount)));
            plugin.getDataManager().logTransaction(uuid, "BANK_DEPOSIT", currency, amount);
            return true;
        }
        return false;
    }

    public boolean withdraw(UUID uuid, String currency, BigDecimal amount) {
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        BigDecimal current = data.getBankBalance(currency);
        
        if (current.compareTo(amount) < 0) {
            return false;
        }
        
        data.setBankBalance(currency, round(current.subtract(amount)));
        plugin.getEconomyManager().addBalance(uuid, currency, amount);
        plugin.getDataManager().logTransaction(uuid, "BANK_WITHDRAW", currency, amount);
        return true;
    }

    public boolean transfer(UUID from, UUID to, String currency, BigDecimal amount) {
        PlayerData fromData = plugin.getDataManager().getPlayerData(from);
        BigDecimal current = fromData.getBankBalance(currency);
        
        if (current.compareTo(amount) < 0) {
            return false;
        }
        
        fromData.setBankBalance(currency, round(current.subtract(amount)));
        
        PlayerData toData = plugin.getDataManager().getPlayerData(to);
        BigDecimal toCurrent = toData.getBankBalance(currency);
        toData.setBankBalance(currency, round(toCurrent.add(amount)));
        
        plugin.getDataManager().logTransaction(from, "BANK_TRANSFER_OUT", currency, amount);
        plugin.getDataManager().logTransaction(to, "BANK_TRANSFER_IN", currency, amount);
        return true;
    }

    public void applyInterest(UUID uuid, String currency) {
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        BigDecimal balance = data.getBankBalance(currency);
        double interestRate = plugin.getConfigManager().getConfig().getDouble("bank.interest-rate", 0.05);
        
        BigDecimal interest = balance.multiply(new BigDecimal(interestRate));
        data.setBankBalance(currency, round(balance.add(interest)));
        plugin.getDataManager().logTransaction(uuid, "INTEREST", currency, interest);
    }

    private BigDecimal round(BigDecimal value) {
        String mode = plugin.getConfigManager().getConfig().getString("exchange.rounding-mode", "HALF_UP");
        return value.setScale(2, RoundingMode.valueOf(mode));
    }
}