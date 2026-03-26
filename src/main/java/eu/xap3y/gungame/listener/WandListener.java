package eu.xap3y.gungame.listener;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.adapter.PaperAdapter;
import eu.xap3y.gungame.api.SamePair;
import eu.xap3y.gungame.util.ConfigDb;
import eu.xap3y.gungame.util.Utils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class WandListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || !event.getItem().hasItemMeta()) {
            return;
        }
        else if (!Utils.isWand(event.getItem())) {
            return;
        }
        else if (event.getClickedBlock() == null) {
            return;
        }
        Action action = event.getAction();
        event.setCancelled(true);
        if (action == Action.RIGHT_CLICK_BLOCK) {
            if (ConfigDb.POS_CACHE.containsKey(event.getPlayer().getUniqueId())) {
                ConfigDb.POS_CACHE.get(event.getPlayer().getUniqueId()).setFirst(event.getClickedBlock().getLocation());
            } else {
                ConfigDb.POS_CACHE.put(event.getPlayer().getUniqueId(), SamePair.ofSame(event.getClickedBlock().getLocation(), null));
            }

            GunGame.getTexter().response(event.getPlayer(), "&aPos 1 set");
        } else if (action == Action.LEFT_CLICK_BLOCK) {
            if (ConfigDb.POS_CACHE.containsKey(event.getPlayer().getUniqueId())) {
                ConfigDb.POS_CACHE.get(event.getPlayer().getUniqueId()).setSecond(event.getClickedBlock().getLocation());
            } else {
                ConfigDb.POS_CACHE.put(event.getPlayer().getUniqueId(), SamePair.of(null, event.getClickedBlock().getLocation()));
            }
            GunGame.getTexter().response(event.getPlayer(), "&aPos 2 set");
        }

        if (!GunGame.getInstance().isUseComponents()) return; // Spigot/Bukkit (nema adventure api)

        // Posle click buttony pokud jsou nastaveny obě pozice
        if ((action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) && (ConfigDb.POS_CACHE.containsKey(event.getPlayer().getUniqueId()) && ConfigDb.POS_CACHE.get(event.getPlayer().getUniqueId()).isComplete())) {
            PaperAdapter.sendPosButtons(event.getPlayer());
        }
    }
}
