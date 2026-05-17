package eu.xap3y.gungame.listener;

import com.cryptomorin.xseries.XSound;
import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.enums.KillEffectType;
import eu.xap3y.gungame.service.LevelingService;
import eu.xap3y.gungame.service.PotionService;
import eu.xap3y.gungame.service.Texter;
import eu.xap3y.gungame.service.UpgradeService;
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

@AllArgsConstructor
public class GunGameListener implements Listener {

    private final LevelingService levelingService;

    /*@EventHandler
    public void onRespawn(PlayerPostRespawnEvent event) {
        if (!GunGame.getInstance().getArenaManager().isPlayerInArena(event.getPlayer().getUniqueId())) {
            return;
        }
        event.getPlayer().setGameMode(ConfigDb.GAMEMODE_SET);
        PotionService.getInstance().refresh(event.getPlayer());
    }*/

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        GunGame.getTexter().logPos();
        if (!GunGame.getInstance().getArenaManager().isPlayerInArena(event.getPlayer().getUniqueId())) {
            return;
        }
        Location custom = GunGame.getInstance().getArenaManager().getCurrentArena().getSpawn();
        event.setRespawnLocation(custom);

        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(GunGame.getInstance(), () -> {
            GunGame.getTexter().debugLog("LAST_DEATHS_CALLS.remove(" + player.getUniqueId() + ")");
            ConfigDb.LAST_DEATHS_CALLS.remove(player.getUniqueId());
            if (player.isOnline()) {
                player.setGameMode(ConfigDb.GAMEMODE_SET);

                // Re-apply potion effects from your service
                PotionService.getInstance().refresh(player);

                // Optional: Ensure they are full health/hunger
                player.setHealth(player.getMaxHealth());
                player.setFoodLevel(20);
                int lvl = levelingService.get(player.getUniqueId()).getLevel();
                player.setLevel(lvl);
                player.setExp(0f);
            }
        }, 1L);

        if (ConfigDb.STREAM_DEBUG_CHAT)
            GunGame.getTexter().response(player, "Stage: " + levelingService.get(player.getUniqueId()).getLevel());
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
        GunGame.getTexter().logPos();
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();

        if (!GunGame.getInstance().getArenaManager().isPlayerInArena(victim.getUniqueId())) {
            return;
        }

        if (event.getCause() == EntityDamageEvent.DamageCause.FALL && ConfigDb.FALL_DAMAGE_CANCEL.contains(victim.getUniqueId())) {
            event.setCancelled(true);
            ConfigDb.FALL_DAMAGE_CANCEL.remove(victim.getUniqueId());
            if (ConfigDb.FALL_DAMAGE_CANCEL_TASK.containsKey(victim.getUniqueId())) {
                GunGame.getTexter().debugLog("&cConfigDb.FALL_DAMAGE_CANCEL.remove&cancel(" + victim.getName() + ") &e#EntityDamageEvent");
                ConfigDb.FALL_DAMAGE_CANCEL_TASK.get(victim.getUniqueId()).cancel();
                ConfigDb.FALL_DAMAGE_CANCEL_TASK.remove(victim.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onPlayerDamageByPlayer(EntityDamageByEntityEvent event) {
        GunGame.getTexter().logPos();

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

        event.setDeathMessage(null);
        if (ConfigDb.LAST_DEATHS_CALLS.contains(event.getEntity().getUniqueId())) {
            GunGame.getTexter().debugLog("Skipping death processing for " + event.getEntity().getName() + " due to recent death call.");
            return;
        } else {
            ConfigDb.LAST_DEATHS_CALLS.add(event.getEntity().getUniqueId());
            GunGame.getTexter().debugLog("Added " + event.getEntity().getName() + " to LAST_DEATHS_CALLS.");
        }

        if (!GunGame.getInstance().getArenaManager().isPlayerInArena(event.getEntity().getUniqueId())) {
            return;
        }


        GunGame.getTexter().console("Player &e" + event.getEntity().getName() + "&f died, processing death...");

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

        //GunGame.getTexter().broadcast("&e" + victim.getName() + " &fwas killed by " + ((isWithKiller) ? killer.getName() : "&4N/A&f") + " | isWithKiller: " + isWithKiller);

        boolean deathMessage = GunGame.getInstance().getConfig().getBoolean("death-message", true);
        if (deathMessage) {
            String path = (isWithKiller) ? "death-by-player-message" : "death-message";
            String msg = GunGame.getInstance().getLangManager().get(path, "&c{player} died!").replace("{player}", victim.getName()).replace("{killer}", (isWithKiller) ? killer.getName() : "&cN/A");
            event.setDeathMessage(Texter.colored(msg));
        } else {
            event.setDeathMessage(null);
        }

        if (ConfigDb.FALL_DAMAGE_CANCEL_TASK.containsKey(victim.getUniqueId())) {
            GunGame.getTexter().debugLog("&cConfigDb.FALL_DAMAGE_CANCEL.remove&cancel(" + victim.getName() + ") &e#PlayerDeathEvent");
            ConfigDb.FALL_DAMAGE_CANCEL_TASK.get(victim.getUniqueId()).cancel();
            ConfigDb.FALL_DAMAGE_CANCEL_TASK.remove(victim.getUniqueId());
        }

        GunGame.getTexter().debugLog("onDeath for " + victim.getName() + " | isWithKilled = " + isWithKiller + " | Killer: " + ((killer != null) ? killer.getName() : "N/A"));

        // Handle killer upgrade
        if (isWithKiller) {

            ConfigDb.KILL_EFFECT_MAP.get(KillEffectType.BLOOD).playEffect(victim.getLocation());

            boolean doubleUpgrade = UpgradeService.getInstance().processDoubleUpgrade(killer);
            int lvl = (doubleUpgrade) ? 2 : 1;
            boolean leveled = levelingService.addKill(killer.getUniqueId(), lvl);


            String msg = GunGame.getInstance().getLangManager().get("actionbar.kill" + ((doubleUpgrade) ? "-double" : ""));
            msg = msg
                    .replace("{player}", victim.getName())
                    .replace("{stage}", levelingService.get(killer.getUniqueId()).getLevel() + "");

            if (leveled) {
                killer.setLevel(killer.getLevel()+lvl);
                applyLoadout(killer);
                XSound.ENTITY_PLAYER_LEVELUP.play(killer, .7f, 1.5f);
                ActionBar.sendActionbar(killer, Texter.colored(msg));
            }

            UpgradeService.getInstance().processLifeSteal(killer);
            UpgradeService.getInstance().processRandomEffect(killer);
        }

        // Handle victim downgrade
        if (victim.getGameMode() == ConfigDb.GAMEMODE_SET) {
            Bukkit.getScheduler().runTaskLater(
                    GunGame.getInstance(),
                    () -> {
                        applyLoadout(victim);
                        boolean downgraded = levelingService.addDeath(victim.getUniqueId());
                        String deathMsgPath = (downgraded) ? "downgrade" : "death";
                        String msg = GunGame.getInstance().getLangManager().get("actionbar." + deathMsgPath, "&cYou died!");
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