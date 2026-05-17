package eu.xap3y.gungame.manager;

import com.cryptomorin.xseries.XSound;
import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.event.MapChangeEvent;
import eu.xap3y.gungame.model.Arena;
import eu.xap3y.gungame.service.Texter;
import eu.xap3y.gungame.util.ActionBar;
import eu.xap3y.gungame.util.ConfigDb;
import eu.xap3y.gungame.util.UpgradeUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("deprecation")
@NoArgsConstructor
public class ArenaManager {

    @Getter
    private Arena currentArena;

    @Getter
    private final List<Player> players = new java.util.ArrayList<>();

    @Getter
    private final Map<UUID, Player> lastDamager = new java.util.HashMap<>();

    private BukkitTask arenaRotationTask;

    @Getter
    private LocalDateTime arenaRotationStartTime;

    @Getter
    private LocalDateTime nextArenaTime;

    private static final int ARENA_ROTATE_PRE_COUNTDOWN = 15;

    public long getSecondsUntilNextArena() {
        if (nextArenaTime == null) return -1;
        return Duration.between(LocalDateTime.now(), nextArenaTime).getSeconds();
    }

    public void respawnPlayer(@NotNull Player p0) {
        p0.teleport(currentArena.getSpawn());
    }

    public void changeArena(Arena arena) {
        GunGame.getTexter().console("&7Changing arena to: &e" + arena.getArenaName());
        GunGame.getTexter().debugLog("changeArena to " + arena.getArenaName());
        resetArena();
        currentArena = arena;
        arena.getSpawn().getWorld().setSpawnLocation(arena.getSpawn().getBlockX(), arena.getSpawn().getBlockY(), arena.getSpawn().getBlockZ());
        Bukkit.getScheduler().runTask(GunGame.getInstance(), () -> arena.getSpawn().getWorld().setTime(1000));
        arenaRotationStartTime = LocalDateTime.now();
        restartArenaRotationTask();
    }

    public void resetArena() {
        if (currentArena != null) {
            currentArena = null;
        }
    }

    public void addPlayer(@NotNull Player player) {
        if (currentArena == null) return;
        if (players.stream().noneMatch((p) -> p.getUniqueId().equals(player.getUniqueId()))) {
            players.add(player);
        }
    }

    public void addLastDamager(@NotNull Player player, @NotNull UUID victimId) {
        lastDamager.put(victimId, player);
    }

    public void removeLastDamager(@NotNull Player player) {
        lastDamager.entrySet().removeIf(e -> e.getValue().getUniqueId().equals(player.getUniqueId()));
    }

    public void removeLastDamagerByVictim(@NotNull UUID victimId) {
        lastDamager.remove(victimId);
    }

    public void clearLastDamagers() {
        lastDamager.clear();
    }

    public @Nullable Player getPlayerByDamager(@NotNull UUID damagerId) {
        Optional<Map.Entry<UUID, Player>> entry = lastDamager.entrySet().stream().filter(e -> e.getValue().getUniqueId().equals(damagerId)).findFirst();
        return entry.map(Map.Entry::getValue).orElse(null);
    }

    public @Nullable Player getPlayerByVictim(@NotNull UUID damagerId) {
        return lastDamager.get(damagerId);
    }

    public void removePlayer(@NotNull UUID playerId) {
        if (currentArena == null) return;
        players.removeIf((p) -> p.getUniqueId().equals(playerId));
    }

    public void teleportPlayerToSpawn(@NotNull UUID playerId) {
        if (currentArena == null) return;
        Optional<Player> player = players.stream().filter((p) -> p.getUniqueId().equals(playerId)).findFirst();
        if (player.isEmpty()) return;
        player.get().teleport(currentArena.getSpawn());
    }

    public void teleportAllSpawn() {
        GunGame.getTexter().debugLog("teleportAllSpawn()");
        if (currentArena == null) return;
        GunGame.getTexter().console("&8&oScheduled teleport of " + players.size() + " players to arena spawn...");
        Bukkit.getScheduler().runTask(GunGame.getInstance(), () -> {
            players.forEach((p) -> p.teleport(currentArena.getSpawn()));
        });
    }

    public boolean isPlayerInArena(@NotNull UUID playerId) {
        if (currentArena == null) return false;
        return players.stream().anyMatch((p) -> p.getUniqueId().equals(playerId));
    }

    public void preparePlayer(@NotNull Player player) {
        player.getInventory().clear();

        UpgradeUtil.process(
                player
        );

        Bukkit.getScheduler().runTaskLater(
                GunGame.getInstance(),
                () -> player.setGameMode(ConfigDb.GAMEMODE_SET),
                3L
        );
    }

    public void joinPlayerToArena(@NotNull Player player, boolean delayJoin) {

        GunGame.getTexter().debugLog("joinPlayerToArena(" + player.getName() + ", " + delayJoin + ")");

        if (currentArena == null) {
            GunGame.getTexter().debugLog("currentArena == null, cant join player " + player.getName());
            return;
        }
        addPlayer(player);
        if (delayJoin) {
            player.teleport(currentArena.getSpawn());
            Bukkit.getScheduler().runTaskLater(GunGame.getInstance(), () -> {
                if (isPlayerInArena(player.getUniqueId())) {
                    player.teleport(currentArena.getSpawn());
                    preparePlayer(player);
                }
            }, 10L);
            loadPlayerDb(player);
            return;
        } else {
            player.teleport(currentArena.getSpawn());
            preparePlayer(player);
        }

        Bukkit.getScheduler().runTask(GunGame.getInstance(), () -> {
            player.setRespawnLocation(currentArena.getSpawn(), true);
        });

        GunGame.getTexter().logPos();
        loadPlayerDb(player);
    }

    private void loadPlayerDb(@NotNull LivingEntity player) {
        GunGame.getInstance().getDatabaseManager().getPlayerDao().loadOrCreate(player)
                .publishOn(Schedulers.boundedElastic())

                // Load upgrades to cache if player
                .doOnSuccess(p -> {
                    if (p == null) return;
                    GunGame.getInstance().getDatabaseManager().getPlayerDao().loadUpgradesToCache(p.getId()).subscribe();
                })

                // Update scoreboard
                .doOnNext(s -> {
                    GunGame.getBoardApi().updateBoard(player.getUniqueId());
                })
                .subscribe();
    }

    public void joinPlayerToArena(@NotNull Player player) {
        joinPlayerToArena(player, false);
    }

    public void leavePlayerFromArena(@NotNull Player player) {
        if (!isPlayerInArena(player.getUniqueId())) return;
        removePlayer(player.getUniqueId());
        player.teleport(currentArena.getSpawn());
        player.getInventory().clear();
        removeLastDamager(player);
        GunGame.getInstance().getLevelingService().reset(player.getUniqueId());
    }

    public Arena getNextArena() {
        return GunGame.getInstance().getArenaLoader().getArenaPool().peek();
    }

    public void rotateArenaPre() {
        String countdownStr = GunGame.getInstance().getLangManager().get("arena-map-change.countdown", "&7Next map change in &e{time}&7 seconds.");

        BukkitTask finalCountdown = new BukkitRunnable() {
            int secs = ARENA_ROTATE_PRE_COUNTDOWN;

            @Override
            public void run() {
                if (secs <= 0) {
                    cancel();
                    rotateArena();
                    return;
                }
                if (secs == 5) {
                    Bukkit.getScheduler().runTask(GunGame.getInstance(), () -> GunGame.getInstance().getArenaManager().getNextArena().getSpawn().getWorld().setTime(1000));
                }
                if (secs == 15 || secs == 10 || secs <= 5) {
                    String msg = countdownStr.replace("{time}", String.valueOf(secs));
                    players.forEach(p -> {
                        ActionBar.sendActionbar(p, Texter.colored(msg));
                        if (secs <= 5) {
                            XSound.BLOCK_NOTE_BLOCK_BASS.play(p, .6f, 1f);
                        }
                    });
                }
                secs -= 1;
            }
        }.runTaskTimerAsynchronously(GunGame.getInstance(), 0L, 20L);
    }

    public void cancelArenaRotation() {
        GunGame.getTexter().logPos();
        if (arenaRotationTask != null) {
            arenaRotationTask.cancel();
            arenaRotationTask = null;
            nextArenaTime = null;
            GunGame.getTexter().console("&aArena rotation cancelled.");
        }
    }

    // Change arena
    public void rotateArena() {
        GunGame.getTexter().debugLog("rotateArena() [map]");
        Arena arena = GunGame.getInstance().getArenaLoader().rotateArena();
        if (arena != null) {
            GunGame.getTexter().debugLog("next-map: " + arena.getArenaName() + " | " + arena.getSpawn().toString());
            MapChangeEvent mapChangeEvent = new MapChangeEvent(arena);
            Bukkit.getScheduler().runTask(GunGame.getInstance(), () ->Bukkit.getServer().getPluginManager().callEvent(mapChangeEvent));
            if (mapChangeEvent.isCancelled()) {
                GunGame.getTexter().console("&4Arena rotation cancelled by event listener!");
                restartArenaRotationTask();
                return;
            }
            changeArena(arena);
            String arenaMsg = GunGame.getInstance().getLangManager().get("arena-rotated", "&6Arena has been rotated to &e{arena}&6!");
            arenaMsg = arenaMsg.replace("{arena}", arena.getDisplayName());
            GunGame.getTexter().broadcast(arenaMsg);

            String titleMsg = GunGame.getInstance().getLangManager().get("arena-map-change.title", "&6Arena Rotated!")
                    .replace("{arena}", arena.getDisplayName());

            getPlayers()
                    .stream()
                    .filter(Player::isOnline)
                    .forEach(p -> {
                        p.setVelocity(p.getVelocity().zero());
                        GunGame.getInstance().getServer().getScheduler().runTask(GunGame.getInstance(), () -> {
                            p.setRespawnLocation(arena.getSpawn(), true);
                        });
                        ConfigDb.FALL_DAMAGE_CANCEL.add(p.getUniqueId());
                        GunGame.getInstance().getServer().getScheduler().runTaskLaterAsynchronously(GunGame.getInstance(), () -> {
                            ConfigDb.FALL_DAMAGE_CANCEL.remove(p.getUniqueId());
                        }, 10L);
                        ActionBar.sendActionbar(p, "");
                        p.sendTitle(Texter.colored(titleMsg), "", 20, 70, 20);
                        //Titles.sendTitle(p, 20, 70, 20, Texter.colored(titleMsg), "");
                        XSound.BLOCK_NOTE_BLOCK_PLING.play(p, .8f, 1f);
                    });

            teleportAllSpawn();
            ConfigDb.LAST_LAUNCHES.clear();
            if (GunGame.getBoardApi() != null) GunGame.getBoardApi().reloadAllBoards();
            Arena next = GunGame.getInstance().getArenaLoader().getArenaPool().peek();
            GunGame.getTexter().console("&8&o[C]&r &aRotated arena to: &e" + arena.getArenaName() + "&a. Next arena: &e" + (next != null ? next.getArenaName() : "&cN/A"));
        } else {
            GunGame.getTexter().broadcast("&4Failed to rotate arena: no arenas available!");
        }
        restartArenaRotationTask();
    }

    // Restart rotate task, called on plugin onLoad or when adding/removing arenas
    // Only schedules a new task, actual rotation is #rotateArena(Arena arena)
    public void restartArenaRotationTask() {
        GunGame.getTexter().logPos();
        if (arenaRotationTask != null) {
            arenaRotationTask.cancel();
        }
        if (GunGame.getInstance().getArenaLoader().getArenaPool().isEmpty()) {
            GunGame.getTexter().console("&4No arenas available in pool, cannot start rotation!");
            return;
        } else if (currentArena == null) {
            //rotateArena();
            rotateArena();
            return;
        }
        boolean enabled = GunGame.getInstance().getConfig().getBoolean("arena-rotate.enabled", false);
        if (!enabled) return;
        int interval = GunGame.getInstance().getConfig().getInt("arena-rotate.interval", 900);
        nextArenaTime = LocalDateTime.now().plusSeconds(interval);
        arenaRotationTask = Bukkit.getScheduler().runTaskLaterAsynchronously(
                GunGame.getInstance(),
                this::rotateArenaPre,
                (interval * 20L) - ARENA_ROTATE_PRE_COUNTDOWN * 20L
        );
    }
}
