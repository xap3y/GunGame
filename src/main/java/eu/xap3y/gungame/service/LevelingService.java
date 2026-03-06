package eu.xap3y.gungame.service;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.database.dao.PlayerDao;
import eu.xap3y.gungame.model.LevelProgress;
import eu.xap3y.gungame.model.Progression;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ThreadLocalRandom;

public class LevelingService {
    private final ConcurrentMap<UUID, LevelProgress> progress = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Integer> killStreak = new ConcurrentHashMap<>();
    private final Progression progression;

    public LevelingService(Progression progression) {
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
        int coins = ThreadLocalRandom.current().nextInt(3, 6);
        int xp = ThreadLocalRandom.current().nextInt(6, 12);
        PlayerDao dao = GunGame.getInstance().getDatabaseManager().getPlayerDao();
        dao.addCacheKill(uuid);
        dao.addCacheCoins(uuid, coins);
        dao.addCacheXp(uuid, xp);
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
        p.decrement();
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

    public void reset(UUID uuid) {
        progress.remove(uuid);
        killStreak.remove(uuid);
    }
}