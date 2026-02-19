package eu.xap3y.gungame.command;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.model.Arena;
import eu.xap3y.gungame.util.ConfigDb;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.time.LocalDateTime;

public class DevCommand {

    @Command(ConfigDb.COMMAND_BASE + "-dev info")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "dev"}, mode = Permission.Mode.ANY_OF)
    public void devInfo(
            CommandSender ctx
    ) {
        LocalDateTime nextArenaTime = GunGame.getInstance().getArenaManager().getNextArenaTime();
        LocalDateTime currentArenaFrom = GunGame.getInstance().getArenaManager().getArenaRotationStartTime();
        int arenaPoolSize = GunGame.getInstance().getArenaLoader().getArenaPool().size();
        String currentArenaName = GunGame.getInstance().getArenaManager().getCurrentArena() != null ? GunGame.getInstance().getArenaManager().getCurrentArena().getArenaName() : "&cN/A";
        String arenaPool = GunGame.getInstance().getArenaLoader().getArenaPool().stream()
                .map(arena -> "&a" + arena.getArenaName())
                .reduce((a, b) -> a + "&f, " + b)
                .orElse("&cN/A");
        GunGame.getTexter().response(ctx, "&fNext arena: &a" + nextArenaTime);
        GunGame.getTexter().response(ctx, "&fCurrent arena: &a" + currentArenaName);
        GunGame.getTexter().response(ctx, "&fCurrent arena from: &a" + currentArenaFrom);
        GunGame.getTexter().response(ctx, "&fArena pool size: &a" + arenaPoolSize);
        GunGame.getTexter().response(ctx, "&fArena pool: " + arenaPool);

        Arena first = GunGame.getInstance().getArenaLoader().getArenaPool().getFirst();
        Arena last = GunGame.getInstance().getArenaLoader().getArenaPool().getLast();

        GunGame.getTexter().response(ctx, "&fFirst arena in pool: &a" + (first != null ? first.getArenaName() : "&cN/A"));
        GunGame.getTexter().response(ctx, "&fLast arena in pool: &a" + (last != null ? last.getArenaName() : "&cN/A"));
    }
}
