package eu.xap3y.gungame.util;

import eu.xap3y.gungame.GunGame;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.List;
import java.util.Set;


public final class UpgradeUtil {
    private UpgradeUtil() {}

    public static void process(Player p0) {

        int level = GunGame.getInstance().getLevelingService().get(p0.getUniqueId()).getLevel();
        Set<Material> managedTypes = GunGame.getInstance().getProgression().managedMaterials();
        PlayerInventory inv = p0.getInventory();
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack it = contents[i];
            if (it != null && managedTypes.contains(it.getType())) {
                inv.setItem(i, null);
            }
        }

        if (inv.getHelmet() != null && managedTypes.contains(inv.getHelmet().getType())) inv.setHelmet(null);
        if (inv.getChestplate() != null && managedTypes.contains(inv.getChestplate().getType())) inv.setChestplate(null);
        if (inv.getLeggings() != null && managedTypes.contains(inv.getLeggings().getType())) inv.setLeggings(null);
        if (inv.getBoots() != null && managedTypes.contains(inv.getBoots().getType())) inv.setBoots(null);

        List<ItemStack> tier = GunGame.getInstance().getProgression().itemsForLevel(level);

        for (ItemStack base : tier) {
            ItemStack stack = base.clone();
            //ItemMeta meta = stack.getItemMeta();
            /*if (meta instanceof Damageable dmg) {
                dmg.setUnbreakable(true);
                stack.setItemMeta(dmg);
            }*/
            Material type = stack.getType();

            String name = type.name();
            if (name.endsWith("_HELMET")) {
                inv.setHelmet(stack);
            } else if (name.endsWith("_CHESTPLATE")) {
                inv.setChestplate(stack);
            } else if (name.endsWith("_LEGGINGS")) {
                inv.setLeggings(stack);
            } else if (name.endsWith("_BOOTS")) {
                inv.setBoots(stack);
            } else {
                inv.setItem(0, stack);
            }
        }

        p0.updateInventory();
    }
}