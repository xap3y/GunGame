package eu.xap3y.gungame.api.enums;

public enum UpgradeEnum {
    DOUBLE_UPGRADE,
    KILL_EFFECT;

    public static UpgradeEnum fromOrdinal(int ordinal) {
        for (UpgradeEnum upgrade : values()) {
            if (upgrade.ordinal() == ordinal) {
                return upgrade;
            }
        }
        throw new IllegalArgumentException("No UpgradeEnum with ordinal " + ordinal);
    }
}
