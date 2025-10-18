package com.uniqueeconomy.commands;

import com.uniqueeconomy.UniqueEconomy;
import com.uniqueeconomy.data.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class BankCommand implements CommandExecutor {
    private final UniqueEconomy plugin;

    public BankCommand(UniqueEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showBankBalance(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "deposit":
                if (args.length < 2) {
                    player.sendMessage("§cUse: /bank deposit <amount>");
                    return true;
                }
                handleDeposit(player, args[1]);
                break;

            case "withdraw":
                if (args.length < 2) {
                    player.sendMessage("§cUse: /bank withdraw <amount>");
                    return true;
                }
                handleWithdraw(player, args[1]);
                break;

            case "exchange":
                if (args.length < 4) {
                    player.sendMessage("§cUse: /bank exchange <from> <to> <amount>");
                    return true;
                }
                handleExchange(player, args[1], args[2], args[3]);
                break;

            case "rates":
                showExchangeRates(player);
                break;

            default:
                showBankBalance(player);
        }

        return true;
    }

    private void showBankBalance(Player player) {
        PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
        String currency = data.getPrimaryCurrency();
        BigDecimal balance = plugin.getBankManager().getBankBalance(player.getUniqueId(), currency);
        
        player.sendMessage("§6§lBank Account");
        player.sendMessage("§7Balance: §e" + String.format("%.2f", balance.doubleValue()) + " " + currency);
    }

    private void handleDeposit(Player player, String amountStr) {
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
            String currency = data.getPrimaryCurrency();

            if (plugin.getBankManager().deposit(player.getUniqueId(), currency, amount)) {
                player.sendMessage(plugin.getConfigManager().getMessage("bank-deposit-success")
                    .replace("{amount}", String.format("%.2f", amount.doubleValue()))
                    .replace("{currency}", currency));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("pay-insufficient"));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
        }
    }

    private void handleWithdraw(Player player, String amountStr) {
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
            String currency = data.getPrimaryCurrency();

            if (plugin.getBankManager().withdraw(player.getUniqueId(), currency, amount)) {
                player.sendMessage(plugin.getConfigManager().getMessage("bank-withdraw-success")
                    .replace("{amount}", String.format("%.2f", amount.doubleValue()))
                    .replace("{currency}", currency));
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("bank-insufficient"));
            }
        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
        }
    }

    private void handleExchange(Player player, String fromCurrency, String toCurrency, String amountStr) {
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            fromCurrency = fromCurrency.toUpperCase();
            toCurrency = toCurrency.toUpperCase();

            BigDecimal fromBalance = plugin.getEconomyManager().getBalance(player.getUniqueId(), fromCurrency);
            if (fromBalance.compareTo(amount) < 0) {
                player.sendMessage(plugin.getConfigManager().getMessage("pay-insufficient"));
                return;
            }

            BigDecimal converted = plugin.getCurrencyConverter().convert(amount, fromCurrency, toCurrency);
            
            double feePercent = plugin.getConfigManager().getConfig().getDouble("exchange.fee-percent", 1.5);
            double feeFixed = plugin.getConfigManager().getConfig().getDouble("exchange.fee-fixed", 0.50);
            
            BigDecimal fee = converted.multiply(new BigDecimal(feePercent / 100.0))
                .add(new BigDecimal(feeFixed));
            BigDecimal finalAmount = converted.subtract(fee);

            plugin.getEconomyManager().removeBalance(player.getUniqueId(), fromCurrency, amount);
            plugin.getEconomyManager().addBalance(player.getUniqueId(), toCurrency, finalAmount);

            player.sendMessage(plugin.getConfigManager().getMessage("exchange-success")
                .replace("{from-amount}", String.format("%.2f", amount.doubleValue()))
                .replace("{from-currency}", fromCurrency)
                .replace("{to-amount}", String.format("%.2f", finalAmount.doubleValue()))
                .replace("{to-currency}", toCurrency));
            
            player.sendMessage(plugin.getConfigManager().getMessage("exchange-fee")
                .replace("{fee-amount}", String.format("%.2f", fee.doubleValue()))
                .replace("{fee-currency}", toCurrency));

        } catch (NumberFormatException e) {
            player.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
        }
    }

    private void showExchangeRates(Player player) {
        player.sendMessage("§6§lExchange Rates (USD Base)");
        player.sendMessage("§7EUR: §e" + String.format("%.4f", plugin.getCurrencyConverter().getExchangeRate("USD", "EUR").doubleValue()));
        player.sendMessage("§7GBP: §e" + String.format("%.4f", plugin.getCurrencyConverter().getExchangeRate("USD", "GBP").doubleValue()));
        player.sendMessage("§7PKR: §e" + String.format("%.4f", plugin.getCurrencyConverter().getExchangeRate("USD", "PKR").doubleValue()));
        player.sendMessage("§7INR: §e" + String.format("%.4f", plugin.getCurrencyConverter().getExchangeRate("USD", "INR").doubleValue()));
    }
}