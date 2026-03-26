package eu.xap3y.gungame.database.dto;

import eu.xap3y.gungame.api.enums.UpgradeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class PlayerUpgradesDto {

    private Map<UpgradeEnum, Integer> upgrades = new HashMap<>();

    public PlayerUpgradesDto() {
        Arrays.stream(UpgradeEnum.values()).toList().forEach(upgrade -> upgrades.put(upgrade, 0));
    }

    public int getUpgradeLevel(UpgradeEnum upgrade) {
        return upgrades.getOrDefault(upgrade, 0);
    }

    public void setUpgradeLevel(UpgradeEnum upgrade, int level) {
        upgrades.put(upgrade, level);
    }
}
