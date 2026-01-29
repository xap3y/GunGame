package eu.xap3y.gungame.command;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.util.ConfigDb;
import eu.xap3y.gungame.util.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class SetupCommand {

    @Command(ConfigDb.COMMAND_BASE + " setfallbackpos")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "setup"}, mode = Permission.Mode.ANY_OF)
    public void setUpFallBackPos(
            CommandSender ctx
    ) {
        if (!(ctx instanceof org.bukkit.entity.Player player)) {
            GunGame.getInstance().getTexter().responseLang(ctx, "no-player");
            return;
        }

        Location loc = player.getLocation();
        String compiled = Utils.decodeLocation(loc);
        GunGame.getInstance().getConfig().set("fallback-location", compiled);
        GunGame.getInstance().saveConfig();
        GunGame.getInstance().getTexter().responseLang(ctx, "setup-fallback-pos-set");
    }

    @Command(ConfigDb.COMMAND_BASE + " create [name]")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "create"}, mode = Permission.Mode.ANY_OF)
    public void createMap(
            CommandSender ctx
    ) {
        if (!(ctx instanceof org.bukkit.entity.Player player)) {
            GunGame.getInstance().getTexter().responseLang(ctx, "no-player");
            return;
        }


    }
}
