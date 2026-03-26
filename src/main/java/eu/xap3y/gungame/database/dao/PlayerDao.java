package eu.xap3y.gungame.database.dao;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.enums.UpgradeEnum;
import eu.xap3y.gungame.database.DatabaseManager;
import eu.xap3y.gungame.database.dto.PlayerDto;
import eu.xap3y.gungame.database.dto.PlayerStatsDto;
import eu.xap3y.gungame.database.dto.PlayerUpgradesDto;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mariadb.jdbc.Statement;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDao {

    private final DatabaseManager dbManager;

    public PlayerDao(DatabaseManager manager) {
        this.dbManager = manager;
    }

    @Getter
    private final Map<UUID, PlayerStatsDto> playerCache = new ConcurrentHashMap<>();

    @Getter
    private final Map<UUID, PlayerUpgradesDto> playerUpgradeCache = new ConcurrentHashMap<>();

    public Mono<PlayerDto> loadOrCreate(LivingEntity p0) {
        return getOrCreate(p0)
                .doOnNext(player -> playerCache.put(player.getUuid(), player.getStats()));
    }

    public Mono<PlayerDto> getOrCreate(LivingEntity p0) {
        return getOrCreate(p0.getUniqueId(), p0.getName());
    }

    public Mono<PlayerDto> getStrict(UUID uuid) {
        return getOrCreate(uuid, null);
    }

    public Mono<Void> updateUpgradesFromCache(UUID uuid) {
        PlayerUpgradesDto upgrades = playerUpgradeCache.get(uuid);
        if (upgrades == null) return Mono.empty();

        return Mono.fromRunnable(() -> {
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO gg_player_upgrades (player_id, upgrade, level) VALUES ((SELECT id FROM gg_players WHERE uuid = ?), ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE level = ?"
                 )) {
                for (Map.Entry<UpgradeEnum, Integer> entry : upgrades.getUpgrades().entrySet()) {
                    ps.setString(1, uuid.toString());
                    ps.setInt(2, entry.getKey().ordinal());
                    ps.setInt(3, entry.getValue());
                    ps.setInt(4, entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            } catch (SQLException e) {
                GunGame.getTexter().debugLog("PlayerDao.updateUpgradesFromCache(" + uuid.toString() + ") == " + e.getMessage());
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<?> insertPlayerUpgrade(UUID uuid, UpgradeEnum upgrade, int level) {
        return Mono.fromRunnable(() -> {
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO gg_player_upgrades (player_id, upgrade, level) VALUES ((SELECT id FROM gg_players WHERE uuid = ?), ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE level = ?"
                 )) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, upgrade.ordinal());
                ps.setInt(3, level);
                ps.setInt(4, level);
                ps.executeUpdate();
            } catch (SQLException e) {
                GunGame.getTexter().debugLog("PlayerDao.insertPlayerUpgrade(" + uuid.toString() + ") == " + e.getMessage());
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<Void> loadUpgradesToCache(int playerId) {
        GunGame.getTexter().debugLog("PlayerDao.loadUpgradesToCache for player ID " + playerId);
        return Mono.fromRunnable(() -> {
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT upgrade, level FROM gg_player_upgrades WHERE player_id = ?"
                 )) {
                ps.setInt(1, playerId);
                try (ResultSet rs = ps.executeQuery()) {
                    PlayerUpgradesDto upgradesDto = new PlayerUpgradesDto();
                    while (rs.next()) {
                        int upgradeOrdinal = rs.getInt("upgrade");
                        int level = rs.getInt("level");
                        UpgradeEnum upgradeEnum = UpgradeEnum.values()[upgradeOrdinal];
                        upgradesDto.getUpgrades().put(upgradeEnum, level);
                    }
                    // We need to get the player's UUID to cache by it; we can do this with a simple query since we have the player ID
                    try (PreparedStatement uuidQuery = conn.prepareStatement(
                            "SELECT uuid FROM gg_players WHERE id = ?"
                    )) {
                        uuidQuery.setInt(1, playerId);
                        try (ResultSet uuidRs = uuidQuery.executeQuery()) {
                            if (uuidRs.next()) {
                                UUID uuid = UUID.fromString(uuidRs.getString("uuid"));
                                playerUpgradeCache.put(uuid, upgradesDto);
                            }
                        }
                    }
                }
            } catch (SQLException e) {
                GunGame.getTexter().debugLog("PlayerDao.loadUpgradesToCache(" + playerId + ") == " + e.getMessage());
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Void> loadUpgradesToCache(UUID uuid) {
        return Mono.fromRunnable(() -> {
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT upgrade, level FROM gg_player_upgrades WHERE player_id = (SELECT id FROM gg_players WHERE uuid = ?)"
                 )) {
                ps.setString(1, uuid.toString());
                try (ResultSet rs = ps.executeQuery()) {
                    PlayerUpgradesDto upgradesDto = new PlayerUpgradesDto();
                    while (rs.next()) {
                        int upgradeOrdinal = rs.getInt("upgrade");
                        int level = rs.getInt("level");
                        UpgradeEnum upgradeEnum = UpgradeEnum.values()[upgradeOrdinal];
                        upgradesDto.getUpgrades().put(upgradeEnum, level);
                    }
                    playerUpgradeCache.put(uuid, upgradesDto);
                }
            } catch (SQLException e) {
                GunGame.getTexter().debugLog("PlayerDao.loadUpgradesToCache(" + uuid.toString() + ") == " + e.getMessage());
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Integer> getPlayerUpgradeLevel(UUID uuid, @NotNull UpgradeEnum upgrade) {
        if (!playerUpgradeCache.containsKey(uuid)) {
            playerUpgradeCache.put(uuid, new PlayerUpgradesDto());
        }
        return Mono.fromCallable(() -> {
            try (Connection conn = dbManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT level FROM gg_player_upgrades WHERE player_id = (SELECT id FROM gg_players WHERE uuid = ?) AND upgrade = ?"
                 )) {
                ps.setString(1, uuid.toString());
                ps.setInt(2, upgrade.ordinal());
                try (ResultSet rs = ps.executeQuery()) {
                    int level = 0;
                    if (rs.next()) {
                        level = rs.getInt("level");
                    } else {
                        try (PreparedStatement insert = conn.prepareStatement(
                                "INSERT INTO gg_player_upgrades (player_id, upgrade, level) VALUES ((SELECT id FROM gg_players WHERE uuid = ?), ?, 0)"
                        )) {
                            insert.setString(1, uuid.toString());
                            insert.setInt(2, upgrade.ordinal());
                            insert.executeUpdate();
                        }
                    }
                    playerUpgradeCache.get(uuid).getUpgrades().put(upgrade, level);

                    return level;
                }
            } catch (SQLException e) {
                GunGame.getTexter().debugLog("PlayerDao.getPlayerUpgradeLevel(" + uuid.toString() + ") == " + e.getMessage());
                return 0;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Runs the blocking JDBC work off the main thread by scheduling onto Reactor's boundedElastic scheduler.
     * Any SQLException is propagated as an error signal.
     */
    public Mono<PlayerDto> getOrCreate(UUID uuid, String username) {

        GunGame.getTexter().debugLog("PlayerDao.getOrCreate for UUID " + uuid + " and username " + username);

        return Mono.fromCallable(() -> {
            try (Connection conn = dbManager.getConnection()) {
                int playerId = -1;

                try (PreparedStatement select = conn.prepareStatement("SELECT id FROM gg_players WHERE uuid = ?")) {
                    select.setString(1, uuid.toString());
                    GunGame.getTexter().debugLog("[3]Query: " + select.toString());
                    try (ResultSet rs0 = select.executeQuery()) {
                        if (rs0.next()) {
                            playerId = rs0.getInt("id");
                        } else {
                            try (PreparedStatement insert = conn.prepareStatement(
                                    "INSERT IGNORE INTO gg_players (uuid, username) VALUES (?, ?)",
                                    Statement.RETURN_GENERATED_KEYS
                            )) {
                                GunGame.getTexter().debugLog("[1]Query: " + insert.toString());
                                insert.setString(1, uuid.toString());
                                insert.setString(2, username);
                                int affected = insert.executeUpdate();

                                if (affected > 0) {
                                    try (ResultSet keys = insert.getGeneratedKeys()) {
                                        if (keys.next()) playerId = keys.getInt(1);
                                    }

                                    try (PreparedStatement statInsert = conn.prepareStatement(
                                            "INSERT IGNORE INTO gg_statistics (player_id) VALUES (?)"
                                    )) {
                                        GunGame.getTexter().debugLog("[2]Query: " + statInsert.toString());
                                        statInsert.setInt(1, playerId);
                                        statInsert.executeUpdate();
                                    }

                                    GunGame.getTexter().console("&a[Database] Created new player profile for " + username);
                                } else {
                                    // If insert affected 0 rows (race condition), re-query
                                    try (ResultSet rs1 = select.executeQuery()) {
                                        if (rs1.next()) playerId = rs1.getInt("id");
                                    }
                                }
                            }
                        }
                    }
                }

                if (playerId == -1) {
                    throw new SQLException("Failed to retrieve or create player ID for " + username);
                }

                try (PreparedStatement statQuery = conn.prepareStatement(
                        "SELECT kills, deaths, stage, kill_streak, best_kill_streak, best_stage, coins, xp " +
                                "FROM gg_statistics WHERE player_id = ?")) {
                    statQuery.setInt(1, playerId);
                    GunGame.getTexter().debugLog("[4]Query: " + statQuery.toString());
                    try (ResultSet rs = statQuery.executeQuery()) {
                        PlayerStatsDto stats = new PlayerStatsDto(); // default zeroed
                        if (rs.next()) {
                            stats = new PlayerStatsDto(
                                    rs.getInt("kills"),
                                    rs.getInt("deaths"),
                                    rs.getInt("stage"),
                                    rs.getInt("kill_streak"),
                                    rs.getInt("best_kill_streak"),
                                    rs.getInt("best_stage"),
                                    rs.getInt("coins"),
                                    rs.getInt("xp")
                            );
                        }
                        return new PlayerDto(playerId, uuid, username, stats);
                    }
                }

            } catch (SQLException e) {
                // propagate as unchecked so Reactor can emit onError
                GunGame.getTexter().debugLog("PlayerDao.getOrCreate(" + username + ") == " + e.getMessage());
                throw new RuntimeException(e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Increment kills and persist asynchronously. Returns a Mono that emits the updated PlayerDto.
     * The DB read (--getStrict--) and any DB writes are offloaded to boundedElastic so the main thread is not blocked.
     */
    public Mono<PlayerDto> addKill(UUID p0) {
        return getStrict(p0)
                .map(res -> {
                    res.getStats().setKills(res.getStats().getKills() + 1);
                    saveStats(res);
                    return res;
                });
    }

    /**
     * Increment deaths and persist asynchronously. Returns a Mono that emits the updated PlayerDto.
     */
    public Mono<PlayerDto> addDeath(UUID p0) {
        return getStrict(p0)
                .map(res -> {
                    res.getStats().setDeaths(res.getStats().getDeaths() + 1);
                    saveStats(res);
                    return res;
                });
    }

    public void addCacheDeath(UUID p0) {
        PlayerStatsDto stats = playerCache.get(p0);
        if (stats != null) {
            stats.setDeaths(stats.getDeaths() + 1);
        }
    }

    public void addCacheBestStage(UUID p0, int stage) {
        PlayerStatsDto stats = playerCache.get(p0);
        if (stats != null) {
            stats.setBestStage(stage);
        }
    }

    public void setCacheDeaths(UUID p0, int deaths) {
        PlayerStatsDto stats = playerCache.get(p0);
        if (stats != null) {
            stats.setDeaths(deaths);
        }
    }

    public void addCacheBestKillStreak(UUID p0, int streak) {
        PlayerStatsDto stats = playerCache.get(p0);
        if (stats != null) {
            stats.setBestKillStreak(streak);
        }
    }

    public void addCacheKill(UUID p0) {
        PlayerStatsDto stats = playerCache.get(p0);
        if (stats != null) {
            stats.setKills(stats.getKills() + 1);
        }
    }

    public void setCacheKills(UUID p0, int kills) {
        PlayerStatsDto stats = playerCache.get(p0);
        if (stats != null) {
            stats.setKills(kills);
        }
    }

    public void addCacheCoins(UUID p0, int amount) {
        PlayerStatsDto stats = playerCache.get(p0);
        if (stats != null) {
            stats.setCoins(stats.getCoins() + amount);
        }
    }

    public void addCacheXp(UUID p0, int amount) {
        PlayerStatsDto stats = playerCache.get(p0);
        if (stats != null) {
            stats.setXp(stats.getXp() + amount);
        }
    }

    public void addCacheStage(UUID p0, int stage) {
        PlayerStatsDto stats = playerCache.get(p0);
        if (stats != null) {
            stats.setStage(stage);
        }
    }

    public Mono<Void> updateFromCache(UUID p0) {
        PlayerStatsDto stats = playerCache.get(p0);
        if (stats == null) {
            return Mono.empty();
        }
        return getStrict(p0)
                .doOnNext(player -> {
                    player.setStats(stats);
                    saveStats(player);
                })
                .then();
    }

    /**
     * Schedule stats persistence off the main thread. This method returns immediately; the actual UPDATE runs asynchronously.
     */
    public void saveStats(PlayerDto player) {
        playerCache.put(player.getUuid(), player.getStats());

        Mono.fromRunnable(() -> {
                    try (Connection conn = dbManager.getConnection();
                         PreparedStatement ps = conn.prepareStatement(
                                 "UPDATE gg_statistics SET " +
                                         "kills=?, deaths=?, stage=?, kill_streak=?, best_kill_streak=?, best_stage=?, coins=?, xp=? " +
                                         "WHERE player_id=?"
                         )) {
                        PlayerStatsDto s = player.getStats();
                        ps.setInt(1, s.getKills());
                        ps.setInt(2, s.getDeaths());
                        ps.setInt(3, s.getStage());
                        ps.setInt(4, s.getKillStreak());
                        ps.setInt(5, s.getBestKillStreak());
                        ps.setInt(6, s.getBestStage());
                        ps.setInt(7, s.getCoins());
                        ps.setInt(8, s.getXp());
                        ps.setInt(9, player.getId());
                        GunGame.getTexter().debugLog("[5]Query: " + ps);
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        GunGame.getTexter().debugLog("PlayerDao.saveStats(" + player.getName() + ") == " + e.getMessage());
                        e.printStackTrace();
                    }
                })
                // run blocking JDBC work on boundedElastic
                .subscribeOn(Schedulers.boundedElastic())
                // subscribe to actually kick off the work; we intentionally do not block the caller
                .subscribe();
    }
}