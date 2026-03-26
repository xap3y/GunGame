package eu.xap3y.gungame.listener;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.service.PotionService;
import eu.xap3y.gungame.util.ConfigDb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PlayerConnectListener implements Listener {

    private final Set<UUID> kickedPlayers = new HashSet<>();

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        kickedPlayers.add(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        GunGame.getTexter().debugLog("Player " + event.getPlayer().getName() + " JOIN (" + event.getPlayer().getUniqueId() + ")");
        boolean autoJoin = GunGame.getInstance().getConfig().getBoolean("auto-join", true);
        if (!autoJoin) return;
        GunGame.getTexter().debugLog("Auto-joining player " + event.getPlayer().getName() + " to arena");
        event.getPlayer().setHealth(20);
        event.getPlayer().setFoodLevel(20);
        GunGame.getInstance().getArenaManager().joinPlayerToArena(event.getPlayer(), !event.getPlayer().hasPlayedBefore());
        GunGame.getBoardApi().addBoard(event.getPlayer());
        PotionService.getInstance().refresh(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

        // remove wand points
        ConfigDb.POS_CACHE.remove(event.getPlayer().getUniqueId());

        if (GunGame.getInstance().getArenaManager().isPlayerInArena(event.getPlayer().getUniqueId())) {
            GunGame.getInstance().getArenaManager().leavePlayerFromArena(event.getPlayer());

            if (!kickedPlayers.contains(event.getPlayer().getUniqueId())) {
                GunGame.getInstance().getLevelingService().reset(event.getPlayer().getUniqueId());
            } else {
                kickedPlayers.remove(event.getPlayer().getUniqueId());
            }
        } else kickedPlayers.remove(event.getPlayer().getUniqueId());

        // remove scoreboard
        GunGame.getBoardApi().removeBoard(event.getPlayer().getUniqueId());

        // Remove player from stats cache
        GunGame.getInstance().getDatabaseManager().getPlayerDao().getPlayerCache().remove(event.getPlayer().getUniqueId());

        ConfigDb.FALL_DAMAGE_CANCEL.remove(event.getPlayer().getUniqueId());
        ConfigDb.LAST_LAUNCHES.remove(event.getPlayer().getUniqueId());

        if (ConfigDb.FALL_DAMAGE_CANCEL_TASK.containsKey(event.getPlayer().getUniqueId())) {
            ConfigDb.FALL_DAMAGE_CANCEL_TASK.get(event.getPlayer().getUniqueId()).cancel();
            ConfigDb.FALL_DAMAGE_CANCEL_TASK.remove(event.getPlayer().getUniqueId());
        }

        ConfigDb.LAST_DEATHS_CALLS.remove(event.getPlayer().getUniqueId());
    }
}
