package com.uniqueeconomy.commands;

import com.uniqueeconomy.UniqueEconomy;
import com.uniqueeconomy.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class EcoCommand implements CommandExecutor {
    private final UniqueEconomy plugin;

    public EcoCommand(UniqueEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (sender instanceof Player) {
                showBalance((Player) sender, (Player) sender);
            } else {
                sender.sendMessage("§cUse: /eco <subcommand>");
            }
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "balance":
            case "bal":
                if (sender instanceof Player) {
                    Player target = args.length > 1 ? Bukkit.getPlayer(args[1]) : (Player) sender;
                    if (target != null) {
                        showBalance(sender, target);
                    } else {
                        sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
                    }
                }
                break;

            case "pay":
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can use this command");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUse: /eco pay <player> <amount>");
                    return true;
                }
                handlePay((Player) sender, args[1], args[2]);
                break;

            case "give":
                if (!sender.hasPermission("unique.eco.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage("§cUse: /eco give <player> <amount> <currency>");
                    return true;
                }
                handleGive(sender, args[1], args[2], args[3]);
                break;

            case "take":
                if (!sender.hasPermission("unique.eco.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage("§cUse: /eco take <player> <amount> <currency>");
                    return true;
                }
                handleTake(sender, args[1], args[2], args[3]);
                break;

            case "set":
                if (!sender.hasPermission("unique.eco.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage("§cUse: /eco set <player> <amount> <currency>");
                    return true;
                }
                handleSet(sender, args[1], args[2], args[3]);
                break;

            case "reload":
                if (!sender.hasPermission("unique.eco.admin")) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
                    return true;
                }
                plugin.getConfigManager().reload();
                plugin.getCurrencyConverter().reload();
                sender.sendMessage("§aConfiguration reloaded!");
                break;

            default:
                sender.sendMessage("§cUnknown subcommand!");
        }

        return true;
    }

    private void showBalance(CommandSender sender, Player target) {
        PlayerData data = plugin.getDataManager().getPlayerData(target.getUniqueId());
        String currency = data.getPrimaryCurrency();
        BigDecimal balance = plugin.getEconomyManager().getBalance(target.getUniqueId(), currency);
        
        String message = plugin.getConfigManager().getMessage("balance")
            .replace("{amount}", String.format("%.2f", balance.doubleValue()))
            .replace("{currency}", currency)
            .replace("{player}", target.getName());
        
        sender.sendMessage(message);
    }

    private void handlePay(Player sender, String targetName, String amountStr) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountStr);
            PlayerData senderData = plugin.getDataManager().getPlayerData(sender.getUniqueId());
            String currency = senderData.getPrimaryCurrency();

            if (plugin.getEconomyManager().transfer(sender.getUniqueId(), target.getUniqueId(), currency, amount)) {
                sender.sendMessage(plugin.getConfigManager().getMessage("pay-success")
                    .replace("{amount}", String.format("%.2f", amount.doubleValue()))
                    .replace("{currency}", currency)
                    .replace("{player}", target.getName()));
                
                target.sendMessage(plugin.getConfigManager().getMessage("pay-received")
                    .replace("{amount}", String.format("%.2f", amount.doubleValue()))
                    .replace("{currency}", currency)
                    .replace("{player}", sender.getName()));
            } else {
                sender.sendMessage(plugin.getConfigManager().getMessage("pay-insufficient"));
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
        }
    }

    private void handleGive(CommandSender sender, String targetName, String amountStr, String currency) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountStr);
            plugin.getEconomyManager().addBalance(target.getUniqueId(), currency.toUpperCase(), amount);
            sender.sendMessage("§aGave " + amount + " " + currency + " to " + target.getName());
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
        }
    }

    private void handleTake(CommandSender sender, String targetName, String amountStr, String currency) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountStr);
            if (plugin.getEconomyManager().removeBalance(target.getUniqueId(), currency.toUpperCase(), amount)) {
                sender.sendMessage("§aTook " + amount + " " + currency + " from " + target.getName());
            } else {
                sender.sendMessage("§cPlayer doesn't have enough balance!");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
        }
    }

    private void handleSet(CommandSender sender, String targetName, String amountStr, String currency) {
        Player target = Bukkit.getPlayer(targetName);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("player-not-found"));
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountStr);
            plugin.getEconomyManager().setBalance(target.getUniqueId(), currency.toUpperCase(), amount);
            sender.sendMessage("§aSet " + target.getName() + "'s balance to " + amount + " " + currency);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("invalid-amount"));
        }
    }
}