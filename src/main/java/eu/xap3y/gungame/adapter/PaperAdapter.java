package eu.xap3y.gungame.adapter;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.util.ConfigDb;
import eu.xap3y.xagui.adapter.ParseUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

// Avoid Class not found for kyori adventure API on Spigot/Bukkit platforms
public class PaperAdapter {

    public static void broadcastMsg(String text) {
        Bukkit.broadcast(ParseUtil.parseText(text));
    }

    public static void sendPosButtons(Player p0) {

        Component setSafeArenaButton = ParseUtil
                .parseText("&a[&2Set Safe Arena&a]")
                .hoverEvent(HoverEvent.showText(ParseUtil.parseText("&7Click to set safe arena")))
                .clickEvent(ClickEvent.suggestCommand("/" + ConfigDb.MAIN_COMMAND + " setup set-safezone "));

        Component defineArenaDimension = ParseUtil.parseText("&a[&2Set Arena Dimension&a]")
                .hoverEvent(HoverEvent.showText(ParseUtil.parseText("&7Click to set arena dimension")))
                .clickEvent(ClickEvent.suggestCommand("/" + ConfigDb.MAIN_COMMAND + " setup set-dimension "));

        Component finalComponent = ParseUtil.parseText(GunGame.getTexter().getPrefix())
                .append(ParseUtil.parseText("  "))
                .append(setSafeArenaButton)
                .append(ParseUtil.parseText("   "))
                .append(defineArenaDimension);

        p0.sendMessage(finalComponent);
    }
}
