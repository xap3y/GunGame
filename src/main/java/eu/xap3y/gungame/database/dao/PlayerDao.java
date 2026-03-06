package eu.xap3y.gungame.database.dao;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.database.DatabaseManager;
import eu.xap3y.gungame.database.dto.PlayerDto;
import eu.xap3y.gungame.database.dto.PlayerStatsDto;
import lombok.Getter;
import org.bukkit.entity.LivingEntity;
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

    public Mono<Void> loadOrCreate(LivingEntity p0) {
        return getOrCreate(p0)
                .doOnNext(player -> playerCache.put(player.getUuid(), player.getStats()))
                .then();
    }

    public Mono<PlayerDto> getOrCreate(LivingEntity p0) {
        return getOrCreate(p0.getUniqueId(), p0.getName());
    }

    public Mono<PlayerDto> getStrict(UUID uuid) {
        return getOrCreate(uuid, null);
    }

    /**
     * Runs the blocking JDBC work off the main thread by scheduling onto Reactor's boundedElastic scheduler.
     * Any SQLException is propagated as an error signal.
     */
    public Mono<PlayerDto> getOrCreate(UUID uuid, String username) {
        return Mono.fromCallable(() -> {
            try (Connection conn = dbManager.getConnection()) {
                int playerId = -1;

                try (PreparedStatement select = conn.prepareStatement("SELECT id FROM gg_players WHERE uuid = ?")) {
                    select.setString(1, uuid.toString());
                    try (ResultSet rs0 = select.executeQuery()) {
                        if (rs0.next()) {
                            playerId = rs0.getInt("id");
                        } else {
                            try (PreparedStatement insert = conn.prepareStatement(
                                    "INSERT IGNORE INTO gg_players (uuid, username) VALUES (?, ?)",
                                    Statement.RETURN_GENERATED_KEYS
                            )) {
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
                        "SELECT kills, deaths, stage, kill_streak, best_kill_streak, coins, xp " +
                                "FROM gg_statistics WHERE player_id = ?")) {
                    statQuery.setInt(1, playerId);
                    try (ResultSet rs = statQuery.executeQuery()) {
                        PlayerStatsDto stats = new PlayerStatsDto(); // default zeroed
                        if (rs.next()) {
                            stats = new PlayerStatsDto(
                                    rs.getInt("kills"),
                                    rs.getInt("deaths"),
                                    rs.getInt("stage"),
                                    rs.getInt("kill_streak"),
                                    rs.getInt("best_kill_streak"),
                                    rs.getInt("coins"),
                                    rs.getInt("xp")
                            );
                        }
                        return new PlayerDto(playerId, uuid, username, stats);
                    }
                }

            } catch (SQLException e) {
                // propagate as unchecked so Reactor can emit onError
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

    public void addCacheKill(UUID p0) {
        PlayerStatsDto stats = playerCache.get(p0);
        if (stats != null) {
            stats.setKills(stats.getKills() + 1);
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
                                         "kills=?, deaths=?, stage=?, kill_streak=?, best_kill_streak=?, coins=?, xp=? " +
                                         "WHERE player_id=?"
                         )) {
                        PlayerStatsDto s = player.getStats();
                        ps.setInt(1, s.getKills());
                        ps.setInt(2, s.getDeaths());
                        ps.setInt(3, s.getStage());
                        ps.setInt(4, s.getKillStreak());
                        ps.setInt(5, s.getBestKillStreak());
                        ps.setInt(6, s.getCoins());
                        ps.setInt(7, s.getXp());
                        ps.setInt(8, player.getId());
                        ps.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                })
                // run blocking JDBC work on boundedElastic
                .subscribeOn(Schedulers.boundedElastic())
                // subscribe to actually kick off the work; we intentionally do not block the caller
                .subscribe();
    }
}