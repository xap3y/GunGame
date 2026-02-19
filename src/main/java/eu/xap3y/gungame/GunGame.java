package eu.xap3y.gungame;

import com.cryptomorin.xseries.XSound;
import eu.xap3y.gungame.command.DevCommand;
import eu.xap3y.gungame.command.RootCommand;
import eu.xap3y.gungame.command.SetupCommand;
import eu.xap3y.gungame.database.DatabaseManager;
import eu.xap3y.gungame.listener.GunGameListener;
import eu.xap3y.gungame.listener.PlayerConnectListener;
import eu.xap3y.gungame.listener.PlayerMoveListener;
import eu.xap3y.gungame.manager.*;
import eu.xap3y.gungame.model.Progression;
import eu.xap3y.gungame.service.LevelingService;
import eu.xap3y.gungame.service.Texter;
import eu.xap3y.xagui.XaGui;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

@Getter
public final class GunGame extends JavaPlugin {

    @Getter
    private static GunGame instance;

    @Getter
    private static Texter texter;

    @Getter
    private boolean useComponents = false;

    private XaGui xagui;

    private LangManager langManager;

    private Progression progression;

    private LevelingService levelingService;

    private DatabaseManager databaseManager;

    private final ArenaManager arenaManager = new ArenaManager();

    private ArenaLoader arenaLoader;

    @Override
    public void onEnable() {
        instance = this;

        //  Initializing XaGUI  \\
        xagui = new XaGui(this);

        xagui.setCloseButtonSound(XSound.BLOCK_COPPER_DOOR_CLOSE.or(XSound.BLOCK_WOODEN_DOOR_CLOSE).get());

        //  Creating parser & Parsing command classes below  \\
        CommandManager cmdManager = new CommandManager(true);
        cmdManager.parseLegacy(new RootCommand(), new SetupCommand(), new DevCommand());


        //  Saving if not exists & Reloading config file  \\
        ConfigManager.reloadConfig();

        //  Setting up texter  \\
        String prefix = getConfig().getString("prefix");
        if (prefix == null) prefix = "&7[&bserver&7] &r";
        File debugFile = new File(getDataFolder(), "debug.log");
        texter = new Texter(prefix, true, debugFile);

        langManager = new LangManager(new File(getDataFolder(), "locale.yml"));

        //  Registering PlaceholderAPI  \\
        //registerPapi();

        progression = new Progression();
        levelingService = new LevelingService(progression);

        //   Registering listeners  \\
        PluginManager manager = getServer().getPluginManager();
        registerListeners(manager);

        /*arenaManager.setCurrentArena(new Arena(
                "Default Arena",
                "XAP3Y",
                5.0,
                new Location(Bukkit.getWorld("test"), 0.5, 4, 0.5)
        ));*/

        arenaLoader = new ArenaLoader(new File(getDataFolder(), "arenas.yml"));
        arenaLoader.refreshArenaPool();

        arenaManager.restartArenaRotationTask();

        try {
            Class.forName("net.kyori.adventure.text.Component");
            useComponents = true;
        } catch (ClassNotFoundException e) {
            useComponents = false;
        }

        databaseManager = new DatabaseManager(this);
    }

    private void registerPermission(String permission) {
        Bukkit.getPluginManager().addPermission(new Permission(permission));
    }

    private static void registerListeners(PluginManager manager) {
        //  Registering listeners  \\
        Listener[] listeners = new Listener[]{
                new GunGameListener(GunGame.getInstance().getLevelingService()),
                new PlayerConnectListener(),
                new PlayerMoveListener()
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
