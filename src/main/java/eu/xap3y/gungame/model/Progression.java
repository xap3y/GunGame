package eu.xap3y.gungame.model;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.service.GunGameYamlLoader;
import eu.xap3y.gungame.util.ItemBuilder;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Builds the progression as a list of loadouts (one list per step).
 *
 * Changes:
 *  - Uses GunGameYamlLoader.loadSteps() to obtain stepNumber -> items mapping.
 *  - Applies each step's items to a currentLoadout map (slot -> ItemStack).
 *  - Produces deterministic ordered lists per step (HELMET, CHESTPLATE, LEGGINGS, BOOTS, OFFHAND, WEAPON).
 *
 * Note: The loader and progression currently support a single "WEAPON" entry. If you want
 * multiple weapons per step, we can change the slot-keying strategy.
 */
public class Progression {

    @Getter
    private final List<List<ItemStack>> steps;

    public Progression() {
        GunGame.getTexter().logPos();
        this.steps = buildProgression();
    }

    public int getNextLevel(int before, int amount) {
        int next = before + amount;
        return Math.min(next, maxIndex());
    }

    private List<List<ItemStack>> buildProgression() {
        GunGame.getTexter().logPos();
        List<List<ItemStack>> finalSteps = new ArrayList<>();

        // a deterministic map of current equipment by slot
        Map<String, ItemStack> currentLoadout = new HashMap<>();

        // Load steps from YAML (sorted numeric keys)
        LinkedHashMap<Integer, ItemStack[]> parsed = GunGameYamlLoader.loadSteps();

        // process in numeric order (parsed is a LinkedHashMap with keys in sorted order)
        for (Map.Entry<Integer, ItemStack[]> entry : parsed.entrySet()) {
            ItemStack[] items = entry.getValue();
            // apply each ItemStack to the currentLoadout map by slot
            for (ItemStack item : items) {
                String slot = getSlotType(item);
                // For weapons we keep the newest weapon (overwrites previous weapon)
                // If you want to keep multiple weapons, change the keying scheme.
                currentLoadout.put(slot, item);
            }
            // add a deterministic ordered snapshot of the currentLoadout
            finalSteps.add(buildOrderedLoadoutList(currentLoadout));
        }

        return finalSteps;
    }

    private List<ItemStack> buildOrderedLoadoutList(Map<String, ItemStack> currentLoadout) {
        List<ItemStack> ordered = new ArrayList<>();

        // fixed order for presentation/consistency
        String[] order = new String[] { "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS", "OFFHAND", "WEAPON" };

        for (String slot : order) {
            ItemStack it = currentLoadout.get(slot);
            if (it != null) ordered.add(it);
        }

        return ordered;
    }

    private String getSlotType(ItemStack item) {
        String type = item.getType().name();
        if (type.endsWith("_HELMET")) return "HELMET";
        if (type.endsWith("_CHESTPLATE")) return "CHESTPLATE";
        if (type.endsWith("_LEGGINGS")) return "LEGGINGS";
        if (type.endsWith("_BOOTS")) return "BOOTS";
        if (type.equals("SHIELD")) return "OFFHAND";
        return "WEAPON";
    }

    public int maxIndex() {
        return steps.size() - 1;
    }

    public List<ItemStack> itemsForLevel(int level) {
        int clamped = Math.max(0, Math.min(level, maxIndex()));
        if (steps.isEmpty() || clamped >= steps.size()) {
            return Collections.emptyList();
        }
        return steps.get(clamped);
    }

    public Set<Material> managedMaterials() {
        return steps.stream()
                .flatMap(List::stream)
                .map(ItemStack::getType)
                .collect(Collectors.toSet());
    }
}