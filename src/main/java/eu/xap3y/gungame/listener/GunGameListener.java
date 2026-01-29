package eu.xap3y.gungame.listener;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.service.LevelingService;
import eu.xap3y.gungame.util.ConfigDb;
import eu.xap3y.gungame.util.UpgradeUtil;
import eu.xap3y.xagui.adapter.ParseUtil;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

@AllArgsConstructor
public class GunGameListener implements Listener {

    private final LevelingService levelingService;

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if (!GunGame.getInstance().getArenaManager().isPlayerInArena(event.getPlayer().getUniqueId())) {
            return;
        }
        Location custom = GunGame.getInstance().getArenaManager().getCurrentArena().getSpawn();
        event.setRespawnLocation(custom);
        event.getPlayer().setGameMode(ConfigDb.GAMEMODE_SET);
    }

    @EventHandler
    public void onPlayerDrop(PlayerDropItemEvent event) {
        if (GunGame.getInstance().getArenaManager().isPlayerInArena(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!GunGame.getInstance().getArenaManager().isPlayerInArena(event.getEntity().getUniqueId())) {
            return;
        }

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Handle killer upgrade
        if (killer != null && killer != victim && killer.getGameMode() == ConfigDb.GAMEMODE_SET) {
            boolean leveled = levelingService.addKill(killer.getUniqueId());
            if (leveled) {
                applyLoadout(killer);
                killer.playSound(killer.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                killer.sendActionBar(ParseUtil.parseText("§aLevel Up! Now at §e" + levelingService.get(killer.getUniqueId()).getLevel()));
            }
        }

        // Handle victim downgrade
        if (victim.getGameMode() == ConfigDb.GAMEMODE_SET) {
            boolean downgrade = levelingService.get(victim.getUniqueId()).getLevel() > 0;
            levelingService.addDeath(victim.getUniqueId());
            Bukkit.getScheduler().runTaskLater(
                    GunGame.getInstance(),
                    () -> {
                        applyLoadout(victim);
                        if (downgrade) {
                            victim.sendActionBar(ParseUtil.parseText("&cYou died!"));
                        } else {
                            victim.sendActionBar(ParseUtil.parseText("&cLevel down > &e" + levelingService.get(victim.getUniqueId()).getLevel()));
                        }
                        victim.playSound(victim.getLocation(), Sound.ENTITY_SKELETON_DEATH, 1f, 0.7f);
                    },
                    1L
            );
        }

        event.getDrops().clear();
        event.setDroppedExp(0);

        Bukkit.getScheduler().runTask(
                GunGame.getInstance(),
                () -> event.getEntity().spigot().respawn()
        );
    }

    private void applyLoadout(Player player) {
        UpgradeUtil.process(player);

    }
}