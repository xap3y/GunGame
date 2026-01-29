package eu.xap3y.gungame.util;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SkullCreator {

    public static @NotNull ItemStack getTexturedSkull(@NotNull String base64) {

        final ItemStack head = new ItemStack(Material.PLAYER_HEAD);

        final UUID uuid = UUID.randomUUID();
        final PlayerProfile playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
        playerProfile.setProperty(new ProfileProperty("textures", base64));

        return getSkullFromProfile(head, playerProfile);
    }

    public static @NotNull ItemStack getPlayerHead(@NotNull Player player) {
        return getSkullFromProfile(new ItemStack(Material.PLAYER_HEAD), player.getPlayerProfile());
    }

    private static @NotNull ItemStack getSkullFromProfile(@NotNull ItemStack head, PlayerProfile profile) {
        head.editMeta(SkullMeta.class, skullMeta -> {
            skullMeta.setPlayerProfile(profile);
        });

        return head;
    }
}
