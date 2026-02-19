package eu.xap3y.gungame.listener;

import eu.xap3y.gungame.GunGame;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        boolean autoJoin = GunGame.getInstance().getConfig().getBoolean("auto-join");
        if (!autoJoin) return;
        GunGame.getInstance().getArenaManager().joinPlayerToArena(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        GunGame.getInstance().getArenaManager().leavePlayerFromArena(event.getPlayer());
    }
}
