package eu.xap3y.gungame.manager;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.Pair;
import eu.xap3y.gungame.api.SamePair;
import eu.xap3y.gungame.model.Arena;
import eu.xap3y.gungame.util.Utils;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

@Data
public class ArenaLoader {

    private final File arenasFile;
    private YamlConfiguration arenasConfig;

    // Arena map pool
    private Deque<Arena> arenaPool = new ArrayDeque<Arena>();

    public ArenaLoader(@NotNull File arenaFile) {
        this.arenasFile = arenaFile;
        if (!arenasFile.exists()) {
            try {
                boolean created = arenasFile.createNewFile();
                // IGNORE result
            } catch (Exception e) {
                GunGame.getTexter().console("&4Failed to create arenas.yml: " + e.getMessage());
                GunGame.getTexter().debugLog(e.getMessage(), Level.SEVERE);
            }
        }
        this.arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);
    }


    public void reloadArenas() {
        this.arenasConfig = YamlConfiguration.loadConfiguration(arenasFile);
    }

    public void saveArenas() {
        try {
            arenasConfig.save(arenasFile);
        } catch (Exception e) {
            GunGame.getTexter().console("&4Failed to create arenas.yml: " + e.getMessage());
            GunGame.getTexter().debugLog(e.getMessage(), Level.SEVERE);
        }
    }

    public boolean arenaExists(@NotNull String arenaName) {
        return arenasConfig.contains(arenaName);
    }

    public Optional<Arena> loadArena(@NotNull String arenaName) {
        GunGame.getTexter().console("&fLoading arena: &e" + arenaName);
        if (!arenaExists(arenaName)) {
            GunGame.getTexter().console("&cEmpty");
            return Optional.empty();
        }
        try {
            String spawnStr = arenasConfig.getString(arenaName + ".spawn");
            if (spawnStr == null) {
                GunGame.getTexter().console("&cspawnStr empty");
                GunGame.getTexter().console("&4Arena " + arenaName + " is missing spawn location!");
                return Optional.empty();
            }
            Location loc = Utils.encodeLocation(spawnStr);
            GunGame.getTexter().console("&8&oLocation structed: &e" + loc.getWorld().getName());
            Arena arena = new Arena(
                    arenaName,
                    arenasConfig.getString(arenaName + ".displayName", arenaName),
                    arenasConfig.getBoolean(arenaName + ".enabled", false),
                    arenasConfig.getString(arenaName + ".builder", "N/A"),
                    arenasConfig.getDouble(arenaName + ".rating", 0.0),
                    loc,
                    arenasConfig.getBoolean(arenaName + ".allowBoosters", false),
                    arenasConfig.getBoolean(arenaName + ".waterKills", true),
                    loadArenaDimension(arenaName),
                    loadArenaSafeSpot(arenaName)
            );
            return Optional.of(arena);
        } catch (Exception e) {
            GunGame.getTexter().console("&4Failed to load arena " + arenaName + ": " + e.getMessage());
            GunGame.getTexter().debugLog(e.getMessage(), Level.SEVERE);
            return Optional.empty();
        }
    }

    public void saveArena(@NotNull Arena arena) {
        String arenaName = arena.getArenaName();
        arenasConfig.set(arenaName + ".enabled", arena.isEnabled());
        arenasConfig.set(arenaName + ".displayName", arena.getDisplayName());
        arenasConfig.set(arenaName + ".builder", arena.getBuilder());
        arenasConfig.set(arenaName + ".rating", arena.getRating());
        arenasConfig.set(arenaName + ".spawn", Utils.decodeLocation(arena.getSpawn()));
        arenasConfig.set(arenaName + ".allowBoosters", arena.isAllowBoosters());
        arenasConfig.set(arenaName + ".waterKills", arena.isWaterKills());
        saveArenas();
    }

    public void saveArenaSafeSpot(@NotNull String arenaName, @NotNull Pair<Location, Location> safeSpot) {
        if (!arenaExists(arenaName)) {
            return;
        }
        arenasConfig.set(arenaName + ".safe-spot.pos1", Utils.decodeLocation(safeSpot.getFirst()));
        arenasConfig.set(arenaName + ".safe-spot.pos2", Utils.decodeLocation(safeSpot.getSecond()));
        saveArenas();

        // Update arena in pool if exists
        arenaPool.stream().filter(arena -> arena.getArenaName().equals(arenaName))
                .findFirst()
                .ifPresent(arena -> arena.setSafeZone(SamePair.ofSame(safeSpot.getFirst(), safeSpot.getSecond())));
    }

    public void saveArenaDimension(@NotNull String arenaName, @NotNull Pair<Location, Location> dimension) {
        if (!arenaExists(arenaName)) {
            return;
        }
        arenasConfig.set(arenaName + ".dimension.pos1", Utils.decodeLocation(dimension.getFirst()));
        arenasConfig.set(arenaName + ".dimension.pos2", Utils.decodeLocation(dimension.getSecond()));
        saveArenas();

        // Update arena in pool if exists
        arenaPool.stream().filter(arena -> arena.getArenaName().equals(arenaName))
                .findFirst()
                .ifPresent(arena -> arena.setDimensions(SamePair.ofSame(dimension.getFirst(), dimension.getSecond())));
    }

    public @Nullable SamePair<Location> loadArenaSafeSpot(@NotNull String arenaName) {
        if (!arenaExists(arenaName)) {
            return null;
        }
        String pos1Str = arenasConfig.getString(arenaName + ".safe-spot.pos1");
        String pos2Str = arenasConfig.getString(arenaName + ".safe-spot.pos2");
        if (pos1Str == null || pos2Str == null) {
            return null;
        }
        return SamePair.ofSame(Utils.encodeLocation(pos1Str), Utils.encodeLocation(pos2Str));
    }

    public @Nullable SamePair<Location> loadArenaDimension(@NotNull String arenaName) {
        if (!arenaExists(arenaName)) {
            return null;
        }
        String pos1Str = arenasConfig.getString(arenaName + ".dimension.pos1");
        String pos2Str = arenasConfig.getString(arenaName + ".dimension.pos2");
        if (pos1Str == null || pos2Str == null) {
            return null;
        }
        return SamePair.ofSame(Utils.encodeLocation(pos1Str), Utils.encodeLocation(pos2Str));
    }

    public boolean enableArena(@NotNull String arenaName) {
        if (!arenaExists(arenaName)) return false;
        arenasConfig.set(arenaName + ".enabled", true);
        saveArenas();
        return true;
    }

    public List<Arena> loadAllArenas() {
        return arenasConfig.getKeys(false).stream()
                .map(this::loadArena)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    public void refreshArenaPool() {
        arenaPool.clear();
        arenaPool.addAll(loadAllArenas().stream().filter(Arena::isComplete).filter(Arena::isEnabled).toList());
    }

    public void disableArena(@NotNull String arenaName) {
        if (!arenaExists(arenaName)) return;
        arenasConfig.set(arenaName + ".enabled", false);
        arenaPool.removeIf(arena -> arena.getArenaName().equals(arenaName));
        saveArenas();
    }

    public boolean addArenaToPool(@NotNull Arena arena) {
        if (!arenaExists(arena.getArenaName())) {
            return false;
        }
        if (!arena.isEnabled() || !arena.isComplete()) {
            return false;
        }
        arenaPool.addLast(arena);
        return true;
    }

    public void deleteArena(@NotNull String arenaName) {
        if (!arenaExists(arenaName)) return;
        arenasConfig.set(arenaName, null);
        saveArenas();
    }

    public @Nullable Arena rotateArena() {
        if (arenaPool.isEmpty()) return null;
        Arena arena = arenaPool.pollFirst();
        arenaPool.addLast(arena);
        return arena;
    }

    public void setArenaSpawn(@NotNull String arenaName, @NotNull Location spawn) {
        if (!arenaExists(arenaName)) {
            GunGame.getTexter().console("&4Arena " + arenaName + " does not exist!");
            return;
        }
        arenasConfig.set(arenaName + ".spawn", Utils.decodeLocation(spawn));
        saveArenas();

        arenaPool.stream()
                .filter(arena -> arena.getArenaName().equals(arenaName))
                .findFirst()
                .ifPresent(arena -> arena.setSpawn(spawn));
    }
}
