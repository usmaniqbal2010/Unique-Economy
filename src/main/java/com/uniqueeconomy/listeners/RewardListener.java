package com.uniqueeconomy.listeners;

import com.uniqueeconomy.UniqueEconomy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RewardListener implements Listener {
    private final UniqueEconomy plugin;
    private final Map<UUID, Integer> blocksBroken = new HashMap<>();

    public RewardListener(UniqueEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("rewards.mining.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        int count = blocksBroken.getOrDefault(uuid, 0) + 1;
        blocksBroken.put(uuid, count);

        int blocksPerReward = plugin.getConfigManager().getConfig().getInt("rewards.mining.blocks-per-reward", 100);
        if (count >= blocksPerReward) {
            blocksBroken.put(uuid, 0);

            double amount = plugin.getConfigManager().getConfig().getDouble("rewards.mining.amount", 1.0);
            String currency = plugin.getConfigManager().getConfig().getString("rewards.mining.currency", "USD");

            plugin.getEconomyManager().addBalance(uuid, currency, new BigDecimal(amount));
            player.sendMessage("§a+§e" + amount + " " + currency + " §a(Mining Reward)");
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("rewards.pvp.enabled", true)) {
            return;
        }

        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            double amount = plugin.getConfigManager().getConfig().getDouble("rewards.pvp.amount", 10.0);
            String currency = plugin.getConfigManager().getConfig().getString("rewards.pvp.currency", "USD");

            plugin.getEconomyManager().addBalance(killer.getUniqueId(), currency, new BigDecimal(amount));
            killer.sendMessage("§a+§e" + amount + " " + currency + " §a(PvP Kill Reward)");
        }
    }

    @EventHandler
    public void onMobKill(EntityDeathEvent event) {
        if (!plugin.getConfigManager().getConfig().getBoolean("rewards.mob-kill.enabled", true)) {
            return;
        }

        if (event.getEntity().getKiller() instanceof Player) {
            Player killer = (Player) event.getEntity().getKiller();
            double amount = plugin.getConfigManager().getConfig().getDouble("rewards.mob-kill.amount", 0.10);
            String currency = plugin.getConfigManager().getConfig().getString("rewards.mob-kill.currency", "USD");

            plugin.getEconomyManager().addBalance(killer.getUniqueId(), currency, new BigDecimal(amount));
        }
    }
}