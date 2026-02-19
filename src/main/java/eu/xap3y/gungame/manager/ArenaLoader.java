package eu.xap3y.gungame.manager;

import eu.xap3y.gungame.GunGame;
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
        GunGame.getTexter().console("&6Loading arena: " + arenaName);
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
            Arena arena = new Arena(
                    arenaName,
                    arenasConfig.getBoolean(arenaName + ".enabled", false),
                    arenasConfig.getString(arenaName + ".builder", "N/A"),
                    arenasConfig.getDouble(arenaName + ".rating", 0.0),
                    loc,
                    arenasConfig.getBoolean(arenaName + ".allowBoosters", false),
                    arenasConfig.getBoolean(arenaName + ".waterKills", true)
            );
            GunGame.getTexter().console("&afound");
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
        arenasConfig.set(arenaName + ".builder", arena.getBuilder());
        arenasConfig.set(arenaName + ".rating", arena.getRating());
        arenasConfig.set(arenaName + ".spawn", Utils.decodeLocation(arena.getSpawn()));
        arenasConfig.set(arenaName + ".allowBoosters", arena.isAllowBoosters());
        arenasConfig.set(arenaName + ".waterKills", arena.isWaterKills());
        saveArenas();
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
        arenaPool.addAll(loadAllArenas().stream().filter(Arena::isEnabled).toList());
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

        GunGame.getTexter().console("[dev] &ffirst: &e" + arenaPool.getFirst().getArenaName());
        GunGame.getTexter().console("[dev] &flast: &e" + arenaPool.getLast().getArenaName());
        GunGame.getTexter().console("[dev] &fpeek: &e" + arenaPool.peek().getArenaName());
        return arena;
    }
}
