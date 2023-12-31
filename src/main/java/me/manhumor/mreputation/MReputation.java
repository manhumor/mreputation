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

        CommandManager commandManager = new CommandManager();
        getCommand("mreputation").setExecutor(commandManager);
        getCommand("mreputation").setTabCompleter(commandManager);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        database.close();
    }

    public int getReputation(String playerName) {
        return database.getReputation(playerName);
    }
    public void setReputation(String playerName, int reputation) {
        database.setReputation(playerName, reputation);
    }
}
