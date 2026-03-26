package eu.xap3y.gungame.listener;

import eu.xap3y.gungame.GunGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodListener implements Listener {

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        GunGame.getTexter().logPos();
        if (event.getEntity() instanceof Player p0) {
            if (GunGame.getInstance().getArenaManager().isPlayerInArena(p0.getUniqueId())) {
                event.setFoodLevel(20);
                event.setCancelled(true);
            }
        }
    }
}
