package com.uniqueeconomy;

import com.uniqueeconomy.commands.BankCommand;
import com.uniqueeconomy.commands.EcoCommand;
import com.uniqueeconomy.commands.UniqueEcoCommand;
import com.uniqueeconomy.data.ConfigManager;
import com.uniqueeconomy.data.DataManager;
import com.uniqueeconomy.economy.BankManager;
import com.uniqueeconomy.economy.CurrencyConverter;
import com.uniqueeconomy.economy.EconomyManager;
import com.uniqueeconomy.economy.LeaderboardManager;
import com.uniqueeconomy.economy.PlaceholderHook;
import com.uniqueeconomy.economy.VaultEconomyProvider;
import com.uniqueeconomy.listeners.PlayerJoinListener;
import com.uniqueeconomy.listeners.RewardListener;
import com.uniqueeconomy.tasks.AutoSaveTask;
import com.uniqueeconomy.tasks.DailyDeductionTask;
import com.uniqueeconomy.tasks.MonthlyBankFeeTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;

public class UniqueEconomy extends JavaPlugin {
    private static UniqueEconomy instance;
    private ConfigManager configManager;
    private DataManager dataManager;
    private EconomyManager economyManager;
    private BankManager bankManager;
    private CurrencyConverter currencyConverter;
    private LeaderboardManager leaderboardManager;
    private VaultEconomyProvider vaultProvider;

    @Override
    public void onEnable() {
        instance = this;
        
        configManager = new ConfigManager(this);
        configManager.loadConfigs();
        
        dataManager = new DataManager(this);
        dataManager.initialize();
        
        currencyConverter = new CurrencyConverter(this);
        economyManager = new EconomyManager(this);
        bankManager = new BankManager(this);
        leaderboardManager = new LeaderboardManager(this);
        
        registerCommands();
        registerListeners();
        registerVault();
        registerPlaceholders();
        scheduleTasks();
        
        disableEssentialsEconomy();
        
        getLogger().info("UniqueEconomy has been enabled!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) {
            dataManager.saveAll();
            dataManager.shutdown();
        }
        getLogger().info("UniqueEconomy has been disabled!");
    }

    private void registerCommands() {
        getCommand("eco").setExecutor(new EcoCommand(this));
        getCommand("bank").setExecutor(new BankCommand(this));
        getCommand("uniqueeco").setExecutor(new UniqueEcoCommand(this));
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new RewardListener(this), this);
    }

    private void registerVault() {
        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            vaultProvider = new VaultEconomyProvider(this);
            getServer().getServicesManager().register(
                Economy.class,
                vaultProvider,
                this,
                ServicePriority.Highest
            );
            getLogger().info("Vault integration enabled!");
        }
    }

    private void registerPlaceholders() {
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(this).register();
            getLogger().info("PlaceholderAPI integration enabled!");
        }
    }

    private void scheduleTasks() {
        int autoSaveInterval = configManager.getConfig().getInt("auto-save-interval-minutes", 5) * 20 * 60;
        new AutoSaveTask(this).runTaskTimerAsynchronously(this, autoSaveInterval, autoSaveInterval);
        
        if (configManager.getConfig().getBoolean("fees.daily-penalty.enabled", true)) {
            new DailyDeductionTask(this).runTaskTimerAsynchronously(this, 20 * 60, 20 * 60 * 60);
        }
        
        if (configManager.getConfig().getBoolean("fees.monthly-fee.enabled", true)) {
            new MonthlyBankFeeTask(this).runTaskTimerAsynchronously(this, 20 * 60, 20 * 60 * 60);
        }
    }

    private void disableEssentialsEconomy() {
        if (getServer().getPluginManager().getPlugin("Essentials") != null) {
            getLogger().info("Detected Essentials - UniqueEconomy will override its economy");
        }
    }

    public static UniqueEconomy getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }

    public BankManager getBankManager() {
        return bankManager;
    }

    public CurrencyConverter getCurrencyConverter() {
        return currencyConverter;
    }

    public LeaderboardManager getLeaderboardManager() {
        return leaderboardManager;
    }
}