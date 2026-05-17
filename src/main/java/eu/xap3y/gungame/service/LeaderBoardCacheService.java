package eu.xap3y.gungame.service;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.enums.LeaderboardType;
import eu.xap3y.gungame.model.Leaderboard;
import eu.xap3y.gungame.util.ConfigDb;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@NoArgsConstructor
public class LeaderBoardCacheService {

    private final Map<LeaderboardType, Leaderboard> cache = new ConcurrentHashMap<>();

    public void refreshWholeCache() {
        for (LeaderboardType type : LeaderboardType.values()) {
            refreshCacheForType(type);
        }
    }

    public void refreshCacheForType(LeaderboardType type) {
        GunGame.getInstance().getDatabaseManager().getPlayerDao().getLeaderBoard(type, ConfigDb.MAX_LB_ENTRIES)
                .doOnSuccess(entry -> {
                    cache.put(type, entry);
                    GunGame.getTexter().console("&aSuccessfully refreshed leaderboard cache for " + type.name());
                })
                .doOnError(err -> {
                    GunGame.getTexter().console("&cFailed to refresh leaderboard cache for " + type.name() + ": " + err.getMessage());
                })
                .subscribe();
    }

    public Leaderboard getLeaderBoard(LeaderboardType type) {
        return cache.get(type);
    }

}
