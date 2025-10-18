package com.uniqueeconomy.economy;

import com.uniqueeconomy.UniqueEconomy;
import org.bukkit.configuration.file.FileConfiguration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

public class CurrencyConverter {
    private final UniqueEconomy plugin;
    private final Map<String, BigDecimal> exchangeRates = new HashMap<>();

    public CurrencyConverter(UniqueEconomy plugin) {
        this.plugin = plugin;
        loadExchangeRates();
    }

    private void loadExchangeRates() {
        FileConfiguration currencies = plugin.getConfigManager().getCurrencies();
        if (currencies.getConfigurationSection("exchange-rates") != null) {
            for (String key : currencies.getConfigurationSection("exchange-rates").getKeys(false)) {
                double rate = currencies.getDouble("exchange-rates." + key);
                exchangeRates.put(key, new BigDecimal(rate));
            }
        }
    }

    public BigDecimal convert(BigDecimal amount, String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return amount;
        }

        String key = fromCurrency + "-" + toCurrency;
        String reverseKey = toCurrency + "-" + fromCurrency;

        BigDecimal rate;
        if (exchangeRates.containsKey(key)) {
            rate = exchangeRates.get(key);
        } else if (exchangeRates.containsKey(reverseKey)) {
            rate = BigDecimal.ONE.divide(exchangeRates.get(reverseKey), 8, RoundingMode.HALF_UP);
        } else {
            BigDecimal fromUSD = convertToUSD(amount, fromCurrency);
            return convertFromUSD(fromUSD, toCurrency);
        }

        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal convertToUSD(BigDecimal amount, String currency) {
        if (currency.equals("USD")) {
            return amount;
        }
        
        String key = currency + "-USD";
        if (exchangeRates.containsKey(key)) {
            return amount.multiply(exchangeRates.get(key));
        }
        
        String reverseKey = "USD-" + currency;
        if (exchangeRates.containsKey(reverseKey)) {
            return amount.divide(exchangeRates.get(reverseKey), 8, RoundingMode.HALF_UP);
        }
        
        return amount;
    }

    private BigDecimal convertFromUSD(BigDecimal amount, String currency) {
        if (currency.equals("USD")) {
            return amount;
        }
        
        String key = "USD-" + currency;
        if (exchangeRates.containsKey(key)) {
            return amount.multiply(exchangeRates.get(key));
        }
        
        String reverseKey = currency + "-USD";
        if (exchangeRates.containsKey(reverseKey)) {
            return amount.divide(exchangeRates.get(reverseKey), 8, RoundingMode.HALF_UP);
        }
        
        return amount;
    }

    public BigDecimal getExchangeRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return BigDecimal.ONE;
        }

        String key = fromCurrency + "-" + toCurrency;
        if (exchangeRates.containsKey(key)) {
            return exchangeRates.get(key);
        }

        String reverseKey = toCurrency + "-" + fromCurrency;
        if (exchangeRates.containsKey(reverseKey)) {
            return BigDecimal.ONE.divide(exchangeRates.get(reverseKey), 8, RoundingMode.HALF_UP);
        }

        return BigDecimal.ONE;
    }

    public void reload() {
        exchangeRates.clear();
        loadExchangeRates();
    }
}