package eu.xap3y.gungame.listener;

import com.cryptomorin.xseries.XSound;
import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.service.LevelingService;
import eu.xap3y.gungame.service.Texter;
import eu.xap3y.gungame.util.ActionBar;
import eu.xap3y.gungame.util.ConfigDb;
import eu.xap3y.gungame.util.UpgradeUtil;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.List;
import java.util.UUID;

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
    public void onInventoryClick(InventoryClickEvent event) {
        if (GunGame.getInstance().getArenaManager().isPlayerInArena(event.getWhoClicked().getUniqueId())) {

            if (!event.getWhoClicked().getGameMode().equals(ConfigDb.GAMEMODE_SET) || event.getWhoClicked().isOp()) return;

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (
                !(event.getEntity() instanceof Player victim) ||
                !GunGame.getInstance().getArenaManager().isPlayerInArena(victim.getUniqueId())
        ) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && ConfigDb.FALL_DAMAGE_CANCEL.contains(victim.getUniqueId())) {
            event.setCancelled(true);
            ConfigDb.FALL_DAMAGE_CANCEL.remove(victim.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerDamageByPlayer(EntityDamageByEntityEvent event) {

        if (
                !(event.getDamager() instanceof Player attacker) ||
                !(event.getEntity() instanceof Player victim)
        ) {
            return;
        }

        if (!GunGame.getInstance().getArenaManager().isPlayerInArena(victim.getUniqueId())) {
            return;
        } else if (GunGame.getInstance().getArenaManager().getCurrentArena().isInSafeZone(victim.getLocation())) {
            event.setCancelled(true);
            return;
        }

        GunGame.getInstance().getArenaManager().addLastDamager(attacker, victim.getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!GunGame.getInstance().getArenaManager().isPlayerInArena(event.getEntity().getUniqueId())) {
            return;
        }

        event.setDeathMessage(null);

        Player victim = event.getEntity();
        Player killer;

        Player lastDamager = GunGame.getInstance().getArenaManager().getPlayerByVictim(victim.getUniqueId());
        if (lastDamager != null) {
            killer = lastDamager;
        } else if (victim.getKiller() != null) {
            killer = victim.getKiller();
        } else {
            killer = null;
        }

        boolean isWithKiller = killer != null && killer != victim && killer.getGameMode() == ConfigDb.GAMEMODE_SET;

        // Handle killer upgrade
        if (isWithKiller) {
            boolean leveled = levelingService.addKill(killer.getUniqueId());

            String msg = GunGame.getInstance().getLangManager().get("actionbar.kill");
            msg = msg
                    .replace("{player}", victim.getName())
                    .replace("{stage}", levelingService.get(killer.getUniqueId()).getLevel() + "");

            if (leveled) {
                applyLoadout(killer);
                XSound.ENTITY_PLAYER_LEVELUP.play(killer, .7f, 1.5f);
                ActionBar.sendActionbar(killer, Texter.colored(msg));
            }
        }

        // Handle victim downgrade
        if (victim.getGameMode() == ConfigDb.GAMEMODE_SET) {
            Bukkit.getScheduler().runTaskLater(
                    GunGame.getInstance(),
                    () -> {
                        applyLoadout(victim);
                        boolean downgraded = levelingService.addDeath(victim.getUniqueId());
                        String msg = (isWithKiller) ? GunGame.getInstance().getLangManager().get("actionbar.death-by-player") : GunGame.getInstance().getLangManager().get("actionbar.death");
                        msg = msg
                                .replace("{killer}", (isWithKiller) ? killer.getName() : "Unknown")
                                .replace("{stage}", levelingService.get(victim.getUniqueId()).getLevel() + "");
                        ActionBar.sendActionbar(victim, Texter.colored(msg));
                        XSound.ENTITY_SKELETON_DEATH.play(victim, 1f, .7f);
                    },
                    1L
            );
        }

        event.getDrops().clear();
        event.setDroppedExp(0);

        GunGame.getInstance().getArenaManager().removeLastDamagerByVictim(victim.getUniqueId());

        Bukkit.getScheduler().runTask(
                GunGame.getInstance(),
                () -> event.getEntity().spigot().respawn()
        );
    }

    private void applyLoadout(Player player) {
        UpgradeUtil.process(player);
    }
}