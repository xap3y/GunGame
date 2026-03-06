package eu.xap3y.gungame.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.database.dao.PlayerDao;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.jspecify.annotations.NonNull;
import reactor.core.publisher.Mono;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;

public class DatabaseManager {

    private HikariDataSource dataSource;
    private final GunGame plugin;

    @Getter
    private PlayerDao playerDao;

    public DatabaseManager(GunGame plugin) {
        this.plugin = plugin;
        connect();
    }

    public void connect() {
        if (dataSource != null && !dataSource.isClosed()) return;

        HikariConfig hikari = getHikariConfig();

        // Essential for performance
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            this.dataSource = new HikariDataSource(hikari);
            GunGame.getTexter().console("&a[Database] Connected to MariaDB.");
            createTables();
            playerDao = new PlayerDao(this);
        } catch (Exception e) {
            GunGame.getTexter().console("&c[Database] Connection failed: " + e.getMessage());
        }
    }

    private @NonNull HikariConfig getHikariConfig() {
        FileConfiguration config = plugin.getConfig();
        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl("jdbc:mariadb://" + config.getString("database.host") + ":" + config.getString("database.port") + "/" + config.getString("database.database"));
        hikari.setUsername(config.getString("database.user"));
        hikari.setPassword(config.getString("database.password"));
        hikari.setDriverClassName("org.mariadb.jdbc.Driver");
        hikari.setMaximumPoolSize(10);
        return hikari;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    private void createTables() {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

                // 1. Players
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS gg_players (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "uuid VARCHAR(36) NOT NULL UNIQUE, " +
                        "username VARCHAR(16) NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ") ENGINE=InnoDB;");

                // 2. Stats (Updated with new fields)
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS gg_statistics (" +
                        "player_id INT NOT NULL PRIMARY KEY, " +
                        "kills INT DEFAULT 0, " +
                        "deaths INT DEFAULT 0, " +
                        "stage INT DEFAULT 0, " +
                        "kill_streak INT DEFAULT 0, " +
                        "best_kill_streak INT DEFAULT 0, " +
                        "coins INT DEFAULT 0, " +
                        "xp INT DEFAULT 0, " +
                        "FOREIGN KEY (player_id) REFERENCES gg_players(id) ON DELETE CASCADE" +
                        ") ENGINE=InnoDB;");

                // 3. Active Server Quests (Global state for all servers)
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS gg_server_quests (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "quest_type ENUM('DAILY', 'WEEKLY') NOT NULL, " +
                        "quest_index INT NOT NULL, " +
                        "quest_enum VARCHAR(50) NOT NULL, " +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "UNIQUE(quest_type, quest_index)" +
                        ") ENGINE=InnoDB;");

                // 4. Player Quest Progress
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS gg_player_quest_progress (" +
                        "player_id INT NOT NULL, " +
                        "quest_enum VARCHAR(50) NOT NULL, " +
                        "progress INT DEFAULT 0, " +
                        "is_completed BOOLEAN DEFAULT FALSE, " +
                        "completed_at TIMESTAMP NULL, " +
                        "FOREIGN KEY (player_id) REFERENCES gg_players(id) ON DELETE CASCADE, " +
                        "UNIQUE(player_id, quest_enum)" +
                        ") ENGINE=InnoDB;");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    // ... disconnect/reconnect logic
}