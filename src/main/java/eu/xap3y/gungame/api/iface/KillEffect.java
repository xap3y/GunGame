package eu.xap3y.gungame.api.iface;

import eu.xap3y.gungame.api.enums.KillEffectType;
import org.bukkit.Location;

public interface KillEffect {

    String getName();

    void playEffect(Location loc);

    KillEffectType getType();
}
