package eu.xap3y.gungame.api.gui;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.api.enums.UpgradeEnum;
import eu.xap3y.gungame.database.dto.PlayerUpgradesDto;
import eu.xap3y.gungame.manager.LangManager;
import eu.xap3y.gungame.util.Utils;
import eu.xap3y.xagui.VirtualMenu;
import eu.xap3y.xagui.interfaces.GuiMenuInterface;
import eu.xap3y.xagui.models.GuiButton;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class UpgradesShopGui extends VirtualMenu<Player> {

    private static final int MAX_LEVEL = 5;

    public UpgradesShopGui() {
        super("&bUpgrades Shop", 3, GunGame.getInstance().getXagui());
    }

    @Override
    public @NotNull GuiMenuInterface build(@NonNull Player ctx) {

        GuiMenuInterface gui = getGui();
        gui.fillBorder();
        gui.addCloseButton();

        AtomicBoolean open = new AtomicBoolean(true);

        FileConfiguration cfg = GunGame.getInstance().getConfig();

        PlayerUpgradesDto upgrades = GunGame.getInstance().getDatabaseManager().getPlayerDao().getPlayerUpgradeCache().computeIfAbsent(ctx.getUniqueId(), uuid -> new PlayerUpgradesDto());

        Map<UpgradeEnum, Integer> upgradeLevels = new HashMap<>();

        for (UpgradeEnum upgrade : UpgradeEnum.values()) {
            upgradeLevels.put(upgrade, upgrades.getUpgradeLevel(upgrade));
        }

        Map<UpgradeEnum, Integer> upgradeCosts = new HashMap<>();

        for (UpgradeEnum upgrade : UpgradeEnum.values()) {
            int cost = cfg.getInt("shop.upgrades-price." + upgrade.name().toLowerCase() + "." + (upgradeLevels.get(upgrade) + 1), 1000);
            upgradeCosts.put(upgrade, cost);
        }

        LangManager lang = GunGame.getInstance().getLangManager();

        gui.setName(lang.get("gui.upgrades.title", "Upgrades - " + ctx.getName()));

        GuiButton back = new GuiButton(XMaterial.ARROW.parseItem())
                .setName(lang.get("gui.upgrades.back", "&c&lBack"))
                .withListener(e -> {
                    if (!open.get()) { // UI utils prevention
                        return;
                    }
                    XSound.UI_BUTTON_CLICK.play(e.getPlayer(), .6f, 1f);
                    new ShopGui().build(null).open(ctx);
                });

        gui.setSlot(18, back);

        List<String> doubleLore = getUpgradeLore(UpgradeEnum.DOUBLE_UPGRADE, upgradeLevels.get(UpgradeEnum.DOUBLE_UPGRADE), upgradeCosts.get(UpgradeEnum.DOUBLE_UPGRADE));
        GuiButton doubleChance = new GuiButton(XMaterial.PISTON.parseItem())
                .setName(lang.get("gui.upgrades." + UpgradeEnum.DOUBLE_UPGRADE.name().toLowerCase() + ".name", "&e&lDouble Chance"))
                .setLoreList(doubleLore)
                .withListener(e -> {
                    if (!open.get()) { // UI utils prevention
                        return;
                    }
                    open.set(false);
                    gui.close(e.getPlayer());
                    buyUpgrade(ctx, UpgradeEnum.DOUBLE_UPGRADE);
                });

        gui.setSlot(11, doubleChance);

        List<String> killEffectLore = getUpgradeLore(UpgradeEnum.KILL_EFFECT, upgradeLevels.get(UpgradeEnum.KILL_EFFECT), upgradeCosts.get(UpgradeEnum.KILL_EFFECT));
        GuiButton killEffect = new GuiButton(Utils.removeAttributes(XMaterial.POTION.parseItem()))
                .setName(lang.get("gui.upgrades." + UpgradeEnum.KILL_EFFECT.name().toLowerCase() + ".name", "&e&lKill Effect"))
                .setLoreList(killEffectLore)
                .withListener(e -> {
                    if (!open.get()) {
                        return;
                    }
                    open.set(false);
                    gui.close(e.getPlayer());
                    buyUpgrade(ctx, UpgradeEnum.KILL_EFFECT);
                });

        gui.setSlot(13, killEffect);

        List<String> lifeStealLore = getUpgradeLore(UpgradeEnum.LIFE_STEAL, upgradeLevels.get(UpgradeEnum.LIFE_STEAL), upgradeCosts.get(UpgradeEnum.LIFE_STEAL));
        GuiButton lifeSteal = new GuiButton(Utils.removeAttributes(XMaterial.GOLDEN_APPLE.parseItem()))
                .setName(lang.get("gui.upgrades." + UpgradeEnum.LIFE_STEAL.name().toLowerCase() + ".name", "&c&lLife Steal"))
                .setLoreList(lifeStealLore)
                .withListener(e -> {
                    if (!open.get()) {
                        return;
                    }
                    open.set(false);
                    gui.close(e.getPlayer());
                    buyUpgrade(ctx, UpgradeEnum.LIFE_STEAL);
                });

        gui.setSlot(15, lifeSteal);

        return gui;
    }

    public List<String> getUpgradeLore(UpgradeEnum upgrade, int level, int cost) {

        boolean full = level >= MAX_LEVEL;

        LangManager lang = GunGame.getInstance().getLangManager();
        List<String> lore = lang.getList("gui.upgrades." + upgrade.name().toLowerCase() + ".lore", "&7Cost: &a{cost} coins");
        if (full) {
            // remove line with {cost} placeholder if max level reached
            lore = lore.stream().filter(line -> !line.contains("{cost}")).toList();
        }

        return lore
                .stream()
                .map(line -> line
                        .replace("{level}", level + "")
                        .replace("{cost}", cost + "")
                        .replace("{progress}", getProgression(level, MAX_LEVEL))
                        .replace("{button}", full ? lang.get("gui.upgrades.button_max", "&cMax Level") : lang.get("gui.upgrades.button_buy", "&aClick to upgrade"))
                )
                .toList();
    }

    public void buyUpgrade(Player player, UpgradeEnum upgrade) {
        if (!GunGame.getInstance().getArenaManager().getCurrentArena().isInSafeZone(player.getLocation())) {
            XSound.ENTITY_VILLAGER_NO.play(player, .6f, 1f);
            GunGame.getTexter().responseLang(player, "not-in-safezone");
            return;
        }
        PlayerUpgradesDto upgrades = GunGame.getInstance().getDatabaseManager().getPlayerDao().getPlayerUpgradeCache().computeIfAbsent(player.getUniqueId(), uuid -> new PlayerUpgradesDto());
        int currentLevel = upgrades.getUpgradeLevel(upgrade);
        int nextLevel = currentLevel + 1;

        if (nextLevel > MAX_LEVEL) {
            XSound.ENTITY_VILLAGER_NO.play(player, .6f, 1f);
            GunGame.getTexter().responseLang(player, "upgrade-max-level");
            return;
        }

        FileConfiguration cfg = GunGame.getInstance().getConfig();
        int cost = cfg.getInt("shop.upgrades-price." + upgrade.name().toLowerCase() + "." + nextLevel, 1000 * nextLevel);

        double bal = GunGame.getEcon().getBalance(player);

        if (bal < cost) {
            XSound.ENTITY_VILLAGER_NO.play(player, .6f, 1f);
            double needed = cost - bal;
            GunGame.getTexter().responseLang(player, "not-enough-money", Map.of("price", cost + "", "missing", needed + ""));
            return;
        }

        upgrades.setUpgradeLevel(upgrade, nextLevel);

        GunGame.getInstance()
                .getDatabaseManager()
                .getPlayerDao()
                .updateUpgradesFromCache(player.getUniqueId())
                .doOnSuccess(aVoid -> {
                    GunGame.getEcon().withdrawPlayer(player, cost);
                    GunGame.getTexter().responseLang(player, "upgrade-purchased", Map.of("upgrade", upgrade.name(), "level", nextLevel + ""));
                    XSound.ENTITY_VILLAGER_YES.play(player, 1f, 1f);
                    reopen(player);
                })
                .doOnError(throwable -> {
                    GunGame.getTexter().responseLang(player, "upgrade-purchase-failed");
                })
                .subscribe();
    }

    public String getProgression(int level, int max) {
        StringBuilder progression = new StringBuilder();
        for (int i = 0; i < max; i++) {
            if (i < level) {
                progression.append("&a&l●");
            } else {
                progression.append("&c&l●");
            }
        }
        return progression.toString();
    }

    public void reopen(Player player) {
        build(player).open(player);
    }
}
