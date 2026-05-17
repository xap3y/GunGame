package eu.xap3y.gungame.command;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.model.Arena;
import eu.xap3y.gungame.util.ConfigDb;
import eu.xap3y.gungame.util.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.util.Map;
import java.util.Optional;

public class SetupCommand {

    @Command(ConfigDb.COMMAND_BASE + " setup set-fallbackpos")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "setup"}, mode = Permission.Mode.ANY_OF)
    public void setUpFallBackPos(
            CommandSender ctx
    ) {
        if (!(ctx instanceof org.bukkit.entity.Player player)) {
            GunGame.getTexter().responseLang(ctx, "no-player");
            return;
        }

        Location loc = player.getLocation();
        String compiled = Utils.decodeLocation(loc);
        GunGame.getInstance().getConfig().set("fallback-location", compiled);
        GunGame.getInstance().saveConfig();
        GunGame.getTexter().responseLang(ctx, "setup-fallback-pos-set");
    }

    @Command(ConfigDb.COMMAND_BASE + " create <name>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "create"}, mode = Permission.Mode.ANY_OF)
    public void createMap(
            @Argument("name") String name,
            CommandSender ctx
    ) {
        if (!(ctx instanceof Player player)) {
            GunGame.getTexter().responseLang(ctx, "no-player");
            return;
        }
        else if (GunGame.getInstance().getArenaLoader().arenaExists(name)) {
            GunGame.getTexter().responseLang(ctx, "arena-exists", Map.of("arena", name));
            return;
        }

        Location loc = player.getLocation();
        Arena arena = new Arena(name, loc);
        GunGame.getInstance().getArenaLoader().saveArena(arena);
        GunGame.getTexter().responseLang(ctx, "arena-created", Map.of("arena", name));
    }

    @Command(ConfigDb.COMMAND_BASE + " setup set-spawn <name>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "setup"}, mode = Permission.Mode.ANY_OF)
    public void arenaSetSpawn(
            @Argument(value = "name", suggestions = "maps") String name,
            CommandSender ctx
    ) {
        if (!(ctx instanceof Player player)) {
            GunGame.getTexter().responseLang(ctx, "no-player");
            return;
        }
        else if (!GunGame.getInstance().getArenaLoader().arenaExists(name)) {
            GunGame.getTexter().responseLang(ctx, "arena-not-exists", Map.of("arena", name));
            return;
        }

        Location loc = player.getLocation();
        GunGame.getInstance().getArenaLoader().setArenaSpawn(name, loc);
        Arena current = GunGame.getInstance().getArenaManager().getCurrentArena();
        if (current != null && current.getArenaName().equals(name)) {
            current.setSpawn(loc);
        }
        GunGame.getTexter().responseLang(ctx, "arena-spawn-set", Map.of("arena", name));
    }

    @Command(ConfigDb.COMMAND_BASE + " setup set-name <name> <displayName>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "setup"}, mode = Permission.Mode.ANY_OF)
    public void arenaSetSpawn(
            @Argument(value = "name", suggestions = "maps") String name,
            @Argument(value = "displayName") String[] displayName,
            CommandSender ctx
    ) {
        if (!GunGame.getInstance().getArenaLoader().arenaExists(name)) {
            GunGame.getTexter().responseLang(ctx, "arena-not-exists", Map.of("arena", name));
            return;
        }

        String joinedDisplayName = String.join(" ", displayName);
        GunGame.getInstance().getArenaLoader().setArenaDisplayName(name, joinedDisplayName);
        GunGame.getTexter().responseLang(ctx, "arena-name-set", Map.of("arena", name));
    }

    @Command(ConfigDb.COMMAND_BASE + " setup enable <name>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "setup"}, mode = Permission.Mode.ANY_OF)
    public void arenaEnable(
            @Argument(value = "name", suggestions = "maps-disabled") String name,
            CommandSender ctx
    ) {
        if (!(ctx instanceof Player player)) {
            GunGame.getTexter().responseLang(ctx, "no-player");
            return;
        }
        Optional<Arena> arenaOpt = GunGame.getInstance().getArenaLoader().loadArena(name);
        if (arenaOpt.isEmpty()) {
            GunGame.getTexter().responseLang(ctx, "arena-not-exists", Map.of("arena", name));
            return;
        }
        Arena arena = arenaOpt.get();
        if (arena.isEnabled()) {
            GunGame.getTexter().responseLang(ctx, "arena-already-enabled", Map.of("arena", name));
            return;
        } else if (!arena.isComplete()) {
            GunGame.getTexter().responseLang(ctx, "arena-not-complete", Map.of("arena", name));
            return;
        }

        GunGame.getInstance().getArenaLoader().enableArena(arena.getArenaName());
        GunGame.getInstance().getArenaLoader().addArenaToPool(arena);
        GunGame.getTexter().responseLang(ctx, "arena-enabled", Map.of("arena", name));
    }

    @Command(ConfigDb.COMMAND_BASE + " wand")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "setup"}, mode = Permission.Mode.ANY_OF)
    public void setupWand(
            CommandSender ctx
    ) {
        if (!(ctx instanceof Player player)) {
            GunGame.getTexter().responseLang(ctx, "no-player");
            return;
        }

        player.getInventory().addItem(Utils.createWand());
        GunGame.getTexter().responseLang(ctx, "wand-given", "&6&oUse the &e&oWand&6&o to select two positions for arena safe zone.");
    }

    @Command(ConfigDb.COMMAND_BASE + " setup set-safezone <name>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "setup"}, mode = Permission.Mode.ANY_OF)
    public void arenaSetSafeZone(
            @Argument(value = "name", suggestions = "maps") String name,
            CommandSender ctx
    ) {
        if (!(ctx instanceof Player p0)) {
            GunGame.getTexter().responseLang(ctx, "no-player");
            return;
        }

        if (ConfigDb.POS_CACHE.get(p0.getUniqueId()) == null || !ConfigDb.POS_CACHE.get(p0.getUniqueId()).isComplete()) {
            GunGame.getTexter().responseLang(ctx, "wand-no-positions");
            return;
        }

        if (!GunGame.getInstance().getArenaLoader().arenaExists(name)) {
            GunGame.getTexter().responseLang(ctx, "arena-not-exists", Map.of("arena", name));
            return;
        }

        GunGame.getInstance().getArenaLoader().saveArenaSafeSpot(name, ConfigDb.POS_CACHE.get(p0.getUniqueId()));
        ConfigDb.POS_CACHE.remove(p0.getUniqueId());
    }

    @Command(ConfigDb.COMMAND_BASE + " setup set-dimension <name>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "setup"}, mode = Permission.Mode.ANY_OF)
    public void arenaSetDimension(
            @Argument(value = "name", suggestions = "maps") String name,
            CommandSender ctx
    ) {
        if (!(ctx instanceof Player p0)) {
            GunGame.getTexter().responseLang(ctx, "no-player");
            return;
        }

        if (!GunGame.getInstance().getArenaLoader().arenaExists(name)) {
            GunGame.getTexter().responseLang(ctx, "arena-not-exists", Map.of("arena", name));
            return;
        }

        GunGame.getInstance().getArenaLoader().saveArenaDimension(name, ConfigDb.POS_CACHE.get(p0.getUniqueId()));
        ConfigDb.POS_CACHE.remove(p0.getUniqueId());
    }
}
