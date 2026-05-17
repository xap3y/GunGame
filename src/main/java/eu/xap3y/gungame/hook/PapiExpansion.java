package eu.xap3y.gungame.hook;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.Pair;
import eu.xap3y.gungame.api.enums.LeaderboardType;
import eu.xap3y.gungame.database.dao.PlayerDao;
import eu.xap3y.gungame.database.dto.PlayerStatsDto;
import eu.xap3y.gungame.manager.LeaderBoardManager;
import eu.xap3y.gungame.model.Leaderboard;
import eu.xap3y.gungame.service.LeaderBoardCacheService;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

public class PapiExpansion extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "gungame";
    }

    @Override
    public @NotNull String getAuthor() {
        return "XAP3Y";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) {
            return null;
        }

        String[] args = params.split("_");

        PlayerDao dao = GunGame.getInstance().getDatabaseManager().getPlayerDao();

        switch (args[0].toLowerCase()) {
            case "leaderboard":
                if (args.length < 3) {
                    return null;
                }
                String type = args[1].toLowerCase();
                int position;
                try {
                    position = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    return null;
                }
                LeaderboardType lbType = LeaderboardType.mapFromString(type);
                if (lbType == null) return null;

                LeaderBoardCacheService cache = LeaderBoardManager.get().getCacheService();

                Leaderboard lb = cache.getLeaderBoard(lbType);

                if (lb == null) return null;

                Leaderboard.Entry data = lb.getPosition(position);

                if (data == null) return "&cN/A";

                return data.getPlayerName();
            case "deaths":
                return String.valueOf(dao.getPlayerCache().get(player.getUniqueId()).getDeaths());
            case "kills":
                return String.valueOf(dao.getPlayerCache().get(player.getUniqueId()).getKills());
            case "kd":
                PlayerStatsDto cachedDto = dao.getPlayerCache().get(player.getUniqueId());
                double kd = (cachedDto.getDeaths() == 0) ? cachedDto.getKills() : (double) cachedDto.getKills() / cachedDto.getDeaths();
                return String.format("%.1f", kd);
            case "stage":
                return String.valueOf(GunGame.getInstance().getLevelingService().get(player.getUniqueId()).getLevel());
            case "killstreak":
                return String.valueOf(dao.getPlayerCache().get(player.getUniqueId()).getKillStreak());
            case "coins":
                if (GunGame.getEcon() == null) return null;
                return String.format("%.1f", GunGame.getEcon().getBalance(player));
            default:
                return null;
        }
    }
}
