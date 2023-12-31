package me.manhumor.mreputation;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final MReputation instance;
    private final Map<String, List<String>> dispatcherMap;
    private final Map<String, List<String>> receiverMap;

    private final String noPermission;
    private final String noArguments;
    private final String noPlayer;
    private final String cantBetYourself;
    private final String wrongArgument;

    private final List<String> resetYourself;

    public CommandManager() {
        this.instance = MReputation.getInstance();
        FileConfiguration config = instance.getConfig();

        this.dispatcherMap = new HashMap<>();
        this.receiverMap = new HashMap<>();

        this.dispatcherMap.put("+", config.getStringList("dispatched-positive"));
        this.dispatcherMap.put("-", config.getStringList("dispatched-negative"));
        this.dispatcherMap.put("reset", config.getStringList("dispatched-reset"));

        this.receiverMap.put("+", config.getStringList("received-positive"));
        this.receiverMap.put("-", config.getStringList("received-negative"));
        this.receiverMap.put("reset", config.getStringList("received-reset"));

        this.noPermission = ColorParser.parseString(config.getString("no-permission")
                .replaceAll("\\{permission}", "mreputation.use"));
        this.noArguments = ColorParser.parseString(config.getString("no-arguments"));
        this.noPlayer = ColorParser.parseString(config.getString("no-player"));
        this.cantBetYourself = ColorParser.parseString(config.getString("cant-bet-yourself"));
        this.wrongArgument = ColorParser.parseString(config.getString("wrong-argument"));

        this.resetYourself = config.getStringList("reset-yourself");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        String playerName = player.getName();

        if (!player.hasPermission("mreputation.use")) {
            player.sendMessage(noPermission);
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(noArguments);
            return true;
        }

        Player playerMentioned = Bukkit.getPlayer(args[0]);
        if (playerMentioned == null || !playerMentioned.isOnline()) {
            player.sendMessage(noPlayer);
            return true;
        }

        String playerMentionedName = playerMentioned.getName();

        if (playerName.equalsIgnoreCase(playerMentionedName) && !args[1].equalsIgnoreCase("reset")) {
            player.sendMessage(cantBetYourself);
            return true;
        }

        int reputation = instance.getReputation(playerMentionedName);

        if (dispatcherMap.containsKey(args[1])) {
            switch (args[1]) {
                case "+":
                    reputation++;
                    break;
                case "-":
                    reputation--;
                    break;
                default:
                    if (!player.hasPermission("mreputation.reset")) {
                        player.sendMessage(noPermission);
                        return true;
                    }
                    reputation = 0;
                    break;
            }
            if (args[1].equalsIgnoreCase("reset") && playerName.equalsIgnoreCase(playerMentionedName)) {
                sendList(player, resetYourself, playerMentionedName, reputation);
            } else {
                sendList(player, dispatcherMap.get(args[1]), playerMentionedName, reputation);
                sendList(playerMentioned, receiverMap.get(args[1]), playerName, reputation);
            }
            instance.setReputation(playerMentionedName, reputation);
        } else {
            player.sendMessage(wrongArgument);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> complete = new ArrayList<>();
        if (args.length==1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                complete.add(player.getName());
            }
        }
        if (args.length==2) complete = Arrays.asList("+", "-", "reset");
        return complete;
    }

    public void sendList(Player player, List<String> list, String mentionedName, int reputation) {
        for (String line : list) {
            player.sendMessage(ColorParser.parseString(line
                    .replaceAll("\\{playerName}", mentionedName)
                    .replaceAll("\\{playerReputation}", Integer.toString(reputation))));
        }
    }
}
