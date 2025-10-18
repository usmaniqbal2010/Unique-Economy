package com.uniqueeconomy.commands;

import com.uniqueeconomy.UniqueEconomy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class UniqueEcoCommand implements CommandExecutor {
    private final UniqueEconomy plugin;

    public UniqueEcoCommand(UniqueEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("unique.eco.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§6UniqueEconomy Admin Commands:");
            sender.sendMessage("§7/uniqueeco reload - Reload configuration");
            sender.sendMessage("§7/uniqueeco license - License management");
            sender.sendMessage("§7/uniqueeco export - Export database");
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload":
                plugin.getConfigManager().reload();
                plugin.getCurrencyConverter().reload();
                sender.sendMessage("§aConfiguration reloaded successfully!");
                break;

            case "license":
                sender.sendMessage("§cLicense management not yet implemented");
                break;

            case "export":
                sender.sendMessage("§cDatabase export not yet implemented");
                break;

            default:
                sender.sendMessage("§cUnknown subcommand!");
        }

        return true;
    }
}