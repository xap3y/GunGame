package eu.xap3y.gungame.service;

import com.cryptomorin.xseries.XPotion;
import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.database.dao.PlayerDao;
import eu.xap3y.gungame.database.dto.PlayerStatsDto;
import eu.xap3y.gungame.model.LevelProgress;
import eu.xap3y.gungame.model.Progression;
import org.bukkit.Bukkit;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

public class LevelingService {
    private final ConcurrentMap<UUID, LevelProgress> progress = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Integer> killStreak = new ConcurrentHashMap<>();
    private final Progression progression;

    public LevelingService(Progression progression) {
        GunGame.getTexter().logPos();
        this.progression = progression;
    }

    public LevelProgress get(UUID uuid) {
        return progress.computeIfAbsent(uuid, id -> new LevelProgress(0));
    }

    public int getKillstreak(UUID p0) {
        return killStreak.getOrDefault(p0, 0);
    }

    public boolean addKill(UUID uuid) {
        LevelProgress p = get(uuid);
        int before = p.getLevel();
        if (before < progression.maxIndex()) {
            p.increment();
        }
        if (killStreak.containsKey(uuid)) {
            killStreak.replace(uuid, killStreak.get(uuid)+1);
        } else {
            killStreak.put(uuid, 1);
        }
        int coins = ThreadLocalRandom.current().nextInt(3, 6); // 3-5 coins per kill
        int xp = ThreadLocalRandom.current().nextInt(6, 12); // 6-11 xp per kill
        PlayerDao dao = GunGame.getInstance().getDatabaseManager().getPlayerDao();
        dao.addCacheKill(uuid);
        dao.addCacheCoins(uuid, coins);
        dao.addCacheXp(uuid, xp);
        if (dao.getPlayerCache().get(uuid) != null) {
            PlayerStatsDto stats = dao.getPlayerCache().get(uuid);
            int best = stats.getBestStage();
            int current = p.getLevel();
            if (current > best) {
                dao.addCacheBestStage(uuid, current);
            }
            if (stats.getBestKillStreak() < killStreak.get(uuid)) {
                dao.addCacheBestKillStreak(uuid, killStreak.get(uuid));
            }
        }
        if (!GunGame.getEcon().depositPlayer(Bukkit.getOfflinePlayer(uuid), coins).transactionSuccess()) {
            GunGame.getTexter().console("&cFailed to deposit coins for " + uuid + ": amount: " + coins);
        };
        GunGame.getBoardApi().updateBoard(uuid);
        dao.updateFromCache(uuid)
                .doOnError((err) -> {
                    GunGame.getTexter().console("&cFailed to update player data for " + uuid + ": " + err.getMessage());
                })
                .subscribe();

        return p.getLevel() != before;
    }

    public boolean addDeath(UUID uuid) {
        LevelProgress p = get(uuid);
        int before = p.getLevel();
        int amountToDecrement = 1;
        if (killStreak.containsKey(uuid)) {
            int streak = killStreak.get(uuid);
            if (streak >= 5) {
                amountToDecrement += 1;
            }
        }
        if (before > 10 && before <= 20) {
            amountToDecrement += 1;
        } else if (before > 20) {
            int rand = ThreadLocalRandom.current().nextInt(1, 101); // 1-100
            if (rand <= 60) {
                amountToDecrement += 2;
            } else if (rand <= 97) {
                amountToDecrement += 3;
            } else {
                amountToDecrement += 4;
            }
        }
        p.decrement(amountToDecrement);
        killStreak.remove(uuid);
        GunGame.getInstance().getDatabaseManager().getPlayerDao().addCacheDeath(uuid);
        GunGame.getBoardApi().updateBoard(uuid);
        GunGame.getInstance().getDatabaseManager().getPlayerDao().updateFromCache(uuid)
                .doOnError((err) -> {
                    GunGame.getTexter().console("&cFailed to update player st for " + uuid + ": " + err.getMessage());
                })
                .subscribe();
        return p.getLevel() != before;
    }

    public void setLevel(UUID uuid, int level) {
        LevelProgress p = get(uuid);
        p.setLevel(level);
        GunGame.getBoardApi().updateBoard(uuid);
        GunGame.getInstance().getDatabaseManager().getPlayerDao().addCacheStage(uuid, level);
        GunGame.getInstance().getDatabaseManager().getPlayerDao().updateFromCache(uuid)
                .doOnError((err) -> {
                    GunGame.getTexter().console("&cFailed to update player st for " + uuid + ": " + err.getMessage());
                })
                .subscribe();
    }

    public void reset(UUID uuid) {
        progress.remove(uuid);
        killStreak.remove(uuid);
    }
}