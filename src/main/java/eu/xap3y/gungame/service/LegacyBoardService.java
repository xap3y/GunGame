package eu.xap3y.gungame.service;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.dto.BoardConfig;
import eu.xap3y.gungame.api.iface.ScoreboardInterface;
import eu.xap3y.gungame.database.dto.PlayerStatsDto;
import eu.xap3y.gungame.model.Arena;
import eu.xap3y.gungame.util.Utils;
import fr.mrmicky.fastboard.FastBoard;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

@NoArgsConstructor
public class LegacyBoardService implements ScoreboardInterface<FastBoard> {

    private static final Map<UUID, FastBoard> boards = new HashMap<>();

    private static BoardConfig boardConfig;

    @Override
    public FastBoard getBoard(UUID uuid) {
        return boards.get(uuid);
    }

    @Override
    public BoardConfig getBoardConfig() {
        return LegacyBoardService.boardConfig;
    }

    @Override
    public void loadConfig() {
        GunGame.getTexter().logPos();
        BoardConfig boardConfig = new BoardConfig();
        boardConfig.setTitle(GunGame.getInstance().getConfig().getString("scoreboard.title", "§b§l✦  §e§lGunGame  §b§l✦"));
        boardConfig.setLines(GunGame.getInstance().getConfig().getStringList("scoreboard.lines"));
        LegacyBoardService.boardConfig = boardConfig;
    }

    @Override
    public void reloadAllBoards() {
        boards.forEach((uuid, board) -> {
            FastBoard newBoard = new FastBoard(board.getPlayer());
            newBoard.updateTitle(Texter.colored(boardConfig.getTitle()));
            boards.replace(uuid, newBoard);
            modifyBoard(uuid, newBoard);
        });
    }

    @Override
    public void addBoard(Player p0) {
        GunGame.getTexter().logPos();
        FastBoard board = new FastBoard(p0);

        board.updateTitle(Texter.colored(boardConfig.getTitle()));

        boards.put(p0.getUniqueId(), board);
        modifyBoard(p0.getUniqueId(), board);
    }

    @Override
    public void removeBoard(UUID p0) {
        boards.get(p0).delete();
        boards.remove(p0);
    }

    @Override
    public void updateBoard(UUID p0) {
        FastBoard board = boards.get(p0);
        if (board == null) {
            return;
        }

        else modifyBoard(p0, board);
    }

    @Override
    public void modifyBoard(UUID p0, FastBoard board) {
        PlayerStatsDto cachedDto = GunGame.getInstance().getDatabaseManager().getPlayerDao().getPlayerCache().getOrDefault(p0, new PlayerStatsDto());

        Arena a = GunGame.getInstance().getArenaManager().getCurrentArena();
        String currentArena = (a == null) ? "&cN/A" : a.getArenaName();

        String mapTime;
        long timeLeft = GunGame.getInstance().getArenaManager().getSecondsUntilNextArena();
        if (timeLeft > 0) {
            mapTime = "&e" + Utils.formatTime(timeLeft);
        } else {
            mapTime = "&cN/A";
        }

        // KD rounded to 1 decimal
        double kd = (cachedDto.getDeaths() == 0) ? cachedDto.getKills() : (double) cachedDto.getKills() / cachedDto.getDeaths();

        String kdColor;
        if (kd < 1) {
            kdColor = "&c";
        } else if (kd == 1) {
            kdColor = "&e";
        } else {
            kdColor = "&a";
        }

        Map<String, String> replace = new HashMap<>() {{
            put("map", currentArena);
            put("kills", cachedDto.getKills() + "");
            put("deaths", cachedDto.getDeaths() + "");
            put("level", "0");
            put("killstreak", GunGame.getInstance().getLevelingService().getKillstreak(p0) + "");
            put("xp", cachedDto.getXp() + "");
            put("event", "N/A");
            put("players", GunGame.getInstance().getArenaManager().getPlayers().size() + "");
            put("stage", GunGame.getInstance().getLevelingService().get(p0).getLevel() + "");
            put("maptime", mapTime);
            put("kd", kdColor + (kd % 1 == 0 ? String.format("%.0f", kd) : String.format("%.1f", kd)));
        }};

        if (GunGame.getEcon() != null) {
            replace.put("coins", GunGame.getEcon().getBalance(Bukkit.getOfflinePlayer(p0)) + "");
        } else {
            replace.put("coins", "&cN/A");
        }

        List<String> updatedLines = boardConfig.getLines()
                .stream()
                .map(line -> {
                    String updated = line;
                    for (Map.Entry<String, String> entry : replace.entrySet()) {
                        updated = updated.replace("{" + entry.getKey() + "}", entry.getValue());
                    }
                    return updated;
                })
                .map(Texter::colored)
                .toList();

        board.updateLines(updatedLines);
    }

    @Override
    public void updateAllBoardTimes() {
        boards.forEach((uuid, board) -> updateBoardTime(uuid));
    }

    public void updateBoardTime(UUID p0) {
        FastBoard board = boards.get(p0);
        if (board == null) {
            return;
        }

        String matcher;

        Optional<String> line = boardConfig.getLines().stream().filter(l -> l.contains("{maptime}")).findFirst();
        if (line.isEmpty()) {
            return;
        } else {
            matcher = Texter.colored(line.get().split("\\{maptime}")[0].replace("{maptime}", "").trim());
        }

        Optional<Integer> lineIndex = board.getLines()
                .stream()
                .map(comp -> {
                    //GunGame.getTexter().console("Checking line content: " + comp + " on index " + board.getLines().indexOf(comp));
                    if (comp.contains(matcher)) {
                        return board.getLines().indexOf(comp);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .findFirst();

        if (lineIndex.isEmpty()) {
            return;
        }

        String mapTime;
        long timeLeft = GunGame.getInstance().getArenaManager().getSecondsUntilNextArena();
        if (timeLeft > 0) {
            mapTime = "&e" + Utils.formatTime(timeLeft);
        } else {
            mapTime = "&cN/A";
        }

        String updatedLine = Texter.colored(boardConfig.getLines().get(lineIndex.get()).replace("{maptime}", mapTime));
        board.updateLine(lineIndex.get(), updatedLine);
    }
}
