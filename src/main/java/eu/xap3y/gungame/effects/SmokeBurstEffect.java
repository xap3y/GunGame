package eu.xap3y.gungame.effects;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.enums.KillEffectType;
import eu.xap3y.gungame.api.iface.KillEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;

public class SmokeBurstEffect implements KillEffect {

    @Override
    public String getName() {
        return "Smoke Burst";
    }

    @Override
    public KillEffectType getType() {
        return KillEffectType.SMOKE_BURST;
    }

    @Override
    public void playEffect(Location loc) {
        Bukkit.getScheduler().runTask(GunGame.getInstance(), () -> {
            Collection<Player> nearbyPlayers = loc.getNearbyPlayers(30);

            Location center = loc.clone().add(0, 1, 0);

            // Spawn several particles with slight offsets for a "burst" effect
            for (int i = 0; i < 15; i++) {
                GunGame.getInstance().getParApi().LIST_1_8.SMOKE_NORMAL
                        .packetMotion(true, center, 0.2, 0.2, 0.2)
                        .sendTo(nearbyPlayers);
            }
        });
    }
}