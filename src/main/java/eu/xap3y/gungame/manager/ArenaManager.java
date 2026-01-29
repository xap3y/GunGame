package eu.xap3y.gungame.manager;

import eu.xap3y.gungame.model.Arena;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Data
@NoArgsConstructor
public class ArenaManager {

    private Arena currentArena;
    private final List<Player> players = new java.util.ArrayList<>();

    public void respawnPlayer(Player p0) {
        p0.teleport(currentArena.getSpawn());
    }

    public void changeArena(Arena arena) {
        resetArena();
        currentArena = arena;
    }

    public void resetArena() {
        if (currentArena != null) {
            teleportAllSpawn();
            players.clear();
            currentArena = null;
        }
    }

    public void addPlayer(Player player) {
        if (currentArena == null) return;
        if (players.stream().noneMatch((p) -> p.getUniqueId().equals(player.getUniqueId()))) {
            players.add(player);
        }
    }

    public void removePlayer(UUID playerId) {
        if (currentArena == null) return;
        players.removeIf((p) -> p.getUniqueId().equals(playerId));
    }

    public void teleportPlayerToSpawn(UUID playerId) {
        if (currentArena == null) return;
        Optional<Player> player = players.stream().filter((p) -> p.getUniqueId().equals(playerId)).findFirst();
        if (player.isEmpty()) return;
        player.get().teleport(currentArena.getSpawn());
    }

    public void teleportAllSpawn() {
        if (currentArena == null) return;
        players.forEach((p) -> p.teleport(currentArena.getSpawn()));
    }

    public boolean isPlayerInArena(UUID playerId) {
        if (currentArena == null) return false;
        return players.stream().anyMatch((p) -> p.getUniqueId().equals(playerId));
    }
}
