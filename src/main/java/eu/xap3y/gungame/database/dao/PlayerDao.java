package eu.xap3y.gungame.database.dao;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.database.DatabaseManager;
import eu.xap3y.gungame.database.dto.PlayerDto;
import eu.xap3y.gungame.database.dto.PlayerStatsDto;
import lombok.AllArgsConstructor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.mariadb.jdbc.Statement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@AllArgsConstructor
public class PlayerDao {

    private final DatabaseManager dbManager;

    public CompletableFuture<PlayerDto> getOrCreate(LivingEntity p0) {
        return getOrCreate(p0.getUniqueId(), p0.getName());

    }
    public CompletableFuture<PlayerDto> getOrCreate(UUID uuid, String username) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = dbManager.getConnection()) {
                int playerId = -1;

                PreparedStatement select = conn.prepareStatement("SELECT id FROM gg_players WHERE uuid = ?");
                select.setString(1, uuid.toString());
                ResultSet rs0 = select.executeQuery();

                if (rs0.next()) {
                    playerId = rs0.getInt("id");
                } else {
                    PreparedStatement insert = conn.prepareStatement(
                            "INSERT IGNORE INTO gg_players (uuid, username) VALUES (?, ?)",
                            Statement.RETURN_GENERATED_KEYS
                    );
                    insert.setString(1, uuid.toString());
                    insert.setString(2, username);
                    int affected = insert.executeUpdate();

                    if (affected > 0) {
                        ResultSet keys = insert.getGeneratedKeys();
                        if (keys.next()) playerId = keys.getInt(1);

                        PreparedStatement statInsert = conn.prepareStatement(
                                "INSERT IGNORE INTO gg_statistics (player_id) VALUES (?)"
                        );
                        statInsert.setInt(1, playerId);
                        statInsert.executeUpdate();

                        GunGame.getTexter().console("&a[Database] Created new player profile for " + username);
                    } else {
                        rs0 = select.executeQuery();
                        if (rs0.next()) playerId = rs0.getInt("id");
                    }
                }

                if (playerId == -1) {
                    throw new SQLException("Failed to retrieve or create player ID for " + username);
                }

                PreparedStatement statQuery = conn.prepareStatement(
                        "SELECT kills, deaths, stage, kill_streak, best_kill_streak, coins, xp " +
                                "FROM gg_statistics WHERE player_id = ?");
                statQuery.setInt(1, playerId);
                ResultSet rs = statQuery.executeQuery();

                PlayerStatsDto stats = new PlayerStatsDto(); // Uses default constructor (all 0)
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

            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }
        });
    }

    public void saveStats(PlayerDto player) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = dbManager.getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "UPDATE gg_statistics SET " +
                                "kills=?, deaths=?, stage=?, kill_streak=?, best_kill_streak=?, coins=?, xp=? " +
                                "WHERE player_id=?"
                );
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
        });
    }
}
