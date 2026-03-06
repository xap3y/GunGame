package eu.xap3y.gungame.model;

import eu.xap3y.gungame.api.SamePair;
import eu.xap3y.gungame.api.iface.ArenaInterface;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Arena implements ArenaInterface {

    private String arenaName;
    private String displayName;
    private boolean enabled = true;
    private String builder;
    private double rating; // 0.0 - 5.0
    private Location spawn;

    private boolean allowBoosters = false;
    private boolean waterKills = true;

    private SamePair<Location> dimensions;
    private SamePair<Location> safeZone;

    public Arena(String name, Location loc) {
        this.arenaName = name;
        this.displayName = name;
        this.enabled = false;
        this.rating = 0.0;
        this.spawn = loc;
    }

    public boolean isComplete() {
        return arenaName != null && spawn != null && dimensions != null && safeZone != null;
    }

    public boolean isInSafeZone(@NotNull Location loc) {
        if (safeZone == null) return false;
        return isInRegion(loc, safeZone);
    }

    public boolean isInDimensions(@NotNull Location loc) {
        if (dimensions == null) return false;
        return isInRegion(loc, dimensions);
    }

    private boolean isInRegion(@NotNull Location loc, @NotNull SamePair<Location> pair) {
        Location min = pair.getFirst();
        Location max = pair.getSecond();
        return isInside(loc, min, max);
    }

    public static boolean isInside(@Nullable Location loc, @Nullable Location cornerA, @Nullable Location cornerB) {
        if (loc == null || cornerA == null || cornerB == null) return false;

        World w = cornerA.getWorld();
        if (w == null || !w.equals(cornerB.getWorld()) || !w.equals(loc.getWorld())) return false;

        double minX = Math.min(cornerA.getX(), cornerB.getX());
        double maxX = Math.max(cornerA.getX(), cornerB.getX());
        double minY = Math.min(cornerA.getY(), cornerB.getY());
        double maxY = Math.max(cornerA.getY(), cornerB.getY());
        double minZ = Math.min(cornerA.getZ(), cornerB.getZ());
        double maxZ = Math.max(cornerA.getZ(), cornerB.getZ());

        double x = loc.getX();
        double y = loc.getY();
        double z = loc.getZ();

        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }
}
