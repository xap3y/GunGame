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

public class SetupCommand {

    @Command(ConfigDb.COMMAND_BASE + " setfallbackpos")
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

    @Command(ConfigDb.COMMAND_BASE + " create [name]")
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
}
