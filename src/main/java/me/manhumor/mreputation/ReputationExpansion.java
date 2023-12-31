package me.manhumor.mreputation;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReputationExpansion extends PlaceholderExpansion {
    private MReputation instance;
    private FileConfiguration config;
    public ReputationExpansion() {
        this.instance = MReputation.getInstance();
        this.config = instance.getConfig();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "mreputation";
    }

    @Override
    public @NotNull String getAuthor() {
        return "man_humor";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, String params) {
        Player player = Bukkit.getPlayer(offlinePlayer.getName());
        if (player==null || !player.isOnline()) return null;

        if (params.equalsIgnoreCase("get")) {
            int reputation = instance.getReputation(player.getName());
            String result = "";
            if (reputation > 0) {
                result = config.getString("positive-reputation");
            } else if (reputation < 0) {
                result = config.getString("negative-reputation");
            } else {
                result = config.getString("reputation-zero");
            }
            return ColorParser.parseString(result
                    .replaceAll("\\{reputation}", Integer.toString(reputation)));
        }
        return null;
    }
}
