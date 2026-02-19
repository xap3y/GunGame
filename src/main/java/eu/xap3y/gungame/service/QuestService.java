package eu.xap3y.gungame.service;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.enums.QuestType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

// TODO
public class QuestService {

    private final GunGame plugin;
    // Current Active Quests (Loaded from DB)
    private final Map<Integer, QuestType> activeDailyQuests = new HashMap<>();
    private final Map<Integer, QuestType> activeWeeklyQuests = new HashMap<>();

    // Player Cache: UUID -> Map<QuestEnum, Progress>
    private final Map<UUID, Map<QuestType, Integer>> playerProgressCache = new HashMap<>();
    private final Map<UUID, Set<QuestType>> completedQuestsCache = new HashMap<>();

    public QuestService(GunGame plugin) {
        this.plugin = plugin;
        loadActiveServerQuests(); // Sync with DB on startup
    }

    /**
     * Called when a player does something (e.g., gets a kill).
     * Usage: questService.addProgress(player, QuestType.DAILY_KILL_15, 1);
     */
    public void addProgress(Player player, QuestType type, int amount) {
        if (!isActive(type)) return; // Don't track if this quest isn't active today
        if (isCompleted(player, type)) return; // Already done

        UUID uuid = player.getUniqueId();
        int current = playerProgressCache.computeIfAbsent(uuid, k -> new HashMap<>())
                .getOrDefault(type, 0);
        int newProgress = current + amount;

        // Update Cache
        playerProgressCache.get(uuid).put(type, newProgress);

        // Check Completion
        if (newProgress >= type.getTarget()) {
            completeQuest(player, type);
        } else {
            // Save progress to DB async (maybe not every single kill, but frequently)
            saveProgressAsync(player.getUniqueId(), type, newProgress, false);
        }
    }

    private void completeQuest(Player player, QuestType type) {
        player.sendMessage("§a§lQUEST COMPLETED: §e" + type.getDisplayName());
        type.giveReward(player);

        completedQuestsCache.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(type);
        saveProgressAsync(player.getUniqueId(), type, type.getTarget(), true);
    }

    /**
     * Check if a specific quest is currently active on the server
     */
    private boolean isActive(QuestType type) {
        return activeDailyQuests.containsValue(type) || activeWeeklyQuests.containsValue(type);
    }

    private boolean isCompleted(Player p, QuestType type) {
        return completedQuestsCache.getOrDefault(p.getUniqueId(), Collections.emptySet()).contains(type);
    }

    // --- DB SYNC METHODS ---

    /**
     * Loads what quests are active for ALL servers.
     * If none exist (or are old), it generates new ones.
     */
    public void loadActiveServerQuests() {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                // Logic to check if quests are outdated would go here (checking timestamps)
                // For simplicity, we just load what's there.

                PreparedStatement ps = conn.prepareStatement("SELECT * FROM gg_server_quests");
                ResultSet rs = ps.executeQuery();

                activeDailyQuests.clear();
                activeWeeklyQuests.clear();

                while (rs.next()) {
                    String typeStr = rs.getString("quest_type");
                    String enumStr = rs.getString("quest_enum");
                    int index = rs.getInt("quest_index");

                    try {
                        QuestType qt = QuestType.valueOf(enumStr);
                        if (typeStr.equals("DAILY")) activeDailyQuests.put(index, qt);
                        else activeWeeklyQuests.put(index, qt);
                    } catch (IllegalArgumentException e) {
                        GunGame.getTexter().console("&cUnknown quest in DB: " + enumStr);
                    }
                }

                // If DB was empty, generate new ones (First server to boot does this)
                if (activeDailyQuests.isEmpty()) generateNewQuests("DAILY");
                if (activeWeeklyQuests.isEmpty()) generateNewQuests("WEEKLY");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void loadPlayerProgress(UUID uuid) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT quest_enum, progress, is_completed FROM gg_player_quest_progress WHERE player_id = (SELECT id FROM gg_players WHERE uuid = ?)"
                );
                ps.setString(1, uuid.toString());
                ResultSet rs = ps.executeQuery();

                Map<QuestType, Integer> progressMap = new HashMap<>();
                Set<QuestType> completedSet = new HashSet<>();

                while (rs.next()) {
                    try {
                        QuestType qt = QuestType.valueOf(rs.getString("quest_enum"));
                        if (isActive(qt)) {
                            boolean done = rs.getBoolean("is_completed");
                            if (done) completedSet.add(qt);
                            else progressMap.put(qt, rs.getInt("progress"));
                        }
                    } catch (Exception ignored) {}
                }

                // Sync back to main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    playerProgressCache.put(uuid, progressMap);
                    completedQuestsCache.put(uuid, completedSet);
                });

            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void saveProgressAsync(UUID uuid, QuestType type, int progress, boolean completed) {
        CompletableFuture.runAsync(() -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO gg_player_quest_progress (player_id, quest_enum, progress, is_completed) " +
                                "VALUES ((SELECT id FROM gg_players WHERE uuid=?), ?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE progress=?, is_completed=?"
                );
                ps.setString(1, uuid.toString());
                ps.setString(2, type.name());
                ps.setInt(3, progress);
                ps.setBoolean(4, completed);
                ps.setInt(5, progress);
                ps.setBoolean(6, completed);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    // Helper to generate quests if DB is empty
    private void generateNewQuests(String type) {
        // Implementation: Pick 3 random enums from QuestType and insert into gg_server_quests
        // This ensures all servers see the same random quests.
    }
}