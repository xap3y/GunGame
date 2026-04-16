package eu.xap3y.gungame.command;

import com.cryptomorin.xseries.XSound;
import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.manager.ConfigManager;
import eu.xap3y.gungame.util.ConfigDb;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;
import org.incendo.cloud.annotations.ProxiedBy;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RootCommand {

    @ProxiedBy(ConfigDb.MAIN_COMMAND)
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
        GunGame.getBoardApi().loadConfig();
        GunGame.getInstance().getLangManager().reload();
        GunGame.getBoardApi().reloadAllBoards();

        GunGame.getTexter().responseLang(ctx, "config-reloaded", "&aConfig reloaded!");
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

    @Command(ConfigDb.COMMAND_BASE + " nextmap")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "nextmap"}, mode = Permission.Mode.ANY_OF)
    public void nextMapCommand(
            CommandSender ctx
    ) {

        LocalDateTime nextMap = GunGame.getInstance().getArenaManager().getNextArenaTime();
        if (nextMap == null) {
            GunGame.getInstance().getArenaManager().rotateArena();
        } else {
            GunGame.getInstance().getArenaManager().cancelArenaRotation();
            GunGame.getInstance().getArenaManager().rotateArenaPre();
        }

        GunGame.getTexter().responseLang(ctx, "nextmap-rotated", "&aNext map has been rotated!");
    }

    @ProxiedBy("shop")
    @Command(ConfigDb.COMMAND_BASE + " shop")
    public void shopCommand(
            CommandSender ctx
    ) {
        if (ctx instanceof Player player) {
            if (!GunGame.getInstance().getArenaManager().isPlayerInArena(player.getUniqueId())) {
                GunGame.getTexter().responseLang(ctx, "not-in-arena");
                return;
            } else if (!GunGame.getInstance().getArenaManager().getCurrentArena().isInSafeZone(player.getLocation())) {
                GunGame.getTexter().responseLang(ctx, "not-in-safezone");
                return;
            }
            XSound.BLOCK_ENDER_CHEST_OPEN.or(XSound.BLOCK_CHEST_OPEN).play(player, .7f, 1f);
            eu.xap3y.gungame.api.gui.ShopGui shopGui = new eu.xap3y.gungame.api.gui.ShopGui();
            shopGui.build("").open(player);
        } else {
            GunGame.getTexter().response(ctx, "&cThis command can be only used by players!");
        }
    }

    @Command(ConfigDb.COMMAND_BASE + " gui")
    public void guiCommand(
            CommandSender ctx
    ) {
        if (ctx instanceof Player player) {
            if (!GunGame.getInstance().getArenaManager().isPlayerInArena(player.getUniqueId())) {
                GunGame.getTexter().responseLang(ctx, "not-in-arena");
                return;
            } else if (!GunGame.getInstance().getArenaManager().getCurrentArena().isInSafeZone(player.getLocation())) {
                GunGame.getTexter().responseLang(ctx, "not-in-safezone");
                return;
            }
            eu.xap3y.gungame.api.gui.MainGui mainGui = new eu.xap3y.gungame.api.gui.MainGui();
            mainGui.build("").open(player);
        } else {
            GunGame.getTexter().response(ctx, "&cThis command can be only used by players!");
        }
    }

    @ProxiedBy("stats")
    @Command(ConfigDb.COMMAND_BASE + " stats")
    public void statsGui(
            CommandSender ctx
    ) {
        if (ctx instanceof Player player) {
            if (!GunGame.getInstance().getArenaManager().isPlayerInArena(player.getUniqueId())) {
                GunGame.getTexter().responseLang(ctx, "not-in-arena");
                return;
            } else if (!GunGame.getInstance().getArenaManager().getCurrentArena().isInSafeZone(player.getLocation())) {
                GunGame.getTexter().responseLang(ctx, "not-in-safezone");
                return;
            }
            eu.xap3y.gungame.api.gui.StatsGui statsGui = new eu.xap3y.gungame.api.gui.StatsGui();
            statsGui.build(player).open(player);
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

    @Command(ConfigDb.COMMAND_BASE + " join")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "exit"}, mode = Permission.Mode.ANY_OF)
    public void joinCommand(
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
