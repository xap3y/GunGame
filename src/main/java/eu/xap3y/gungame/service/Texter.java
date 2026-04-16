package eu.xap3y.gungame.service;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.dto.TextModifierDto;
import eu.xap3y.gungame.api.dto.TexterObjDto;
import eu.xap3y.gungame.api.enums.DefaultFontInfo;
import eu.xap3y.gungame.util.ConfigDb;
import eu.xap3y.xagui.adapter.ParseUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;

@SuppressWarnings({"deprecation", "ResultOfMethodCallIgnored"})
public class Texter {
    private final TexterObjDto data;

    public String getPrefix() {
        return data.getPrefix();
    }

    public Texter(TexterObjDto data) {
        this.data = data;
    }

    public Texter(String prefix, boolean debug, @Nullable File logFolder) {
        if (logFolder == null) {
            this.data = new TexterObjDto(prefix, false, null);
            return;
        }

        // Gen log file
        if (!logFolder.exists()) {
            logFolder.mkdirs();
        }

        // Create debug file like "debug_2025_12_31.log"
        String fileName = "debug_" + new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + ".log";
        File file = new File(logFolder, fileName);

        if (file.exists()) {
            // If file already exists, create a new one with an incremented number like "debug_2025_12_31_1.log"
            int i = 1;
            while (file.exists()) {
                file = new File(logFolder, "debug_" + new SimpleDateFormat("yyyy_MM_dd").format(new Date()) + "_" + i + ".log");
                i++;
            }
        } else {
            try {
                file.createNewFile();
            } catch (Exception e) {
                // If file creation fails, disable debug mode
                GunGame.getInstance().getLogger().log(Level.SEVERE, "Failed to create debug log file: " + e.getMessage());
                debug = false;
                file = null;
            }
        }

        this.data = new TexterObjDto(prefix, debug, file);
    }

    public void setPrefix(String prefix) {
        data.setPrefix(prefix);
    }

    public void response(CommandSender p0, String text, TextModifierDto modifiers) {
        String textToSend = modifiers.colored() ? colored(text) : text;
        String prefix = modifiers.withPrefix() ? colored(data.getPrefix()) : "";
        String finalText = prefix + textToSend;
        if (GunGame.getInstance().isUseComponents()) {
            p0.sendMessage(ParseUtil.parseText(finalText));
        } else {
            p0.sendMessage(prefix + textToSend);
        }
    }

    public void responseLang(CommandSender p0, String path) {
        responseLang(p0, path, null, null);
    }

    public void responseLang(CommandSender p0, String path, String defaultText) {
        responseLang(p0, path, defaultText, null);
    }

    public void responseLang(CommandSender p0, String path, @Nullable Map<String, String> placeholders) {
        responseLang(p0, path, null, placeholders);
    }

    public void responseLang(CommandSender p0, String path, @Nullable String def, @Nullable Map<String, String> placeholders) {
        String prefix = colored(data.getPrefix());
        if (def == null) def = path;
        String textToSend = colored(GunGame.getInstance().getLangManager().get(path, def));
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                textToSend = textToSend.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        String finalText = prefix + textToSend;
        if (GunGame.getInstance().isUseComponents()) {
            p0.sendMessage(ParseUtil.parseText(finalText));
        } else {
            p0.sendMessage(prefix + textToSend);
        }
    }

    public void broadcast(String text) {
        String prefix = colored(data.getPrefix());
        String textToSend = colored(text);
        String finalText = prefix + textToSend;

        GunGame.getInstance().getArenaManager().getPlayers().forEach(p0 -> {
            if (p0 != null) {
                if (GunGame.getInstance().isUseComponents()) {
                    p0.sendMessage(ParseUtil.parseText(finalText));
                } else {
                    p0.sendMessage(prefix + textToSend);
                }
            }
        });
        /*if (GunGame.getInstance().isUseComponents()) {
            PaperAdapter.broadcastMsg(finalText);
        } else {
            Bukkit.broadcastMessage(prefix + textToSend);
        }*/
    }

    public void response(CommandSender p0, String text) {
        response(p0, text, new TextModifierDto(true, true));
    }

    public void console(String text, TextModifierDto modifiers) {
        response(Bukkit.getConsoleSender(), text, modifiers);
    }

    public void console(String text, boolean wPrefix) {
        response(Bukkit.getConsoleSender(), text, new TextModifierDto(wPrefix, true));
    }

    public void console(String text) {
        response(Bukkit.getConsoleSender(), text);
    }

    public void debugLog(String text) {
        debugLog(text, Level.INFO);
    }

    public void logPos() {
        StackTraceElement el = Thread.currentThread().getStackTrace()[2];
        debugLog(el.toString(), Level.INFO);
    }

    public void debugLog(String text, Level level) {
        if (!data.isDebug() || data.getDebugFile() == null) return;

        File debugFile = data.getDebugFile();
        if (!debugFile.exists()) {
            try {
                debugFile.createNewFile();
            } catch (Exception e) {
                return;
            }
        }
        String levelName = level.getName() != null ? level.getName() : "";
        String currentTime = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String textToLog = String.format("[%s] [%s] %s%n", currentTime, levelName, text);

        if (ConfigDb.STREAM_DEBUG_CHAT) {
            Bukkit.broadcastMessage(Texter.colored("&8[D] &f" + text));
        }

        try {
            java.nio.file.Files.write(debugFile.toPath(), textToLog.getBytes(), java.nio.file.StandardOpenOption.APPEND);
        } catch (Exception ignored) {
        }
    }

    public static String colored(@NotNull String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String centered(String message) {
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        for (char c : message.toCharArray()) {
            if (c == '&') {
                previousCode = true;
                continue;
            } else if (previousCode) {
                previousCode = false;
                isBold = (c == 'l' || c == 'L');
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }
        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = 154 - halvedMessageSize;
        StringBuilder sb = new StringBuilder();
        int compensated = 0;
        while (compensated < toCompensate) {
            sb.append(" ");
            compensated += DefaultFontInfo.SPACE.getLength() + 1;
        }
        return sb + message;
    }
}
