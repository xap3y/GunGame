package eu.xap3y.gungame.service;

import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.enums.UpgradeEnum;
import eu.xap3y.gungame.database.dto.PlayerUpgradesDto;
import org.bukkit.Effect;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.UUID;

public class UpgradeService {

    private static UpgradeService instance;

    private final PotionEffect[] KILL_EFFECTS = new PotionEffect[] {
            XPotion.SPEED.buildPotionEffect(60, 1),
            XPotion.JUMP_BOOST.buildPotionEffect(75, 1),
            XPotion.STRENGTH.buildPotionEffect(100, 1),
            XPotion.INSTANT_HEALTH.buildPotionEffect(1, 4),
            XPotion.INVISIBILITY.buildPotionEffect(40, 1)
    };

    public UpgradeService() {
        instance = this;
    }

    public static UpgradeService getInstance() {
        if (instance == null) {
            instance = new UpgradeService();
        }
        return instance;
    }

    public PlayerUpgradesDto getPlayerUpgradeCache(UUID p0) {
        return GunGame.getInstance().getDatabaseManager().getPlayerDao().getPlayerUpgradeCache().get(p0);
    }

    public void processLifeSteal(LivingEntity p0) {
        PlayerUpgradesDto cache = getPlayerUpgradeCache(p0.getUniqueId());
        if (cache == null) {return;}
        int level = cache.getUpgradeLevel(UpgradeEnum.LIFE_STEAL);
        if (level <= 0) {return;}

        // max 5 levels, give regeneration II for 0.5 each level, so max 2.5 seconds of regeneration II at level 5
        int duration = (int) (level * 0.5 * 20); //
        XPotion.REGENERATION.buildPotionEffect(duration, 2).apply(p0);
    }

    public void processRandomEffect(LivingEntity p0) {
        PlayerUpgradesDto cache = getPlayerUpgradeCache(p0.getUniqueId());
        if (cache == null) {return;}
        int level = cache.getUpgradeLevel(UpgradeEnum.KILL_EFFECT);
        if (level <= 0) {return;}

        int chance = level * 3; // 3% chance per level, so max 15% chance at level 5
        if (Math.random() * 100 < chance) {
            XSound.ITEM_HONEY_BOTTLE_DRINK.or(XSound.ENTITY_GENERIC_DRINK).play(p0, .6f, .5f);
            PotionEffect effect = KILL_EFFECTS[(int) (Math.random() * KILL_EFFECTS.length)];
            effect.apply(p0);
            p0.getWorld().playEffect(p0.getLocation(), Effect.MOBSPAWNER_FLAMES, 0);
        }
    }

    public boolean processDoubleUpgrade(Player p0) {
        PlayerUpgradesDto cache = getPlayerUpgradeCache(p0.getUniqueId());
        if (cache == null) {return false;}
        int level = cache.getUpgradeLevel(UpgradeEnum.DOUBLE_UPGRADE);
        if (level <= 0) {return false;}

        int chance = level * 2; // 2% chance per level, so max 10% chance at level 5
        if (Math.random() * 100 < chance) {
            XSound.BLOCK_PISTON_CONTRACT.play(p0, .65f, .2f);
            GunGame.getInstance().getParApi().LIST_1_8.CRIT_MAGIC.packet(true, p0.getLocation()).sendTo(p0);
            return true;
        }
        return false;
    }
}
