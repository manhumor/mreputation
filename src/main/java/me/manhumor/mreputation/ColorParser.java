package me.manhumor.mreputation;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.md_5.bungee.api.ChatColor.COLOR_CHAR;

public class ColorParser {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F\\d]{6})");

    public static String parseString(String message) {
        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer,
                    COLOR_CHAR + "x" + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1) + COLOR_CHAR
                            + group.charAt(2) + COLOR_CHAR + group.charAt(3) + COLOR_CHAR + group.charAt(4)
                            + COLOR_CHAR + group.charAt(5));
        }
        message = matcher.appendTail(buffer).toString();
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static List<String> parseList(List<String> list) {
        List<String> parsedList = new ArrayList<>();
        for (String line : list) parsedList.add(parseString(line));
        return parsedList;
    }
}
