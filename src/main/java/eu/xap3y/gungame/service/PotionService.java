package eu.xap3y.gungame.service;

import eu.xap3y.gungame.GunGame;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PotionService {

    private static final PotionService INSTANCE = new PotionService();

    public static PotionService getInstance() {
        return INSTANCE;
    }

    private final Map<UUID, Map<PotionEffect, Long>> activeEffects = new ConcurrentHashMap<>();

    public void addEffect(Player player, PotionEffect effect, long durationInSeconds) {
        long expiryTime = System.currentTimeMillis() + (durationInSeconds * 1000);

        activeEffects.computeIfAbsent(player.getUniqueId(), k -> new ConcurrentHashMap<>())
                .put(effect, expiryTime);

        applyEffectToEntity(player, effect);
    }

    public void refresh(Player player) {
        Map<PotionEffect, Long> effects = activeEffects.get(player.getUniqueId());

        if (effects == null || effects.isEmpty()) return;

        long now = System.currentTimeMillis();

        effects.forEach((effect, expiry) -> {
            if (expiry > now) {
                applyEffectToEntity(player, effect);
            } else {
                // Clean up expired effects lazily
                effects.remove(effect);
            }
        });
    }

    public boolean hasEffect(PotionEffectType type, UUID player) {
        Map<PotionEffect, Long> effects = activeEffects.get(player);
        if (effects == null) return false;
        return effects.keySet().stream().anyMatch(effect -> effect.getType().equals(type));
    }

    private void applyEffectToEntity(Player player, PotionEffect effect) {
        int duration = (int) ((activeEffects.get(player.getUniqueId()).get(effect) - System.currentTimeMillis()) / 1000);
        GunGame.getTexter().console("Applying effect " + effect.getType().getName() + " to player " + player.getName() + " with duration " + duration + " seconds.");
        if (duration > 0) {
            player.addPotionEffect(new PotionEffect(effect.getType(), duration * 20, effect.getAmplifier(), effect.isAmbient(), effect.hasParticles()));
        } else {
            activeEffects.get(player.getUniqueId()).remove(effect);
        }
    }

    public void clearEffects(Player player) {
        activeEffects.remove(player.getUniqueId());
    }
}
