package eu.xap3y.gungame.listener;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.util.ConfigDb;
import eu.xap3y.gungame.util.UpgradeUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerJoinListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        GunGame.getInstance().getArenaManager().addPlayer(event.getPlayer());
        event.getPlayer().teleport(GunGame.getInstance().getArenaManager().getCurrentArena().getSpawn());
        //event.getPlayer().setRespawnLocation(GunGame.getInstance().getArenaManager().getCurrentArena().getSpawn());

        event.getPlayer().getInventory().clear();

        UpgradeUtil.process(
                event.getPlayer()
        );

        Bukkit.getScheduler().runTaskLater(
                GunGame.getInstance(),
                () -> event.getPlayer().setGameMode(ConfigDb.GAMEMODE_SET),
                3L
        );
    }
}
