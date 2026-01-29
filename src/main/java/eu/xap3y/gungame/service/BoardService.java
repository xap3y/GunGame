package eu.xap3y.gungame.service;

import eu.xap3y.gungame.manager.ConfigManager;
import eu.xap3y.xagui.adapter.ParseUtil;
import fr.mrmicky.fastboard.adventure.FastBoard;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.*;

public class BoardService {

    private static final Map<UUID, FastBoard> boards = new HashMap<>();

    //private static BoardConfig boardConfig;

    public static FastBoard getBoard(UUID uuid) {
        return boards.get(uuid);
    }

    /*public static void loadConfig() {
        BoardConfig boardConfig = new BoardConfig();
        boardConfig.setTitle(ConfigManager.getLocale().getString("scoreboard.title", "§b§l✦  §e§lPrison  §b§l✦"));
        boardConfig.setLines(ConfigManager.getLocale().getStringList("scoreboard.lines"));
        BoardService.boardConfig = boardConfig;
    }*/

    public static void reloadAllBoards() {
        boards.forEach((uuid, board) -> {
            FastBoard newBoard = new FastBoard(board.getPlayer());
            newBoard.updateTitle(ParseUtil.parseText("&aGun Game :)"));
            boards.replace(uuid, newBoard);
            modifyBoard(uuid, newBoard);
        });
    }

    public static void addBoard(Player p0) {
        FastBoard board = new FastBoard(p0);

        board.updateTitle(ParseUtil.parseText("&aGun Game :)"));

        boards.put(p0.getUniqueId(), board);
        modifyBoard(p0.getUniqueId(), board);
    }

    public static void removeBoard(UUID p0) {
        boards.get(p0).delete();
        boards.remove(p0);
    }

    public static void updateBoard(UUID p0) {
        FastBoard board = boards.get(p0);
        if (board == null) {
            return;
        }

        else modifyBoard(p0, board);
    }

    private static void modifyBoard(UUID p0, FastBoard board) {

        List<Component> lines = new ArrayList<>();

        lines.add(ParseUtil.parseText(""));
        lines.add(ParseUtil.parseText("&aName: &f" + board.getPlayer().getName()));

        board.updateLines(lines);
    }
}

