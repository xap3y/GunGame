package eu.xap3y.gungame.effects;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.enums.KillEffectType;
import eu.xap3y.gungame.api.iface.KillEffect;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;

public class BloodKillEffect implements KillEffect {

    @Override
    public String getName() {
        return "Blood Splash";
    }

    @Override
    public KillEffectType getType() {
        return KillEffectType.BLOOD;
    }

    @Override
    public void playEffect(Location loc) {
        Bukkit.getScheduler().runTask(GunGame.getInstance(), () -> {
            Collection<Player> nearbyPlayers = loc.getNearbyPlayers(30);

            Location spawnLoc = loc.clone().add(0, 1, 0);

            // We use BLOCK_CRACK with Redstone Block for a "bloody" look
            // packetMotion(bool, loc, xOff, yOff, zOff)
            GunGame.getTexter().console("Spawning blood effect at " + spawnLoc + " for " + nearbyPlayers.size() + " nearby players.");
            for (int i = 0; i < 10; i++) {
                GunGame.getInstance().getParApi().LIST_1_8.BLOCK_CRACK
                        .of(Material.REDSTONE_BLOCK)
                        .packetMotion(true, spawnLoc, 0.2, 0.2, 0.2)
                        .sendTo(nearbyPlayers);
            }
        });
    }
}
