package eu.xap3y.gungame.listener;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.adapter.PaperAdapter;
import eu.xap3y.gungame.api.Pair;
import eu.xap3y.gungame.api.SamePair;
import eu.xap3y.gungame.util.ConfigDb;
import eu.xap3y.gungame.util.Utils;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
        event.setCancelled(true);
        if (event.getAction().isRightClick()) {
            if (ConfigDb.POS_CACHE.containsKey(event.getPlayer().getUniqueId())) {
                ConfigDb.POS_CACHE.get(event.getPlayer().getUniqueId()).setFirst(event.getClickedBlock().getLocation());
            } else {
                ConfigDb.POS_CACHE.put(event.getPlayer().getUniqueId(), SamePair.ofSame(event.getClickedBlock().getLocation(), null));
            }

            GunGame.getTexter().response(event.getPlayer(), "&aPos 1 set");
        } else if (event.getAction().isLeftClick()) {
            if (ConfigDb.POS_CACHE.containsKey(event.getPlayer().getUniqueId())) {
                ConfigDb.POS_CACHE.get(event.getPlayer().getUniqueId()).setSecond(event.getClickedBlock().getLocation());
            } else {
                ConfigDb.POS_CACHE.put(event.getPlayer().getUniqueId(), SamePair.of(null, event.getClickedBlock().getLocation()));
            }
            GunGame.getTexter().response(event.getPlayer(), "&aPos 2 set");
        }

        if (!GunGame.getInstance().isUseComponents()) return; // Spigot/Bukkit (nema adventure api)

        // Posle click buttony pokud jsou nastaveny obě pozice
        if ((event.getAction().isRightClick() || event.getAction().isLeftClick()) && (ConfigDb.POS_CACHE.containsKey(event.getPlayer().getUniqueId()) && ConfigDb.POS_CACHE.get(event.getPlayer().getUniqueId()).isComplete())) {
            PaperAdapter.sendPosButtons(event.getPlayer());
        }
    }
}
