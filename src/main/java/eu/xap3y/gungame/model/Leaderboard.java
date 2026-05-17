package eu.xap3y.gungame.model;

import eu.xap3y.gungame.api.Pair;
import eu.xap3y.gungame.api.enums.LeaderboardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.LinkedList;

@Getter
public class Leaderboard {
    private final LeaderboardType type;

    // Map of player names to their respective values for this leaderboard type
    private final LinkedList<Entry> entries;

    public Leaderboard(LeaderboardType type, LinkedList<Entry> entries) {
        this.type = type;
        this.entries = entries;
    }

    public LinkedList<Entry> getTopEntries(int limit) {
        return new LinkedList<>(entries.subList(0, Math.min(limit, entries.size())));
    }

    public Entry getPosition(int position) {
        return entries.stream()
                .filter(entry -> entry.getPosition() == position)
                .findFirst()
                .orElse(null);
    }

    @AllArgsConstructor
    @Builder
    @Data
    public static class Entry {
        private final String playerName;
        private final int score;
        private final int position;
    }

}