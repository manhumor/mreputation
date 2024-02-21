package me.manhumor.mreputation;

import me.manhumor.mreputation.database.Database;
import me.manhumor.mreputation.database.impl.SQLiteDatabase;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
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

        Bukkit.getPluginManager().registerEvents(new InventoryManager(), this);

        if (getConfig().getBoolean("player-press", true)) {
            Bukkit.getPluginManager().registerEvents(new Listener() {
                @EventHandler
                public void onPlayerInteract(PlayerInteractEntityEvent event) {
                    if (!(event.getRightClicked() instanceof Player)) return;
                    Player player = event.getPlayer();

                    if (!player.isSneaking() || !player.hasPermission("mreputation.gui")) return;
                    Player playerPressed = (Player) event.getRightClicked();

                    InventoryManager.messages.remove(playerPressed);
                    player.openInventory(InventoryManager.createInventory(playerPressed));
                }
            }, this);
        }

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
