package me.manhumor.mreputation;

import me.manhumor.mreputation.database.Database;
import me.manhumor.mreputation.database.impl.SQLiteDatabase;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.SQLException;

public final class MReputation extends JavaPlugin {
    private static MReputation instance;
    public static MReputation getInstance() {
        return instance;
    }
    private Database database;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        instance=this;

        try {
            database = new SQLiteDatabase(new File(getDataFolder(), "database.db"));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database.", e);
        }

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ReputationExpansion().register();
        }

        InventoryManager.initMap();

        CommandManager commandManager = new CommandManager();
        getCommand("mreputation").setExecutor(commandManager);
        getCommand("mreputation").setTabCompleter(commandManager);

        Bukkit.getPluginManager().registerEvents(new InventoryManager(), this);

        getLogger().info("§c. . . . . . . . . . . .");
        getLogger().info("§c| §fPlugin §cM§fReputation");
        getLogger().info("§c| §f- §cSuccessful §floaded");
        getLogger().info("§c| §f- §cI wish you §fluck!!!");
        getLogger().info("§c˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        database.close();
        getLogger().info("§c. . . . . . . . . . . .");
        getLogger().info("§c| §fPlugin §cM§fReputation");
        getLogger().info("§c| §f- §cSuccessful §funloaded");
        getLogger().info("§c| §f- §cI wish you §fluck!!!");
        getLogger().info("§c˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙ ˙");
    }

    public int getReputation(String playerName) {
        return database.getReputation(playerName);
    }
    public void setReputation(String playerName, int reputation) {
        database.setReputation(playerName, reputation);
    }
}
