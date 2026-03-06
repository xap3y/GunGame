package eu.xap3y.gungame.api.iface;

import eu.xap3y.gungame.api.dto.BoardConfig;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface ScoreboardInterface <T> {

    void loadConfig();

    void reloadAllBoards();

    void addBoard(Player p0);

    void removeBoard(UUID p0);

    void updateBoard(UUID p0);

    void modifyBoard(UUID p0, T board);

    void updateAllBoardTimes();

     T getBoard(UUID uuid);

    BoardConfig getBoardConfig();
}
