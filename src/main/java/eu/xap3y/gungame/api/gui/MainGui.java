package eu.xap3y.gungame.api.gui;

import eu.xap3y.gungame.GunGame;
import eu.xap3y.xagui.VirtualMenu;
import eu.xap3y.xagui.interfaces.GuiMenuInterface;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class MainGui extends VirtualMenu<String> {

    public MainGui() {
        super("GunGame Main Menu", 3, GunGame.getInstance().getXagui());
    }


    @Override
    public @NotNull GuiMenuInterface build(@NonNull String ctx) {
        GuiMenuInterface gui = getGui();
        gui.fillBorder();
        gui.addCloseButton();
        return gui;
    }
}
