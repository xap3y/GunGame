package eu.xap3y.gungame.util;

import eu.xap3y.gungame.api.Pair;
import eu.xap3y.gungame.api.enums.KillEffectType;
import eu.xap3y.gungame.api.iface.KillEffect;
import eu.xap3y.gungame.effects.BloodKillEffect;
import eu.xap3y.gungame.effects.SmokeBurstEffect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigDb {

    public static final String VERSION = "@VERSION@";
    public static final String GIT_HASH = "@GIT_HASH@";
    public static final String GIT_URL = "@GIT_URL@";
    public static final String PERMISSION_NODE = "@PERMISSION_NODE@.";
    public static final String COMMAND_BASE = "@COMMAND_BASE@";
    public static final String MAIN_COMMAND = "@MAIN_COMMAND@";
    public static final GameMode GAMEMODE_SET = GameMode.ADVENTURE;

    public static boolean STREAM_DEBUG_CHAT = false;

    public static final Map<UUID, Pair<Location, Location>> POS_CACHE = new HashMap<>();

    public static final Set<UUID> LAST_LAUNCHES = new HashSet<>();
    public static final Set<UUID> FALL_DAMAGE_CANCEL = new HashSet<>();

    // 1.8.8 issue
    public static final Set<UUID> LAST_DEATHS_CALLS = new HashSet<>();

    public static final Map<UUID, BukkitTask> FALL_DAMAGE_CANCEL_TASK = new ConcurrentHashMap<>();

    public static final Map<KillEffectType, KillEffect> KILL_EFFECT_MAP = new HashMap<>() {{
       put(KillEffectType.BLOOD, new BloodKillEffect());
       put(KillEffectType.SMOKE_BURST, new SmokeBurstEffect());
    }};
}
