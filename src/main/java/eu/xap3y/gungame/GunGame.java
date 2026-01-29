package eu.xap3y.gungame;

import eu.xap3y.gungame.command.RootCommand;
import eu.xap3y.gungame.listener.GunGameListener;
import eu.xap3y.gungame.listener.PlayerJoinListener;
import eu.xap3y.gungame.manager.ArenaManager;
import eu.xap3y.gungame.manager.CommandManager;
import eu.xap3y.gungame.manager.ConfigManager;
import eu.xap3y.gungame.manager.LangManager;
import eu.xap3y.gungame.model.Arena;
import eu.xap3y.gungame.model.Progression;
import eu.xap3y.gungame.service.LevelingService;
import eu.xap3y.gungame.service.Texter;
import eu.xap3y.xagui.XaGui;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;

@Getter
public final class GunGame extends JavaPlugin {

    @Getter
    private static GunGame instance;

    private Texter texter;

    @Getter
    private boolean useComponents = false;

    private XaGui xagui;

    private LangManager langManager;

    private Progression progression;

    private LevelingService levelingService;

    private final ArenaManager arenaManager = new ArenaManager();

    @Override
    public void onEnable() {
        instance = this;

        //  Initializing XaGUI  \\
        xagui = new XaGui(this);

        //  Creating parser & Parsing command classes below  \\
        CommandManager cmdManager = new CommandManager(true);
        cmdManager.parseLegacy(new RootCommand());


        //  Saving if not exists & Reloading config file  \\
        ConfigManager.reloadConfig();

        //  Setting up texter  \\
        String prefix = getConfig().getString("prefix");
        if (prefix == null) prefix = "&7[&bserver&7] &r";
        texter = new Texter(prefix, false, null);

        langManager = new LangManager(new File(getDataFolder(), "locale.yml"));

        //  Registering PlaceholderAPI  \\
        //registerPapi();

        progression = new Progression();
        levelingService = new LevelingService(progression);

        //   Registering listeners  \\
        PluginManager manager = getServer().getPluginManager();
        registerListeners(manager);

        arenaManager.setCurrentArena(new Arena(
                "Default Arena",
                "XAP3Y",
                5.0,
                new Location(Bukkit.getWorld("test"), 0.5, 100, 0.5)
        ));

        try {
            Class.forName("net.kyori.adventure.text.Component");
            useComponents = true;
        } catch (ClassNotFoundException e) {
            useComponents = false;
        }
    }

    private void registerPermission(String permission) {
        Bukkit.getPluginManager().addPermission(new Permission(permission));
    }

    private static void registerListeners(PluginManager manager) {
        //  Registering listeners  \\
        Listener[] listeners = new Listener[]{
                new GunGameListener(GunGame.getInstance().getLevelingService()),
                new PlayerJoinListener()
        };

        for (Listener listener : listeners) {
            manager.registerEvents(listener, GunGame.getInstance());
        }
    }

    /*private void registerPapi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            *//*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             *//*
            //Bukkit.getPluginManager().registerEvents(new MyListener(), this);
        } else {
            *//*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             *//*
            //Bukkit.getPluginManager().disablePlugin(this);
        }
    }*/
}
