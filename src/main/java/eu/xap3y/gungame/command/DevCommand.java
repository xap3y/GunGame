package eu.xap3y.gungame.command;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.enums.KillEffectType;
import eu.xap3y.gungame.api.enums.UpgradeEnum;
import eu.xap3y.gungame.database.dto.PlayerUpgradesDto;
import eu.xap3y.gungame.model.Arena;
import eu.xap3y.gungame.util.ConfigDb;
import eu.xap3y.gungame.util.UpgradeUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.incendo.cloud.annotation.specifier.Range;
import org.incendo.cloud.annotations.Argument;
import org.incendo.cloud.annotations.Command;
import org.incendo.cloud.annotations.Permission;

import java.time.LocalDateTime;

public class DevCommand {

    @Command(ConfigDb.COMMAND_BASE + " dev info")
    /*@Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "dev"}, mode = Permission.Mode.ANY_OF)*/
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

        if (!(ctx instanceof Player p0)) {
            return;
        }

        PlayerUpgradesDto dto = GunGame.getInstance().getDatabaseManager().getPlayerDao().getPlayerUpgradeCache().get(p0.getUniqueId());

        if (dto == null) {
            GunGame.getTexter().response(p0, "&cNo upgrade data found for you in cache.");
            return;
        } else {
            GunGame.getTexter().response(p0, "&fYour upgrade levels:");
            for (UpgradeEnum upgrade : UpgradeEnum.values()) {
                int level = dto.getUpgradeLevel(upgrade);
                GunGame.getTexter().response(p0, "&r  &e" + upgrade.name() + ": &6" + level);
            }
        }
    }

    @Command(ConfigDb.COMMAND_BASE + " dev safezone")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "dev"}, mode = Permission.Mode.ANY_OF)
    public void safeZoneTest(
            CommandSender ctx
    ) {
        if (ctx instanceof Player player) {
            boolean isInSafeZone = GunGame.getInstance().getArenaManager().getCurrentArena() != null &&
                    GunGame.getInstance().getArenaManager().getCurrentArena().isInSafeZone(player.getLocation());
            GunGame.getTexter().response(ctx, "&fYou are currently " + (isInSafeZone ? "&ainside" : "&coutside") + " the safe zone.");
        }
    }

    @Command(ConfigDb.COMMAND_BASE + " dev progression")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "dev"}, mode = Permission.Mode.ANY_OF)
    public void progression(
            CommandSender ctx
    ) {
        GunGame.getInstance().getProgression().getSteps().forEach(step -> {
            String items = step.stream()
                    .map(item -> "&a" + item.getType().name())
                    .reduce((a, b) -> a + "&f, " + b)
                    .orElse("&cN/A");
            GunGame.getTexter().response(ctx, "&fStep " + (GunGame.getInstance().getProgression().getSteps().indexOf(step) + 1) + ": " + items);
        });
    }

    @Command(ConfigDb.COMMAND_BASE + " dev spawn-par <particle>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "dev"}, mode = Permission.Mode.ANY_OF)
    public void spawnParticle(
            CommandSender ctx,
            @Argument("particle") KillEffectType particle
    ) {
        if (ctx instanceof Player player) {
            ConfigDb.KILL_EFFECT_MAP.get(particle).playEffect(player.getLocation());
        }
    }

    @Command(ConfigDb.COMMAND_BASE + " dev reload-sb")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "dev"}, mode = Permission.Mode.ANY_OF)
    public void reloadScoreBoards(
            CommandSender ctx
    ) {
        if (GunGame.getBoardApi() != null) {
            GunGame.getBoardApi().reloadAllBoards();
            GunGame.getTexter().response(ctx, "&aScoreboards reloaded!");
        } else {
            GunGame.getTexter().response(ctx, "&cBoard API not found!");
        }
    }

    @Command(ConfigDb.COMMAND_BASE + " dev set-stage <stage>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "dev"}, mode = Permission.Mode.ANY_OF)
    public void setStage(
            CommandSender ctx,
            @Argument("stage") @Range(min = "0", max = "50") int stage
    ) {
        if (ctx instanceof Player player) {
            GunGame.getInstance().getLevelingService().setLevel(player.getUniqueId(), stage);
            GunGame.getTexter().response(player, "&aYour stage has been set to " + stage);
            UpgradeUtil.process(player);
        }
    }

    @Command(ConfigDb.COMMAND_BASE + " dev-db get-upgrade <upgrade>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "dev"}, mode = Permission.Mode.ANY_OF)
    public void getUpgradeLevel(
            CommandSender ctx,
            @Argument("upgrade") UpgradeEnum upgradeEnum
    ) {
        if (!(ctx instanceof Player p0)) {
            return;
        }

        GunGame.getInstance().getDatabaseManager().getPlayerDao().getPlayerUpgradeLevel(p0.getUniqueId(), upgradeEnum).subscribe(level -> {
            GunGame.getTexter().response(p0, "&aYour " + upgradeEnum.name() + " level is: " + level);
        }, error -> {
            GunGame.getTexter().response(p0, "&cAn error occurred while fetching your upgrade level.");
            GunGame.getTexter().response(p0, "&4" + error.getMessage());
        });
    }

    @Command(ConfigDb.COMMAND_BASE + " dev-db set-upgrade <upgrade> <level>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "dev"}, mode = Permission.Mode.ANY_OF)
    public void setUpgradeLevel(
            CommandSender ctx,
            @Argument("upgrade") UpgradeEnum upgradeEnum,
            @Argument("level") @Range(min = "0", max = "10") int level
    ) {
        if (!(ctx instanceof Player p0)) {
            return;
        }

        GunGame.getInstance().getDatabaseManager().getPlayerDao().getPlayerUpgradeCache().get(p0.getUniqueId()).getUpgrades().put(upgradeEnum, level);
        GunGame.getTexter().response(p0, "&aYour " + upgradeEnum.name() + " level has been set to: " + level);
        GunGame.getInstance().getDatabaseManager().getPlayerDao().updateUpgradesFromCache(p0.getUniqueId()).subscribe();
    }

    @Command(ConfigDb.COMMAND_BASE + " dev set-kills <kills>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "dev"}, mode = Permission.Mode.ANY_OF)
    public void setKills(
            CommandSender ctx,
            @Argument("kills") @Range(min = "0", max = "100") int kills
    ) {
        if (ctx instanceof Player player) {
            GunGame.getInstance().getDatabaseManager().getPlayerDao().setCacheKills(player.getUniqueId(), kills);
            GunGame.getTexter().response(player, "&aYour kills has been set to " + kills);
            GunGame.getInstance().getDatabaseManager().getPlayerDao().updateFromCache(player.getUniqueId()).subscribe();
        }
    }

    @Command(ConfigDb.COMMAND_BASE + " dev set-deaths <deaths>")
    @Permission(value = {ConfigDb.PERMISSION_NODE + "*", ConfigDb.PERMISSION_NODE + "dev"}, mode = Permission.Mode.ANY_OF)
    public void setDeaths(
            CommandSender ctx,
            @Argument("deaths") @Range(min = "0", max = "100") int deaths
    ) {
        if (ctx instanceof Player player) {
            GunGame.getInstance().getDatabaseManager().getPlayerDao().setCacheDeaths(player.getUniqueId(), deaths);
            GunGame.getTexter().response(player, "&aYour deaths has been set to " + deaths);
            GunGame.getInstance().getDatabaseManager().getPlayerDao().updateFromCache(player.getUniqueId()).subscribe();
        }
    }
}
