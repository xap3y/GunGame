package eu.xap3y.gungame.api.gui;

import com.cryptomorin.xseries.XMaterial;
import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.database.dto.PlayerStatsDto;
import eu.xap3y.xagui.VirtualMenu;
import eu.xap3y.xagui.interfaces.GuiMenuInterface;
import eu.xap3y.xagui.models.GuiButton;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class StatsGui extends VirtualMenu<Player> {

    public StatsGui() {
        super("&6GunGame Stats", 3, GunGame.getInstance().getXagui());
    }

    @Override
    public @NonNull GuiMenuInterface build(@NonNull Player ctx) {
        GuiMenuInterface gui = getGui();

        gui.fillBorder();
        gui.addCloseButton();

        PlayerStatsDto stats = GunGame.getInstance().getDatabaseManager().getPlayerDao().getPlayerCache().get(ctx.getUniqueId());
        if (stats == null) {
            gui.setSlot(13, new GuiButton(XMaterial.BARRIER.get()).setName("&4ERROR").setLore("&cMAP EMPTY"));
            return gui;
        }

        GuiButton kills = new GuiButton(XMaterial.IRON_SWORD.get())
                .setName("&eKills: &6" + stats.getKills());

        GuiButton deaths = new GuiButton(XMaterial.REDSTONE.get())
                .setName("&eDeaths: &6" + stats.getDeaths());

        GuiButton bestStage = new GuiButton(XMaterial.DIAMOND.get())
                .setName("&eBest Stage: &6" + stats.getBestStage());

        GuiButton topKillStreak = new GuiButton(XMaterial.GOLDEN_APPLE.get())
                .setName("&eTop Killstreak: &6" + stats.getBestKillStreak());

        gui .setSlot(10, kills);
        gui.setSlot(12, deaths);
        gui.setSlot(14, bestStage);
        gui.setSlot(16, topKillStreak);

        return gui;
    }
}
