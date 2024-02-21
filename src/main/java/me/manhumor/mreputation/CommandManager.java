package me.manhumor.mreputation;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final MReputation instance;

    private final FileConfiguration config;
    private final Map<String, List<String>> dispatcherMap;
    private final Map<String, List<String>> receiverMap;

    private final String noPermission;
    private final String noArguments;
    private final String noPlayer;
    private final String wrongArgument;
    private final String cantBetYourself;
    private final String hasCooldown;

    private final List<String> resetYourself;

    public CommandManager() {
        this.instance = MReputation.getInstance();
        this.config = instance.getConfig();

        ConfigurationSection dispatcher = config.getConfigurationSection("dispatcher");
        this.dispatcherMap = Map.of(
                "+", ColorParser.parseList(dispatcher .getStringList("dispatched-positive")),
                "-", ColorParser.parseList(dispatcher .getStringList("dispatched-negative")),
                "reset", ColorParser.parseList(dispatcher .getStringList("dispatched-reset"))
        );
        ConfigurationSection receiver = config.getConfigurationSection("dispatcher");
        this.receiverMap = Map.of(
                "+", ColorParser.parseList(receiver.getStringList("received-positive")),
                "-", ColorParser.parseList(receiver.getStringList("received-negative")),
                "reset", ColorParser.parseList(receiver.getStringList("received-reset"))
        );
        ConfigurationSection errors = config.getConfigurationSection("errors");
        this.noPermission = ColorParser.parseString(errors.getString("no-permission"));
        this.noArguments = ColorParser.parseString(errors.getString("no-arguments"));
        this.noPlayer = ColorParser.parseString(errors.getString("no-player"));
        this.wrongArgument = ColorParser.parseString(errors.getString("wrong-argument"));
        this.cantBetYourself = ColorParser.parseString(errors.getString("cant-bet-yourself"));

        this.hasCooldown = ColorParser.parseString(errors.getString("has-cooldown"));

        this.resetYourself = ColorParser.parseList(config.getStringList("yourself.reset-yourself"));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        String playerName = player.getName();

        if (!player.hasPermission("mreputation.use")) {
            player.sendMessage(noPermission.replaceAll("\\{permission}", "mreputation.use"));
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(noArguments);
            return true;
        }

        if (!Arrays.asList("+", "-", "gui", "reset").contains(args[1].toLowerCase())) {
            player.sendMessage(wrongArgument);
            return true;
        }

        if (!CooldownManager.checkCooldown(playerName)) {
            player.sendMessage(hasCooldown.replaceAll("\\{cooldown}", CooldownManager.getCooldown(playerName)));
            return true;
        }

        Player playerMentioned = Bukkit.getPlayer(args[0]);
        if (playerMentioned == null || !playerMentioned.isOnline()) {
            player.sendMessage(noPlayer);
            return true;
        }

        String playerMentionedName = playerMentioned.getName();

        if (playerName.equalsIgnoreCase(playerMentionedName) && !args[1].toLowerCase().equalsIgnoreCase("reset")) {
            player.sendMessage(cantBetYourself);
            return true;
        }

        int reputation = instance.getReputation(playerMentionedName);

        String message = "";

        if (args.length > 2) message = String.join(" ", Arrays.asList(args).subList(2, args.length));

        switch (args[1]) {
            case "+" -> ++reputation;
            case "-" -> --reputation;
            case "gui" -> {
                if (!player.hasPermission("mreputation.gui")) {
                    player.sendMessage(noPermission.replaceAll("\\{permission}", "mreputation.gui"));
                    return true;
                }

                InventoryManager.messages.remove(playerName);
                player.openInventory(InventoryManager.createInventory(playerMentioned));
                if (!message.trim().isEmpty()) InventoryManager.messages.put(playerName, message);
                return true;
            }
            case "reset" -> {
                if (!player.hasPermission("mreputation.reset")) {
                    player.sendMessage(noPermission.replaceAll("\\{permission}", "mreputation.reset"));
                    return true;
                }
                if (playerName.equalsIgnoreCase(playerMentionedName)) {
                    sendList(playerMentioned, resetYourself, playerMentionedName, reputation, message);
                    instance.setReputation(playerMentionedName, 0);

                    CooldownManager.addCooldown(playerName);

                    return true;
                }
                reputation = 0;
            }
        }

        sendList(player, dispatcherMap.get(args[1]), playerMentionedName, reputation, message);
        sendList(playerMentioned, receiverMap.get(args[1]), playerMentionedName, reputation, message);
        instance.setReputation(playerMentionedName, reputation);

        CooldownManager.addCooldown(playerName);

        return true;
    }

    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
        }
        else if (args.length == 2) {
            return Arrays.asList("+", "-", "gui", "reset");
        }
        else if (args.length == 3) {
            return Collections.singletonList("(сообщение)");
        }

        return null;
    }

    public void sendList(Player player, List<String> list, String mentionedName, int reputation, String message) {
        for (String line : list) {
            player.sendMessage(line
                    .replaceAll("\\{playerName}", mentionedName)
                    .replaceAll("\\{playerReputation}", Integer.toString(reputation))
                    .replaceAll("\\{message}", message));
        }
    }
}
