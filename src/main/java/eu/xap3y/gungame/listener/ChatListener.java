package eu.xap3y.gungame.listener;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.service.Texter;
import eu.xap3y.gungame.util.ConfigDb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {


    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {

        boolean enabled = GunGame.getInstance().getConfig().getBoolean("chat.enabled", true);

        if (!enabled) {
            return;
        } else if (!GunGame.getInstance().getArenaManager().isPlayerInArena(event.getPlayer().getUniqueId())) {
            return;
        }

        Player p = event.getPlayer();

        boolean isAdmin = p.isOp() || p.hasPermission(ConfigDb.PERMISSION_NODE + "*");

        String allowed = GunGame.getInstance().getConfig().getString("chat.allowed-characters", "^[a-zA-Z0-9_ !.,?/@#$%^&*()\\-_=+;: '\"\\[\\]{}<>`~\\\\|]*$");

        String message = event.getMessage();

        try {
            if (!message.matches(allowed) && !p.isOp() && !p.hasPermission(ConfigDb.PERMISSION_NODE + "chat.bypass")) {
                event.setCancelled(true);
                GunGame.getTexter().responseLang(p, "chat.invalid-message", "&cYour message contains invalid characters.");
                return;
            }
        } catch (Exception ignored) {
            // IGNORE
        }


        String format = GunGame.getInstance().getConfig().getString("chat.format", "[{lvl}] {player}: {message}");

        int level = GunGame.getInstance().getLevelingService().get(p.getUniqueId()).getLevel();

        boolean allowCodes = GunGame.getInstance().getConfig().getBoolean("chat.allow-color-format-codes", false);

        String messageToFormat = allowCodes ? Texter.colored(message) : event.getMessage().replaceAll("&([0-9a-fk-or])", "");
        if (messageToFormat.strip().isBlank()) {
            event.setCancelled(true);
            GunGame.getTexter().responseLang(p, "chat.empty-message", "&cYour message cannot be empty!");
            return;
        }
        String playerFormatted = isAdmin ? GunGame.getInstance().getConfig().getString("chat.admin-color", "§c") + p.getName() : p.getName();

        String formatted = format.replace("{lvl}", String.valueOf(level))
                .replace("{player}", playerFormatted)
                .replace("%", "%%")
                .replace("{message}", "%2$s");

        event.setFormat(formatted);
    }
}
