package eu.xap3y.gungame.listener;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.util.ConfigDb;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;
import java.util.UUID;

public class PlayerMoveListener implements Listener {

    @EventHandler
    public void onMove(PlayerMoveEvent e) {

        double fromX = Math.round(e.getFrom().getX() * 10.0) / 10.0;
        double fromY = Math.round(e.getFrom().getY() * 10.0) / 10.0;
        double fromZ = Math.round(e.getFrom().getZ() * 10.0) / 10.0;

        double toX = Math.round(e.getTo().getZ() * 10.0) / 10.0;
        double toY = Math.round(e.getTo().getZ() * 10.0) / 10.0;
        double toZ = Math.round(e.getTo().getZ() * 10.0) / 10.0;

        if (fromX == toX && fromY == toY && fromZ == toZ) {
            return;
        }

        if (
                !GunGame.getInstance().getArenaManager().isPlayerInArena(e.getPlayer().getUniqueId()) ||
                !e.getPlayer().getGameMode().equals(ConfigDb.GAMEMODE_SET)
        ) {
            return;
        }

        Block toBlock = e.getTo().getBlock();

        if (toBlock.getType() == XMaterial.WATER.get() || toBlock.getType() == Material.getMaterial("STATIONARY_WATER")) {
            Block fromBlock = e.getFrom().getBlock();
            if (fromBlock.getType() != XMaterial.WATER.get()) {
                e.getPlayer().setHealth(0);
            }
        } else if (toBlock.getType() == XMaterial.LIGHT_WEIGHTED_PRESSURE_PLATE.get() && !ConfigDb.LAST_LAUNCHES.contains(e.getPlayer().getUniqueId())) {
            // launch player where he is looking with random velocity
            double randomPower = 0.8 + Math.random() * 0.5;
            double randomPowerY = 1.2 + Math.random() * 0.5;
            e.getPlayer().setVelocity(e.getPlayer().getLocation().getDirection().multiply(randomPower).setY(randomPowerY));
            XSound.BLOCK_PISTON_EXTEND.play(e.getPlayer(), .6f, .8f);
            ConfigDb.FALL_DAMAGE_CANCEL.add(e.getPlayer().getUniqueId());
            ConfigDb.LAST_LAUNCHES.add(e.getPlayer().getUniqueId());
            GunGame.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(GunGame.getInstance(), () -> {
                ConfigDb.LAST_LAUNCHES.remove(e.getPlayer().getUniqueId());
            }, 20L);

            GunGame.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(GunGame.getInstance(), () -> {
                ConfigDb.LAST_LAUNCHES.remove(e.getPlayer().getUniqueId());
            }, 25L * 3L);
        }
    }
}
