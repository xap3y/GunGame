package eu.xap3y.gungame.api.enums;

import lombok.Getter;
import org.bukkit.entity.Player;
import java.util.function.Consumer;

public enum QuestType {

    // --- DAILY QUESTS ---
    DAILY_KILL_15("Butcher", "Kill 15 players.", 15, (p) -> {
        p.sendMessage("§aReward: 100 Coins!");
    }),

    DAILY_STREAK_5("Unstoppable", "Reach a killstreak of 5.", 5, (p) -> {
        p.sendMessage("§aReward: 200 XP!");
    }),

    DAILY_PLAY_GAMES("Gamer", "Play 3 games.", 3, (p) -> {
        p.sendMessage("§aReward: 50 Coins!");
    }),


    // --- WEEKLY QUESTS ---
    WEEKLY_KILL_100("Terminator", "Kill 100 players.", 100, (p) -> {
        p.sendMessage("§6Weekly Reward: 1000 Coins!");
    }),

    WEEKLY_HEADSHOTS("Sniper", "Get 20 Headshots.", 20, (p) -> {
        p.sendMessage("§6Weekly Reward: 500 XP!");
    });

    @Getter
    private final String displayName;
    private final String description;
    @Getter
    private final int target;
    private final Consumer<Player> reward;

    QuestType(String name, String desc, int target, Consumer<Player> reward) {
        this.displayName = name;
        this.description = desc;
        this.target = target;
        this.reward = reward;
    }

    public void giveReward(Player p) { reward.accept(p); }
}