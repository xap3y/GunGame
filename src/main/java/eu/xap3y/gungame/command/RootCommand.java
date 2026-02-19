package eu.xap3y.gungame.command;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.manager.ConfigManager;
import eu.xap3y.gungame.util.ConfigDb;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

public class RootCommand {

    @Command(ConfigDb.COMMAND_BASE + " version")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "version"}, mode = Permission.Mode.ANY_OF)
    public void versionCommand(
            CommandSender ctx
    ) {
        GunGame.getTexter().response(ctx, "&fRunning GunGame v" + ConfigDb.VERSION + " &8(" + ConfigDb.GIT_HASH + ")");
    }

    @Command(ConfigDb.COMMAND_BASE + " reload")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "reload"}, mode = Permission.Mode.ANY_OF)
    public void reloadCommand(
            CommandSender ctx
    ) {
        ConfigManager.reloadConfig();
    }

    @Command(ConfigDb.COMMAND_BASE + " reload arenas")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "reload"}, mode = Permission.Mode.ANY_OF)
    public void reloadArenasCommand(
            CommandSender ctx
    ) {
        GunGame.getInstance().getArenaLoader().reloadArenas(); // just refresh YAML
        GunGame.getInstance().getArenaLoader().refreshArenaPool(); // scrape arenas from YAML and put them in the pool
        GunGame.getTexter().response(ctx, "&aArenas reloaded! &7(&e" + GunGame.getInstance().getArenaLoader().getArenaPool().size() + "&7)");
    }

    @Command(ConfigDb.COMMAND_BASE + " reload scheduler")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "reload"}, mode = Permission.Mode.ANY_OF)
    public void reloadArenaSchedulerCommand(
            CommandSender ctx
    ) {
        GunGame.getInstance().getArenaManager().restartArenaRotationTask();
        GunGame.getTexter().response(ctx, "&aArena rotation scheduler restarted!");
    }

    @Command(ConfigDb.COMMAND_BASE + " gui")
    public void guiCommand(
            CommandSender ctx
    ) {
        if (ctx instanceof Player player) {
            eu.xap3y.gungame.api.gui.MainGui mainGui = new eu.xap3y.gungame.api.gui.MainGui();
            mainGui.build("").open(player);
        } else {
            GunGame.getTexter().response(ctx, "&cThis command can be only used by players!");
        }
    }

    @Command(ConfigDb.COMMAND_BASE + " exit")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "exit"}, mode = Permission.Mode.ANY_OF)
    public void exitCommand(
            CommandSender ctx
    ) {
        if (!(ctx instanceof org.bukkit.entity.Player player)) {
            GunGame.getTexter().responseLang(ctx, "no-player");
            return;
        }

        boolean playing = GunGame.getInstance().getArenaManager().isPlayerInArena(player.getUniqueId());

        if (!playing) {
            GunGame.getTexter().responseLang(ctx, "not-in-arena");
            return;
        }

        GunGame.getInstance().getArenaManager().removePlayer(player.getUniqueId());
        GunGame.getTexter().responseLang(ctx, "arena-left");
    }
}
