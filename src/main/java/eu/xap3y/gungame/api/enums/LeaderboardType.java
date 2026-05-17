package eu.xap3y.gungame.api.enums;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.manager.LangManager;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum LeaderboardType {
    KD("&cK/D"),
    KILLS("&cKills"),
    DEATHS("&cDeaths"),
    KILL_STREAK("&cKillstreak"),
    COINS("&eCoins"),
    STAGE("&fStage");

    @Setter
    @Getter
    private String displayName;

    public static void reloadNames() {
        LangManager lang = GunGame.getInstance().getLangManager();
        if (lang == null) return;

        for (LeaderboardType type : values()) {
            type.setDisplayName(lang.get("leaderboard.labels." + type.name().toLowerCase(), type.getDisplayName()));
        }
    }


    public static LeaderboardType mapFromString(String str) {
        return switch (str.toLowerCase()) {
            case "kd" -> KD;
            case "kills" -> KILLS;
            case "deaths" -> DEATHS;
            case "killstreak" -> KILL_STREAK;
            case "coins" -> COINS;
            case "stage" -> STAGE;
            default -> null;
        };
    }
}
