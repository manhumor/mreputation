package me.manhumor.mreputation;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;
import java.util.List;

public class InventoryManager implements Listener {
    private MReputation instance;
    private FileConfiguration config;
    private static ConfigurationSection menuSection;

    private static int size;
    private static String title;

    public static HashMap<String, String> messages = new HashMap<>();

    public InventoryManager() {
        this.instance = MReputation.getInstance();
        this.config = instance.getConfig();
        menuSection = config.getConfigurationSection("reputation-menu");

        size = menuSection.getInt("size");
        title = ColorParser.parseString(menuSection.getString("title"));
    }
    public static Inventory createInventory(Player player) {
        String playerName = player.getName();
        Inventory inventory = Bukkit.createInventory(player, size, title);
        ConfigurationSection items = menuSection.getConfigurationSection("items");
        for (String item : items.getKeys(false)) {
            ConfigurationSection itemSection = items.getConfigurationSection(item);
            String material = itemSection.getString("material");
            int slot = Integer.parseInt(item);

            ItemStack itemStack;
            if (material.equalsIgnoreCase("{playerHead}")) {
                itemStack = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
                itemStack.setItemMeta(skullMeta);
            } else {
                itemStack = new ItemStack(Material.valueOf(material));
            }

            ItemMeta meta = itemStack.getItemMeta();
            meta.setDisplayName(ColorParser.parseString(itemSection.getString("name")
                    .replaceAll("\\{playerName}", playerName)));
            meta.setLore(ColorParser.parseList(itemSection.getStringList("lore")));
            meta.setCustomModelData(slot);
            itemStack.setItemMeta(meta);
            inventory.setItem(slot, itemStack);
        }
        return inventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equalsIgnoreCase(title)) return;
        Player player = (Player) event.getView().getPlayer();
        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasCustomModelData()) return;
        int customModelData = meta.getCustomModelData();

        ConfigurationSection itemSection = menuSection.getConfigurationSection("items." + customModelData);

        if (itemSection.contains("commands")) {
            List<String> commands = itemSection.getStringList("commands");
            Player inventoryOwner = (Player) event.getInventory().getHolder();
            String playerName = player.getName();
            for (String line : commands) {
                Bukkit.dispatchCommand(event.getView().getPlayer(), ColorParser.parseString(line)
                        .replaceAll("\\{playerName}", inventoryOwner.getName())
                        .replaceAll("\\{message}", messages.get(playerName) == null ? "" : messages.get(playerName)));
            }
        }
        player.closeInventory();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equalsIgnoreCase(title)) return;

        String playerName = event.getView().getPlayer().getName();
        if (messages.containsKey(playerName)) messages.remove(playerName);
    }
}
