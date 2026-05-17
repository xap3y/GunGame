package eu.xap3y.gungame.manager;

import eu.xap3y.gungame.api.enums.LeaderboardType;
import eu.xap3y.gungame.model.Leaderboard;
import eu.xap3y.gungame.service.LeaderBoardCacheService;
import lombok.Getter;


public class LeaderBoardManager {

    private static LeaderBoardManager instance;

    @Getter
    private final LeaderBoardCacheService cacheService = new LeaderBoardCacheService();

    public LeaderBoardManager() {
        instance = this;
    }

    public static LeaderBoardManager get() {
        if (instance == null) {
            instance = new LeaderBoardManager();
        }
        return instance;
    }

    public Leaderboard getLeaderBoard(LeaderboardType type) {
        return cacheService.getLeaderBoard(type);
    }
}
