package eu.xap3y.gungame.listener;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.service.Texter;
import eu.xap3y.gungame.util.ActionBar;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunchListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile entity = event.getEntity();

        if (entity.getShooter() instanceof Player victim) {
            if (
                    GunGame.getInstance().getArenaManager().isPlayerInArena(victim.getUniqueId())
                            && GunGame.getInstance().getArenaManager().getCurrentArena().isInSafeZone(victim.getLocation())
            ) {
                event.setCancelled(true);
                ActionBar.sendActionbar(victim, Texter.colored("&cYou are in safe zone!"));
            }
        }
    }
}
