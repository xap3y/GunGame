package eu.xap3y.gungame.service;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.dto.BoardConfig;
import eu.xap3y.gungame.api.iface.ScoreboardInterface;
import eu.xap3y.gungame.database.dto.PlayerStatsDto;
import eu.xap3y.gungame.model.Arena;
import eu.xap3y.gungame.util.Utils;
import eu.xap3y.xagui.adapter.ParseUtil;
import fr.mrmicky.fastboard.adventure.FastBoard;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.entity.Player;

import java.util.*;

@NoArgsConstructor
public class AdventureBoardService implements ScoreboardInterface<FastBoard> {

    private static final Map<UUID, FastBoard> boards = new HashMap<>();

    private static BoardConfig boardConfig;

    @Override
    public FastBoard getBoard(UUID uuid) {
        return boards.get(uuid);
    }

    @Override
    public BoardConfig getBoardConfig() {
        return boardConfig;
    }

    @Override
    public void loadConfig() {
        BoardConfig boardConfig = new BoardConfig();
        boardConfig.setTitle(GunGame.getInstance().getConfig().getString("scoreboard.title", "§b§l✦  §e§lGunGame  §b§l✦"));
        boardConfig.setLines(GunGame.getInstance().getConfig().getStringList("scoreboard.lines"));
        AdventureBoardService.boardConfig = boardConfig;
    }

    @Override
    public void reloadAllBoards() {
        boards.forEach((uuid, board) -> {
            FastBoard newBoard = new FastBoard(board.getPlayer());
            newBoard.updateTitle(ParseUtil.parseText(boardConfig.getTitle()));
            boards.replace(uuid, newBoard);
            modifyBoard(uuid, newBoard);
        });
    }

    @Override
    public void addBoard(Player p0) {
        FastBoard board = new FastBoard(p0);

        board.updateTitle(ParseUtil.parseText(boardConfig.getTitle()));

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
    public void updateAllBoardTimes() {
        boards.forEach((uuid, board) -> updateBoardTime(uuid));
    }

    public void updateBoardTime(UUID p0) {
        FastBoard board = boards.get(p0);
        if (board == null) {
            return;
        }

        Optional<Integer> lineIndex = board.getLines()
                .stream()
                .map(comp -> {
                    String content = comp.contains(ParseUtil.parseText("Next map")) ? "Next map" : comp instanceof TextComponent tc ? tc.content() : "";
                    //GunGame.getTexter().console("Checking line content: " + content + " on index " + board.getLines().indexOf(comp));
                    if (content.contains("Next map")) {
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

        Component updatedLine = ParseUtil.parseText(boardConfig.getLines().get(lineIndex.get()).replace("{maptime}", mapTime));
        board.updateLine(lineIndex.get(), updatedLine);
    }

    @Override
    public void modifyBoard(UUID p0, FastBoard board) {

        PlayerStatsDto cachedDto = GunGame.getInstance().getDatabaseManager().getPlayerDao().getPlayerCache().getOrDefault(p0, new PlayerStatsDto());

        Arena a = GunGame.getInstance().getArenaManager().getCurrentArena();
        String currentArena = (a == null) ? "&cN/A" : a.getDisplayName();

        String mapTime;
        long timeLeft = GunGame.getInstance().getArenaManager().getSecondsUntilNextArena();
        if (timeLeft > 0) {
            mapTime = "&e" + Utils.formatTime(timeLeft);
        } else {
            mapTime = "&cN/A";
        }

        Map<String, String> replace = new HashMap<>() {{
            put("map", currentArena);
            put("kills", cachedDto.getKills() + "");
            put("deaths", cachedDto.getDeaths() + "");
            put("level", "0");
            put("killstreak", GunGame.getInstance().getLevelingService().getKillstreak(p0) + "");
            put("coins", cachedDto.getCoins() + "");
            put("xp", cachedDto.getXp() + "");
            put("event", "N/A");
            put("players", GunGame.getInstance().getArenaManager().getPlayers().size() + "");
            put("stage", GunGame.getInstance().getLevelingService().get(p0).getLevel() + "");
            put("maptime", mapTime);
        }};

        List<Component> updatedLines = boardConfig.getLines()
                .stream()
                .map(line -> {
                    String updated = line;
                    for (Map.Entry<String, String> entry : replace.entrySet()) {
                        updated = updated.replace("{" + entry.getKey() + "}", entry.getValue());
                    }
                    return updated;
                })
                .map(ParseUtil::parseText)
                .toList();

        board.updateLines(updatedLines);
    }
}

