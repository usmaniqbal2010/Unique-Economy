package com.uniqueeconomy.data;

import com.uniqueeconomy.UniqueEconomy;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataManager {
    private final UniqueEconomy plugin;
    private HikariDataSource dataSource;
    private final Map<UUID, PlayerData> cache = new ConcurrentHashMap<>();
    private boolean usingSQLite;

    public DataManager(UniqueEconomy plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        String dbType = plugin.getConfigManager().getConfig().getString("database.type", "SQLITE");
        usingSQLite = dbType.equalsIgnoreCase("SQLITE");

        HikariConfig config = new HikariConfig();
        
        if (usingSQLite) {
            config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder() + "/database.db");
            config.setDriverClassName("org.sqlite.JDBC");
        } else {
            String host = plugin.getConfigManager().getConfig().getString("database.host");
            int port = plugin.getConfigManager().getConfig().getInt("database.port");
            String database = plugin.getConfigManager().getConfig().getString("database.database");
            String username = plugin.getConfigManager().getConfig().getString("database.username");
            String password = plugin.getConfigManager().getConfig().getString("database.password");
            
            config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
            config.setUsername(username);
            config.setPassword(password);
        }
        
        config.setMaximumPoolSize(plugin.getConfigManager().getConfig().getInt("database.pool-size", 10));
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        dataSource = new HikariDataSource(config);
        createTables();
    }

    private void createTables() {
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "name VARCHAR(16), " +
                "primary_currency VARCHAR(3), " +
                "country VARCHAR(2), " +
                "last_seen_ip VARCHAR(45), " +
                "last_login BIGINT)"
            );

            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS wallet_balances (" +
                "uuid VARCHAR(36), " +
                "currency_iso VARCHAR(3), " +
                "amount DECIMAL(24,8), " +
                "PRIMARY KEY (uuid, currency_iso))"
            );

            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS bank_balances (" +
                "uuid VARCHAR(36), " +
                "currency_iso VARCHAR(3), " +
                "amount DECIMAL(24,8), " +
                "PRIMARY KEY (uuid, currency_iso))"
            );

            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS transactions (" +
                "tx_id INT AUTO_INCREMENT PRIMARY KEY, " +
                "uuid VARCHAR(36), " +
                "type VARCHAR(20), " +
                "currency_iso VARCHAR(3), " +
                "amount DECIMAL(24,8), " +
                "timestamp BIGINT)"
            );

            conn.createStatement().execute(
                "CREATE TABLE IF NOT EXISTS exchange_rates (" +
                "from_iso VARCHAR(3), " +
                "to_iso VARCHAR(3), " +
                "rate DECIMAL(24,8), " +
                "last_updated BIGINT, " +
                "PRIMARY KEY (from_iso, to_iso))"
            );
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables: " + e.getMessage());
        }
    }

    public PlayerData getPlayerData(UUID uuid) {
        if (cache.containsKey(uuid)) {
            return cache.get(uuid);
        }
        
        PlayerData data = loadPlayerData(uuid);
        cache.put(uuid, data);
        return data;
    }

    private PlayerData loadPlayerData(UUID uuid) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT * FROM users WHERE uuid = ?"
            );
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            
            PlayerData data;
            if (rs.next()) {
                data = new PlayerData(uuid);
                data.setName(rs.getString("name"));
                data.setPrimaryCurrency(rs.getString("primary_currency"));
                data.setCountry(rs.getString("country"));
                data.setLastSeenIp(rs.getString("last_seen_ip"));
                data.setLastLogin(rs.getLong("last_login"));
            } else {
                data = new PlayerData(uuid);
            }
            
            loadBalances(uuid, data);
            return data;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load player data: " + e.getMessage());
            return new PlayerData(uuid);
        }
    }

    private void loadBalances(UUID uuid, PlayerData data) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement walletStmt = conn.prepareStatement(
                "SELECT currency_iso, amount FROM wallet_balances WHERE uuid = ?"
            );
            walletStmt.setString(1, uuid.toString());
            ResultSet walletRs = walletStmt.executeQuery();
            
            while (walletRs.next()) {
                data.setWalletBalance(
                    walletRs.getString("currency_iso"),
                    walletRs.getBigDecimal("amount")
                );
            }
            
            PreparedStatement bankStmt = conn.prepareStatement(
                "SELECT currency_iso, amount FROM bank_balances WHERE uuid = ?"
            );
            bankStmt.setString(1, uuid.toString());
            ResultSet bankRs = bankStmt.executeQuery();
            
            while (bankRs.next()) {
                data.setBankBalance(
                    bankRs.getString("currency_iso"),
                    bankRs.getBigDecimal("amount")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load balances: " + e.getMessage());
        }
    }

    public void savePlayerData(UUID uuid) {
        PlayerData data = cache.get(uuid);
        if (data == null) return;
        
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "REPLACE INTO users (uuid, name, primary_currency, country, last_seen_ip, last_login) " +
                "VALUES (?, ?, ?, ?, ?, ?)"
            );
            stmt.setString(1, uuid.toString());
            stmt.setString(2, data.getName());
            stmt.setString(3, data.getPrimaryCurrency());
            stmt.setString(4, data.getCountry());
            stmt.setString(5, data.getLastSeenIp());
            stmt.setLong(6, data.getLastLogin());
            stmt.executeUpdate();
            
            saveBalances(uuid, data);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save player data: " + e.getMessage());
        }
    }

    private void saveBalances(UUID uuid, PlayerData data) {
        try (Connection conn = dataSource.getConnection()) {
            conn.createStatement().execute(
                "DELETE FROM wallet_balances WHERE uuid = '" + uuid.toString() + "'"
            );
            conn.createStatement().execute(
                "DELETE FROM bank_balances WHERE uuid = '" + uuid.toString() + "'"
            );
            
            PreparedStatement walletStmt = conn.prepareStatement(
                "INSERT INTO wallet_balances (uuid, currency_iso, amount) VALUES (?, ?, ?)"
            );
            for (Map.Entry<String, BigDecimal> entry : data.getWalletBalances().entrySet()) {
                walletStmt.setString(1, uuid.toString());
                walletStmt.setString(2, entry.getKey());
                walletStmt.setBigDecimal(3, entry.getValue());
                walletStmt.executeUpdate();
            }
            
            PreparedStatement bankStmt = conn.prepareStatement(
                "INSERT INTO bank_balances (uuid, currency_iso, amount) VALUES (?, ?, ?)"
            );
            for (Map.Entry<String, BigDecimal> entry : data.getBankBalances().entrySet()) {
                bankStmt.setString(1, uuid.toString());
                bankStmt.setString(2, entry.getKey());
                bankStmt.setBigDecimal(3, entry.getValue());
                bankStmt.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save balances: " + e.getMessage());
        }
    }

    public void logTransaction(UUID uuid, String type, String currency, BigDecimal amount) {
        try (Connection conn = dataSource.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO transactions (uuid, type, currency_iso, amount, timestamp) VALUES (?, ?, ?, ?, ?)"
            );
            stmt.setString(1, uuid.toString());
            stmt.setString(2, type);
            stmt.setString(3, currency);
            stmt.setBigDecimal(4, amount);
            stmt.setLong(5, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to log transaction: " + e.getMessage());
        }
    }

    public void saveAll() {
        for (UUID uuid : cache.keySet()) {
            savePlayerData(uuid);
        }
    }

    public void shutdown() {
        saveAll();
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}