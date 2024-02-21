package me.manhumor.mreputation;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class ReputationExpansion extends PlaceholderExpansion {
    private final MReputation instance;
    private final FileConfiguration config;
    private final ConfigurationSection replacePositiveSection;
    private final ConfigurationSection replaceNegativeSection;
    public ReputationExpansion() {
        this.instance = MReputation.getInstance();
        this.config = instance.getConfig();
        this.replacePositiveSection = config.getConfigurationSection("replace.positive");
        this.replaceNegativeSection = config.getConfigurationSection("replace.negative");
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
        return "1.2";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return null;

        if (params.equals("get")) {
            int reputation = instance.getReputation(player.getName());
            String result;

            if (reputation > replacePositiveSection.getInt("maximum-positive") ||
                    reputation < replaceNegativeSection.getInt("maximum-negative")) {

                result = reputation > 0 ? replacePositiveSection.getString("positive-replace") : replaceNegativeSection.getString("negative-replace");
            } else {
                if (reputation > 0) {
                    result = config.getString("positive-reputation");
                } else if (reputation < 0) {
                    result = config.getString("negative-reputation");
                } else {
                    result = config.getString("reputation-zero");
                }
            }
            return ColorParser.parseString(result.replaceAll("\\{reputation}", Integer.toString(reputation)));
        }
        return null;
    }
}
