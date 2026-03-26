package eu.xap3y.gungame.api.gui;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XPotion;
import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.particles.XParticle;
import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.service.PotionService;
import eu.xap3y.xagui.VirtualMenu;
import eu.xap3y.xagui.interfaces.GuiMenuInterface;
import eu.xap3y.xagui.models.GuiButton;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ShopGui extends VirtualMenu<String> {

    public ShopGui() {
        super("&bGunGame Shop", 3, GunGame.getInstance().getXagui());
    }

    @Override
    public @NotNull GuiMenuInterface build(@NonNull String ctx) {

        GuiMenuInterface gui = getGui();

        AtomicBoolean open = new AtomicBoolean(true);

        gui.fillBorder();
        gui.addCloseButton();
        //gui.setOpenSound(XSound.BLOCK_ENDER_CHEST_OPEN.or(XSound.BLOCK_CHEST_OPEN).get(), .6f);

        FileConfiguration cfg = GunGame.getInstance().getConfig();

        /*GuiButton buyKillEffects = new GuiButton(XMaterial.BLAZE_POWDER.get())
                .setName(cfg.getString("shop.items.kill-effects.name", "&eKill Effects"))
                .setLoreList(cfg.getStringList("shop.items.kill-effects.lore"));

        gui.setSlot(14, buyKillEffects);*/

        gui.setSlot(14, XMaterial.BARRIER.get());

        GuiButton buyUpgrades = new GuiButton(XMaterial.GUNPOWDER)
                .setName(cfg.getString("shop.items.upgrades.name", "&eKill Effects"))
                .setLoreList(cfg.getStringList("shop.items.upgrades.lore"))
                .withListener(e -> {
                    if (!open.get()) {
                        return;
                    }
                    if (!GunGame.getInstance().getArenaManager().getCurrentArena().isInSafeZone(e.getPlayer().getLocation())) {
                        XSound.ENTITY_VILLAGER_NO.play(e.getPlayer(), .6f, 1f);
                        GunGame.getTexter().responseLang(e.getPlayer(), "not-in-safezone");
                        gui.close(e.getPlayer());
                        return;
                    }
                    XSound.UI_BUTTON_CLICK.play(e.getPlayer(), .6f, 1f);
                    open.set(false);
                    new UpgradesShopGui().build(e.getPlayer()).open(e.getPlayer());
                });

        gui.setSlot(16, buyUpgrades);

        GuiButton speed = new GuiButton(XMaterial.FEATHER)
                .setName(cfg.getString("shop.items.speed.name", "&eSpeed"))
                .setLoreList(cfg.getStringList("shop.items.speed.lore"))
                .withListener(e -> {;
                    if (!open.get()) {
                        return;
                    }
                    open.set(false);
                    gui.close(e.getPlayer());
                    if (!GunGame.getInstance().getArenaManager().getCurrentArena().isInSafeZone(e.getPlayer().getLocation())) {
                        XSound.ENTITY_VILLAGER_NO.play(e.getPlayer(), .6f, 1f);
                        GunGame.getTexter().responseLang(e.getPlayer(), "not-in-safezone");
                        return;
                    }
                    int price = cfg.getInt("shop.items.speed.price", 1000);
                    if (!checkMoney(price, e.getPlayer())) {
                        return;
                    } else if (PotionService.getInstance().hasEffect(XPotion.SPEED.get(), e.getPlayer().getUniqueId())) {
                        GunGame.getTexter().responseLang(e.getPlayer(), "already-have-speed");
                        XSound.ENTITY_VILLAGER_NO.play(e.getPlayer(), .6f, 1f);
                        return;
                    }

                    GunGame.getEcon().withdrawPlayer(e.getPlayer(), price);
                    int duration = cfg.getInt("shop.items.speed.duration", 180);
                    PotionService.getInstance().addEffect(e.getPlayer(), XPotion.SPEED.buildPotionEffect(20 * duration, 1), duration);
                    XSound.ENTITY_ITEM_PICKUP.play(e.getPlayer(), .6f, 1f);
                    gui.close(e.getPlayer());
                    GunGame.getBoardApi().updateBoard(e.getPlayer().getUniqueId());
                });

        gui.setSlot(12, speed);

        GuiButton snowball = new GuiButton(XMaterial.SNOWBALL)
                .setName(cfg.getString("shop.items.snowball.name", "&eSpeed"))
                .setLoreList(cfg.getStringList("shop.items.snowball.lore"))
                .withListener(e -> {
                    if (!open.get()) {
                        return;
                    }
                    open.set(false);
                    gui.close(e.getPlayer());
                    if (!GunGame.getInstance().getArenaManager().getCurrentArena().isInSafeZone(e.getPlayer().getLocation())) {
                        XSound.ENTITY_VILLAGER_NO.play(e.getPlayer(), .6f, 1f);
                        GunGame.getTexter().responseLang(e.getPlayer(), "not-in-safezone");
                        return;
                    }
                    int price = cfg.getInt("shop.items.snowball.price", 300);
                    if (!checkMoney(price, e.getPlayer())) {
                        return;
                    }

                    GunGame.getEcon().withdrawPlayer(e.getPlayer(), price);
                    e.getPlayer().getInventory().addItem(XMaterial.SNOWBALL.parseItem());
                    XSound.ENTITY_ITEM_PICKUP.play(e.getPlayer(), .6f, 1f);
                    GunGame.getBoardApi().updateBoard(e.getPlayer().getUniqueId());
                });

        gui.setSlot(10, snowball);

        return gui;
    }

    private static boolean checkMoney(int price, Player p0) {
        double bal = GunGame.getEcon().getBalance(p0);
        if (bal < price) {
            int required = price - (int) bal;
            GunGame.getTexter().responseLang(p0, "not-enough-money", "{price}", Map.of("price", price + "", "missing", required + ""));
            XSound.ENTITY_VILLAGER_NO.play(p0, .6f, 1f);
            return false;
        }
        return true;
    }
}
