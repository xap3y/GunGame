package eu.xap3y.gungame.listener;

import com.cryptomorin.xseries.XMaterial;
import eu.xap3y.gungame.GunGame;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (!GunGame.getInstance().getArenaManager().isPlayerInArena(e.getPlayer().getUniqueId()) || e.getFrom().getBlockX() == e.getTo().getBlockX() &&
                e.getFrom().getBlockY() == e.getTo().getBlockY() &&
                e.getFrom().getBlockZ() == e.getTo().getBlockZ()) {
            return;
        }

        Block toBlock = e.getTo().getBlock();

        if (toBlock.getType() == XMaterial.WATER.get()) {

            Block fromBlock = e.getFrom().getBlock();
            if (fromBlock.getType() != XMaterial.WATER.get()) {
                e.getPlayer().setHealth(0);
            }
        }
    }
}
