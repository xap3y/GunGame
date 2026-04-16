package eu.xap3y.gungame.listener;

import eu.xap3y.gungame.GunGame;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EntityDamageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityHit(EntityDamageByEntityEvent e) {
        if (e.getEntity().getType() == EntityType.PLAYER) {
            Player victim = (Player) e.getEntity();

            if (
                    GunGame.getInstance().getArenaManager().isPlayerInArena(victim.getUniqueId())
                    && GunGame.getInstance().getArenaManager().getCurrentArena().isInSafeZone(victim.getLocation())
            ) {
                e.setCancelled(true);
            }
        }
    }
}
