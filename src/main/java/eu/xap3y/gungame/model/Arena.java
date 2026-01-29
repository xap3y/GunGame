package eu.xap3y.gungame.model;

import eu.xap3y.gungame.api.iface.ArenaInterface;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Arena implements ArenaInterface {

    private String arenaName;
    private String builder;
    private double rating; // 0.0 - 5.0
    private Location spawn;

    private final boolean allowBoosters = false;
    private final boolean waterKills = true;

}
