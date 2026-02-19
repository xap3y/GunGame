package eu.xap3y.gungame.manager;

import eu.xap3y.gungame.GunGame;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class LangManager {

    private final File langFile;
    private YamlConfiguration langYamlConfig;

    public LangManager(File langFile) {
        this.langFile = langFile;
        if (!langFile.exists()) {
            try {
                langFile.createNewFile();
            } catch (Exception e) {
                // ERR
            }
        }

        reload();
    }

    public void reload() {
        langYamlConfig = YamlConfiguration.loadConfiguration(langFile);
    }


    public String get(String path) {
        return get(path, "LANGUAGE STRING NOT FOUND");
    }

    public String get(String path, String def) {
        return langYamlConfig.getString(path, def);
    }
}
