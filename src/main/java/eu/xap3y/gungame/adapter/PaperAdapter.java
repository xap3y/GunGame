package eu.xap3y.gungame.adapter;

import eu.xap3y.xagui.adapter.ParseUtil;
import org.bukkit.Bukkit;

// Avoid Class not found for kyori adventure API on Spigot/Bukkit platforms
public class PaperAdapter {

    public static void broadcastMsg(String text) {
        Bukkit.broadcast(ParseUtil.parseText(text));
    }
}
