package com.uniqueeconomy.data;

import com.uniqueeconomy.UniqueEconomy;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final UniqueEconomy plugin;
    private FileConfiguration config;
    private FileConfiguration messages;
    private FileConfiguration currencies;
    private FileConfiguration guiMenus;

    public ConfigManager(UniqueEconomy plugin) {
        this.plugin = plugin;
    }

    public void loadConfigs() {
        plugin.saveDefaultConfig();
        config = plugin.getConfig();
        
        messages = loadConfig("messages.yml");
        currencies = loadConfig("currencies.yml");
        guiMenus = loadConfig("gui_menus.yml");
    }

    private FileConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    public void reload() {
        plugin.reloadConfig();
        loadConfigs();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public FileConfiguration getMessages() {
        return messages;
    }

    public FileConfiguration getCurrencies() {
        return currencies;
    }

    public FileConfiguration getGuiMenus() {
        return guiMenus;
    }

    public String getMessage(String path) {
        String msg = messages.getString(path, path);
        String prefix = messages.getString("prefix", "");
        return msg.replace("{prefix}", prefix).replace("&", "§");
    }

    public String getCurrencySymbol(String currencyCode) {
        return currencies.getString("currencies." + currencyCode + ".symbol", currencyCode);
    }

    public String getCurrencyName(String currencyCode) {
        return currencies.getString("currencies." + currencyCode + ".name", currencyCode);
    }

    public Map<String, String> getCountryCurrencyMap() {
        Map<String, String> map = new HashMap<>();
        if (currencies.getConfigurationSection("country-currency-map") != null) {
            for (String country : currencies.getConfigurationSection("country-currency-map").getKeys(false)) {
                map.put(country, currencies.getString("country-currency-map." + country));
            }
        }
        return map;
    }
}