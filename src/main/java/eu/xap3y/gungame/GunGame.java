package eu.xap3y.gungame;

import com.cryptomorin.xseries.XSound;
import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.core.ParticleNativeCore;
import eu.xap3y.gungame.api.iface.ScoreboardInterface;
import eu.xap3y.gungame.command.DebugCommand;
import eu.xap3y.gungame.command.DevCommand;
import eu.xap3y.gungame.command.RootCommand;
import eu.xap3y.gungame.command.SetupCommand;
import eu.xap3y.gungame.database.DatabaseManager;
import eu.xap3y.gungame.hook.PapiExpansion;
import eu.xap3y.gungame.listener.*;
import eu.xap3y.gungame.manager.*;
import eu.xap3y.gungame.model.Progression;
import eu.xap3y.gungame.service.LegacyBoardService;
import eu.xap3y.gungame.service.LevelingService;
import eu.xap3y.gungame.service.Texter;
import eu.xap3y.gungame.util.ConfigDb;
import eu.xap3y.xagui.XaGui;
import eu.xap3y.xagui.XaGuiPlugin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.scheduler.BukkitRunnable;

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

    @Getter
    private ParticleNativeAPI parApi;

    @Getter
    private static Economy econ = null;

    @Getter
    private static ScoreboardInterface<?> boardApi;

    @Override
    public void onEnable() {
        instance = this;

        //  Creating parser & Parsing command classes below  \\
        CommandManager cmdManager = new CommandManager(true);
        cmdManager.parseLegacy(new RootCommand(), new SetupCommand(), new DevCommand(), new DebugCommand());
        //  Saving if not exists & Reloading config file  \\
        ConfigManager.reloadConfig();

        //  Setting up texter  \\
        String prefix = getConfig().getString("prefix");
        if (prefix == null) prefix = "&7[&bserver&7] &r";
        File logFolder = new File(getDataFolder(), "logs");
        texter = new Texter(prefix, true, logFolder);

        // Setting up lang manager  (locale.yml) \\
        langManager = new LangManager(new File(getDataFolder(), "locale.yml"));

        //  Registering PlaceholderAPI  \\
        //registerPapi();

        // Initializing progression & player leveling service  \\
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

        // Load arenas (maps) from yml file and refresh arena pool
        arenaLoader = new ArenaLoader(new File(getDataFolder(), "arenas.yml"));

        this.getServer().getGlobalRegionScheduler().execute(this, () -> {
            arenaLoader.refreshArenaPool();

            // Start map rotation task
            arenaManager.restartArenaRotationTask();
        });


        // Test if plugin is running on PaperMc or other platform with adventure api
        try {
            Class.forName("net.kyori.adventure.text.Component");
            useComponents = true;
        } catch (ClassNotFoundException e) {
            useComponents = false;
        }

        parApi = ParticleNativeCore.loadAPI(this);

        // Database
        databaseManager = new DatabaseManager(this);

        // Try load Vault economy in 15 tries
        BukkitRunnable loadEconomy = new BukkitRunnable() {
            int runs = 0;
            @Override
            public void run() {
                runs++;
                if (runs > 15) {
                    getLogger().severe("Failed to find Vault dependency after 15 attempts. Disabling plugin.");
                    Bukkit.getPluginManager().disablePlugin(GunGame.this);
                    this.cancel();
                    return;
                }
                if (!setupEconomy()) {
                    getLogger().warning("Vault dependency not found! Retrying...");
                } else {
                    texter.console("&aSuccessfully hooked into Vault economy!");
                    if (boardApi != null) {
                        boardApi.reloadAllBoards();
                    }
                    this.cancel();
                }
            }
        };

        loadEconomy.runTaskTimerAsynchronously(this, 60L, 120L);

        // Scoreboard
        /*if (!useComponents)
            boardApi = new AdventureBoardService();
        else*/
        boardApi = new LegacyBoardService();

        boardApi.loadConfig();

        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (boardApi != null) {
                boardApi.updateAllBoardTimes();
            }
        }, 0L, 20L);

        //  Initializing XaGUI  \\
        xagui = XaGuiPlugin.getXaGui();

        xagui.setCloseButtonSound(XSound.BLOCK_COPPER_DOOR_CLOSE.or(XSound.BLOCK_WOODEN_DOOR_CLOSE).get());

        registerPermission(ConfigDb.PERMISSION_NODE + "chat.bypass");

        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : GunGame.getInstance().getArenaLoader().getArenaPool().stream().map(a -> a.getSpawn().getWorld()).toList()) {
                    world.setTime(1000);
                    world.setStorm(false);
                    world.setThundering(false);
                }
            }
        }.runTaskTimer(this, 0L, 120L);

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PapiExpansion().register();
        }
    }

    private void registerPermission(String permission) {
        Bukkit.getPluginManager().addPermission(new Permission(permission));
    }

    private static void registerListeners(PluginManager manager) {
        //  Registering listeners  \\
        Listener[] listeners = new Listener[]{
                new GunGameListener(GunGame.getInstance().getLevelingService()),
                new PlayerConnectListener(),
                new PlayerMoveListener(),
                new EntityDamageListener(),
                new ProjectileLaunchListener(),
                new WandListener(),
                new ChatListener(),
                new OtherListener()
        };

        for (Listener listener : listeners) {
            texter.console("Registering listener: " + listener.getClass().getName());
            manager.registerEvents(listener, GunGame.getInstance());
        }
    }

    private void registerPapi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            //Bukkit.getPluginManager().registerEvents(new PapiListener(), this);
        } else {
            //Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return true;
    }
}
