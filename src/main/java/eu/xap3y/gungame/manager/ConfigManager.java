package eu.xap3y.gungame.manager;

import eu.xap3y.gungame.GunGame;

public class ConfigManager {

    public static void reloadConfig() {
        if (!GunGame.getInstance().getDataFolder().exists()) {
            GunGame.getInstance().getDataFolder().mkdir();
        }

        GunGame.getInstance().saveResource("locale.yml", false);

        GunGame.getInstance().saveDefaultConfig();
        GunGame.getInstance().reloadConfig();
    }
}
