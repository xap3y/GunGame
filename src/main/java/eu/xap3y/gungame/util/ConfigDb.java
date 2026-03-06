package eu.xap3y.gungame.util;

import eu.xap3y.gungame.api.Pair;
import org.bukkit.GameMode;
import org.bukkit.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConfigDb {

    public static final String VERSION = "@VERSION@";
    public static final String GIT_HASH = "@GIT_HASH@";
    public static final String GIT_URL = "@GIT_URL@";
    public static final String PERMISSION_NODE = "@PERMISSION_NODE@.";
    public static final String COMMAND_BASE = "@COMMAND_BASE@";
    public static final String MAIN_COMMAND = "@MAIN_COMMAND@";
    public static final GameMode GAMEMODE_SET = GameMode.ADVENTURE;

    public static final Map<UUID, Pair<Location, Location>> POS_CACHE = new HashMap<>();

    public static final List<UUID> LAST_LAUNCHES = new java.util.ArrayList<>();
    public static final List<UUID> FALL_DAMAGE_CANCEL = new java.util.ArrayList<>();
}
