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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
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
    public void onInventoryClick(InventoryClickEvent event) {
        if (GunGame.getInstance().getArenaManager().isPlayerInArena(event.getWhoClicked().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {

        if (
                !(event.getDamageSource().getCausingEntity() instanceof Player attacker) ||
                !(event.getEntity() instanceof Player victim)
        ) {
            return;
        }

        if (!GunGame.getInstance().getArenaManager().isPlayerInArena(victim.getUniqueId())) {
            return;
        }

        GunGame.getInstance().getArenaManager().addLastDamager(attacker, victim.getUniqueId());
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!GunGame.getInstance().getArenaManager().isPlayerInArena(event.getEntity().getUniqueId())) {
            return;
        }

        Player victim = event.getEntity();
        Player killer;

        Player lastDamager = GunGame.getInstance().getArenaManager().getPlayerByVictim(victim.getUniqueId());
        if (lastDamager != null) {
            GunGame.getTexter().console("lastDamager: " + lastDamager.getName() + " | Victim: " + victim.getName());
            killer = lastDamager;
        } else if (victim.getKiller() != null) {
            killer = victim.getKiller();
            GunGame.getTexter().console("killer: " + killer.getName() + " | Victim: " + victim.getName());
        } else {
            killer = null;
            GunGame.getTexter().console("No killer found for victim: " + victim.getName());
        }

        // Handle killer upgrade
        if (killer != null && killer != victim && killer.getGameMode() == ConfigDb.GAMEMODE_SET) {
            boolean leveled = levelingService.addKill(killer.getUniqueId());
            if (leveled) {
                applyLoadout(killer);
                XSound.ENTITY_PLAYER_LEVELUP.play(killer, .7f, 1.5f);
                ActionBar.sendActionbar(killer, Texter.colored("&aLevel Up! Now at &e" + levelingService.get(killer.getUniqueId()).getLevel()));
            }
        }

        // Handle victim downgrade
        if (victim.getGameMode() == ConfigDb.GAMEMODE_SET) {
            boolean downgrade = levelingService.addDeath(victim.getUniqueId());
            Bukkit.getScheduler().runTaskLater(
                    GunGame.getInstance(),
                    () -> {
                        applyLoadout(victim);
                        if (!downgrade) {
                            ActionBar.sendActionbar(victim, Texter.colored("&cYou died!"));
                        } else {
                            ActionBar.sendActionbar(victim, Texter.colored("&cLevel down > &e" + levelingService.get(victim.getUniqueId()).getLevel()));
                        }
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