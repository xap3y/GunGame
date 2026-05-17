package eu.xap3y.gungame.command;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.enums.LeaderboardType;
import eu.xap3y.gungame.manager.LeaderBoardManager;
import eu.xap3y.gungame.model.Leaderboard;
import eu.xap3y.gungame.util.ConfigDb;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.time.Duration;

public class DebugCommand {

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "arenaManager cancelArenaRotation")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void cancelArenaRotation(
            CommandSender ctx
    ) {
        GunGame.getInstance().getArenaManager().cancelArenaRotation();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "arenaManager clearLastDamagers")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void clearLastDamagers(
            CommandSender ctx
    ) {
        GunGame.getInstance().getArenaManager().clearLastDamagers();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "arenaManager resetArena")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void resetArena(
            CommandSender ctx
    ) {
        GunGame.getInstance().getArenaManager().resetArena();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "arenaManager rotateArena")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void rotateArena(
            CommandSender ctx
    ) {
        GunGame.getInstance().getArenaManager().rotateArena();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "arenaManager rotateArenaPre")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void rotateArenaPre(
            CommandSender ctx
    ) {
        GunGame.getInstance().getArenaManager().rotateArenaPre();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "arenaManager teleportAllSpawn")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void teleportAllSpawn(
            CommandSender ctx
    ) {
        GunGame.getInstance().getArenaManager().teleportAllSpawn();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "arenaLoader loadAllArenas")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void loadAllArenas(
            CommandSender ctx
    ) {
        GunGame.getInstance().getArenaLoader().loadAllArenas();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "arenaLoader refreshArenaPool")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void refreshArenaPool(
            CommandSender ctx
    ) {
        GunGame.getInstance().getArenaLoader().refreshArenaPool();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "arenaLoader rotateArena")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void arenaLoaderRotateArena(
            CommandSender ctx
    ) {
        GunGame.getInstance().getArenaLoader().rotateArena();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "arenaLoader saveArenas")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void saveArenas(
            CommandSender ctx
    ) {
        GunGame.getInstance().getArenaLoader().saveArenas();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "arenaLoader reloadArenas")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void reloadArenas(
            CommandSender ctx
    ) {
        GunGame.getInstance().getArenaLoader().reloadArenas();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "boardApi loadConfig")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void boardApiLoadConfig(
            CommandSender ctx
    ) {
        GunGame.getBoardApi().loadConfig();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "boardApi reloadAllBoards")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void reloadAllBoards(
            CommandSender ctx
    ) {
        GunGame.getBoardApi().reloadAllBoards();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "boardApi updateAllBoardTimes")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void updateAllBoardTimes(
            CommandSender ctx
    ) {
        GunGame.getBoardApi().updateAllBoardTimes();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "database connect")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void dbConnect(
            CommandSender ctx
    ) {
        GunGame.getInstance().getDatabaseManager().connect();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "database playerDao updateFromCache#subscribe <p0>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void updateFromCache(
            CommandSender ctx,
            @Argument("p0") Player p0
    ) {
        GunGame.getInstance().getDatabaseManager().getPlayerDao().updateFromCache(p0.getUniqueId()).subscribe();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "database playerDao updateFromCache#block <p0>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void updateFromCacheBlock(
            CommandSender ctx,
            @Argument("p0") Player p0
    ) {
        GunGame.getInstance().getDatabaseManager().getPlayerDao().updateFromCache(p0.getUniqueId()).block(Duration.ofSeconds(10));
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "database playerDao updateUpgradesFromCache#subscribe <p0>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void updateUpgradesFromCacheSubscribe(
            CommandSender ctx,
            @Argument("p0") Player p0
    ) {
        GunGame.getInstance().getDatabaseManager().getPlayerDao().updateUpgradesFromCache(p0.getUniqueId()).subscribe();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "database playerDao updateUpgradesFromCache#block <p0>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void updateUpgradesFromCacheBlock(
            CommandSender ctx,
            @Argument("p0") Player p0
    ) {
        GunGame.getInstance().getDatabaseManager().getPlayerDao().updateUpgradesFromCache(p0.getUniqueId()).block(Duration.ofSeconds(10));
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "database playerDao loadUpgradesToCache#subscribe <p0>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void loadUpgradesToCacheSubscribe(
            CommandSender ctx,
            @Argument("p0") Player p0
    ) {
        GunGame.getInstance().getDatabaseManager().getPlayerDao().loadUpgradesToCache(p0.getUniqueId()).subscribe();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "database playerDao loadUpgradesToCache#block <p0>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void loadUpgradesToCacheBlock(
            CommandSender ctx,
            @Argument("p0") Player p0
    ) {
        GunGame.getInstance().getDatabaseManager().getPlayerDao().loadUpgradesToCache(p0.getUniqueId()).block(Duration.ofSeconds(10));
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "database playerDao loadOrCreate#subscribe <p0>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void loadOrCreateSubscribe(
            CommandSender ctx,
            @Argument("p0") Player p0
    ) {
        GunGame.getInstance().getDatabaseManager().getPlayerDao().loadOrCreate(p0).subscribe();
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "database playerDao loadOrCreate#block <p0>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void loadOrCreateBlock(
            CommandSender ctx,
            @Argument("p0") Player p0
    ) {
        GunGame.getInstance().getDatabaseManager().getPlayerDao().loadOrCreate(p0).block(Duration.ofSeconds(10));
    }

    @Command(ConfigDb.COMMAND_BASE_DEBUG + "leaderboard display")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "debug"}, mode = Permission.Mode.ANY_OF)
    public void displayLeaderBoard(
            CommandSender ctx
    ) {
        for (LeaderboardType type : LeaderboardType.values()) {

            GunGame.getTexter().response(ctx, "&7&m-------------------------");
            Leaderboard lb = LeaderBoardManager.get().getLeaderBoard(type);

            GunGame.getTexter().response(ctx, "&e" + type.getDisplayName() + ":");

            lb.getEntries()
                    .forEach(entry -> {
                        String playerName = entry.getPlayerName();
                        int value = entry.getScore();
                        GunGame.getTexter().response(ctx, "&7" + playerName + ": &f" + value);
                    });
        }

        GunGame.getTexter().response(ctx, "STAGE LEADERBOARD:");
        GunGame.getInstance().getLevelingService().getLeaderBoard(ConfigDb.MAX_LB_ENTRIES)
                .getEntries()
                .forEach(entry -> {
                    String playerName = entry.getPlayerName();
                    int value = entry.getScore();
                    GunGame.getTexter().response(ctx, "&7" + playerName + ": &f" + value);
                });

        GunGame.getTexter().response(ctx, "K/D LEADERBOARD:");
        GunGame.getInstance().getDatabaseManager().getPlayerDao().getLeaderBoardFromCache(LeaderboardType.KD, ConfigDb.MAX_LB_ENTRIES)
                .getEntries()
                .forEach(entry -> {
                    String playerName = entry.getPlayerName();
                    int value = entry.getScore();
                    GunGame.getTexter().response(ctx, "&7" + playerName + ": &f" + value);
                });
    }
}
