package eu.xap3y.gungame.util;

import com.cryptomorin.xseries.XItemStack;
import com.cryptomorin.xseries.XMaterial;
import eu.xap3y.xagui.models.GuiButton;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Utils {

    public static String getMcVersion() {
        String version = "";
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        } catch (ArrayIndexOutOfBoundsException e) {
            // 1.20.6+
        }
        return version;
    }

    public static boolean isPaper() {
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static ItemStack createWand() {
        GuiButton b0 = new GuiButton(XMaterial.BLAZE_ROD.get())
                .setName("&6&lGunGame Wand")
                .addLore("&r", "&7Right click -> pos1", "&7Left click -> pos2");
        return b0.getItem();
    }

    public static boolean isWand(@NotNull ItemStack item) {
        if (item.getType() != XMaterial.BLAZE_ROD.parseMaterial()) {
            return false;
        }
        if (!item.getItemMeta().hasDisplayName()) {
            return false;
        }
        String displayName = item.getItemMeta().getDisplayName();
        return displayName.contains("GunGame Wand");
    }

    @NotNull
    public static String decodeLocation(@NotNull Location loc) {
        return loc.getWorld().getName() + ";" +
                loc.getX() + ";" +
                loc.getY() + ";" +
                loc.getZ() + ";" +
                loc.getYaw() + ";" +
                loc.getPitch();
    }

    public static Enchantment getLegacyEnchantmentByName(@NotNull String name, @NotNull String fallBack) {
        Enchantment enchantment = Enchantment.getByName(name);
        if (enchantment == null) {
            enchantment = Enchantment.getByName(fallBack);
        }
        return enchantment;
    }

    @NotNull
    public static Location encodeLocation(@NotNull String str) {
        String[] parts = str.split(";");
        if (parts.length != 6) {
            throw new IllegalArgumentException("Invalid location string: " + str);
        }
        String worldName = parts[0];
        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);

        return new Location(
                Bukkit.getWorld(worldName),
                x,
                y,
                z,
                yaw,
                pitch
        );
    }

    // Time like 1m 30s or 45s AND also 2m instead of 2m 0s
    public static String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        StringBuilder sb = new StringBuilder();
        if (minutes > 0) {
            sb.append(minutes).append("m");
        }
        if (remainingSeconds > 0 || minutes == 0) {
            if (!sb.isEmpty()) {
                sb.append(" ");
            }
            sb.append(remainingSeconds).append("s");
        }
        return sb.toString();
    }
}
