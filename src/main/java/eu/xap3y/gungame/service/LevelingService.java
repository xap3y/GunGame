package eu.xap3y.gungame.service;

import eu.xap3y.gungame.model.LevelProgress;
import eu.xap3y.gungame.model.Progression;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class LevelingService {
    private final ConcurrentMap<UUID, LevelProgress> progress = new ConcurrentHashMap<>();
    private final Progression progression;

    public LevelingService(Progression progression) {
        this.progression = progression;
    }

    public LevelProgress get(UUID uuid) {
        return progress.computeIfAbsent(uuid, id -> new LevelProgress(0));
    }

    public boolean addKill(UUID uuid) {
        LevelProgress p = get(uuid);
        int before = p.getLevel();
        if (before < progression.maxIndex()) {
            p.increment();
        }
        return p.getLevel() != before;
    }

    public boolean addDeath(UUID uuid) {
        LevelProgress p = get(uuid);
        int before = p.getLevel();
        p.decrement();
        return p.getLevel() != before;
    }
}