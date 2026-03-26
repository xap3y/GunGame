package eu.xap3y.gungame.manager;

import eu.xap3y.gungame.GunGame;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class LangManager {

    private final File langFile;
    private YamlConfiguration langYamlConfig;

    public LangManager(File langFile) {
        GunGame.getTexter().logPos();
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
        GunGame.getTexter().logPos();
        langYamlConfig = YamlConfiguration.loadConfiguration(langFile);
    }


    public String get(String path) {
        return get(path, "LANGUAGE STRING NOT FOUND");
    }

    public List<String> getList(String path, String... def) {
        return langYamlConfig.getStringList(path).isEmpty() ? Arrays.stream(def).toList() : langYamlConfig.getStringList(path);
    }

    public String get(String path, String def) {
        return langYamlConfig.getString(path, def);
    }
}
