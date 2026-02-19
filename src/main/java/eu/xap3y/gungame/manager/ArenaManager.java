package eu.xap3y.gungame.manager;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.model.Arena;
import eu.xap3y.gungame.util.ConfigDb;
import eu.xap3y.gungame.util.UpgradeUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    public void respawnPlayer(@NotNull Player p0) {
        p0.teleport(currentArena.getSpawn());
    }

    public void changeArena(Arena arena) {
        GunGame.getTexter().console("[=] &6Changing arena to: " + arena.getArenaName());
        resetArena();
        currentArena = arena;
        arenaRotationStartTime = LocalDateTime.now();
        restartArenaRotationTask();
        teleportAllSpawn();
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
        if (currentArena == null) return;
        GunGame.getTexter().console("&fscheduled teleport of " + players.size() + " players to arena spawn...");
        Bukkit.getScheduler().runTask(GunGame.getInstance(), () -> {
            players.forEach((p) -> p.teleport(currentArena.getSpawn()));
        });
    }

    public boolean isPlayerInArena(@NotNull UUID playerId) {
        if (currentArena == null) return false;
        return players.stream().anyMatch((p) -> p.getUniqueId().equals(playerId));
    }

    public void joinPlayerToArena(@NotNull Player player) {
        if (currentArena == null) return;
        addPlayer(player);
        player.teleport(currentArena.getSpawn());
        player.getInventory().clear();

        UpgradeUtil.process(
                player
        );

        Bukkit.getScheduler().runTaskLater(
                GunGame.getInstance(),
                () -> player.setGameMode(ConfigDb.GAMEMODE_SET),
                3L
        );

        GunGame.getInstance().getDatabaseManager().getPlayerDao().getOrCreate(player);
    }

    public void leavePlayerFromArena(@NotNull Player player) {
        if (!isPlayerInArena(player.getUniqueId())) return;
        removePlayer(player.getUniqueId());
        player.teleport(currentArena.getSpawn());
        player.getInventory().clear();
        removeLastDamager(player);
        GunGame.getInstance().getLevelingService().reset(player.getUniqueId());
    }

    public void rotateArena() {
        Arena arena = GunGame.getInstance().getArenaLoader().rotateArena();
        if (arena != null) {
            changeArena(arena);
            String arenaMsg = GunGame.getInstance().getLangManager().get("arena-rotated", "&6Arena has been rotated to &e{arena}&6!");
            arenaMsg = arenaMsg.replace("{arena}", arena.getArenaName());
            GunGame.getTexter().broadcast(arenaMsg);
            Arena next = GunGame.getInstance().getArenaLoader().getArenaPool().peek();
            GunGame.getTexter().console("&aRotated arena to: &e" + arena.getArenaName() + "&a. Next arena: &e" + (next != null ? next.getArenaName() : "&cN/A"));
        } else {
            GunGame.getTexter().broadcast("&4Failed to rotate arena: no arenas available!");
        }
        restartArenaRotationTask();
    }

    // Restart rotate task, called on plugin onLoad or when adding/removing arenas
    // Only schedules a new task, actual rotation is #rotateArena(Arena arena)
    public void restartArenaRotationTask() {
        if (arenaRotationTask != null) {
            arenaRotationTask.cancel();
        }
        if (GunGame.getInstance().getArenaLoader().getArenaPool().isEmpty()) {
            GunGame.getTexter().console("&4No arenas available in pool, cannot start rotation!");
            return;
        } else if (currentArena == null) {
            rotateArena();
            return;
        }
        boolean enabled = GunGame.getInstance().getConfig().getBoolean("arena-rotate.enabled", false);
        if (!enabled) return;
        int interval = GunGame.getInstance().getConfig().getInt("arena-rotate.interval", 900);
        nextArenaTime = LocalDateTime.now().plusSeconds(interval);
        arenaRotationTask = Bukkit.getScheduler().runTaskLaterAsynchronously(
                GunGame.getInstance(),
                () -> {
                    rotateArena();
                },
                interval * 20L
        );
    }
}
