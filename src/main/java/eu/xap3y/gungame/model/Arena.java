package eu.xap3y.gungame.model;

import eu.xap3y.gungame.api.iface.ArenaInterface;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Arena implements ArenaInterface {

    private String arenaName;
    private boolean enabled = true;
    private String builder;
    private double rating; // 0.0 - 5.0
    private Location spawn;

    private boolean allowBoosters = false;
    private boolean waterKills = true;

    public Arena(String name, Location loc) {
        this.arenaName = name;
        this.spawn = loc;
    }
}
