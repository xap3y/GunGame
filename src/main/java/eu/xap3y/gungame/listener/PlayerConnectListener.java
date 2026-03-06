package eu.xap3y.gungame.listener;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.util.ConfigDb;
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
        GunGame.getBoardApi().addBoard(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        // remove wand points
        ConfigDb.POS_CACHE.remove(event.getPlayer().getUniqueId());

        if (GunGame.getInstance().getArenaManager().isPlayerInArena(event.getPlayer().getUniqueId())) {
            GunGame.getInstance().getArenaManager().leavePlayerFromArena(event.getPlayer());

            if (event.getReason() == PlayerQuitEvent.QuitReason.DISCONNECTED) {
                GunGame.getInstance().getLevelingService().reset(event.getPlayer().getUniqueId());
            }
        }

        // remove scoreboard
        GunGame.getBoardApi().removeBoard(event.getPlayer().getUniqueId());

        // Remove player from stats cache
        GunGame.getInstance().getDatabaseManager().getPlayerDao().getPlayerCache().remove(event.getPlayer().getUniqueId());
    }
}
