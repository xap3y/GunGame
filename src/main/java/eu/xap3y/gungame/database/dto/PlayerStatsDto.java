package eu.xap3y.gungame.database.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerStatsDto {
    private int kills;
    private int deaths;
    private int stage;
    private int killStreak;
    private int bestKillStreak;
    private int bestStage;
    private int coins;
    private int xp;

    public PlayerStatsDto() {
        this.kills = 0;
        this.deaths = 0;
        this.stage = 0;
        this.killStreak = 0;
        this.bestKillStreak = 0;
        this.bestStage = 0;
        this.coins = 0;
        this.xp = 0;
    }
}
