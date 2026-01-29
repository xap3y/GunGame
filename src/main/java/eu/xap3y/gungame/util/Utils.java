package eu.xap3y.gungame.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
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
}
