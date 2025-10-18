package com.uniqueeconomy.data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private String name;
    private String primaryCurrency;
    private String country;
    private String lastSeenIp;
    private long lastLogin;
    
    private final Map<String, BigDecimal> walletBalances = new HashMap<>();
    private final Map<String, BigDecimal> bankBalances = new HashMap<>();

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.primaryCurrency = "USD";
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrimaryCurrency() {
        return primaryCurrency;
    }

    public void setPrimaryCurrency(String primaryCurrency) {
        this.primaryCurrency = primaryCurrency;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLastSeenIp() {
        return lastSeenIp;
    }

    public void setLastSeenIp(String lastSeenIp) {
        this.lastSeenIp = lastSeenIp;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public BigDecimal getWalletBalance(String currency) {
        return walletBalances.getOrDefault(currency, BigDecimal.ZERO);
    }

    public void setWalletBalance(String currency, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            walletBalances.remove(currency);
        } else {
            walletBalances.put(currency, amount);
        }
    }

    public BigDecimal getBankBalance(String currency) {
        return bankBalances.getOrDefault(currency, BigDecimal.ZERO);
    }

    public void setBankBalance(String currency, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            bankBalances.remove(currency);
        } else {
            bankBalances.put(currency, amount);
        }
    }

    public Map<String, BigDecimal> getWalletBalances() {
        return walletBalances;
    }

    public Map<String, BigDecimal> getBankBalances() {
        return bankBalances;
    }
}