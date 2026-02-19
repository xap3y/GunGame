package eu.xap3y.gungame.model;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import eu.xap3y.gungame.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

// TODO: more optimized, dont rebuild List every tier, check differences/apply only changes
public class Progression {
    private final List<List<ItemStack>> steps;

    public Progression() {
        this.steps = buildProgression();
    }

    private List<List<ItemStack>> buildProgression() {
        List<List<ItemStack>> finalSteps = new ArrayList<>();

        Map<String, ItemStack> currentLoadout = new HashMap<>();

        java.util.function.Consumer<ItemStack[]> addStep = (items) -> {
            for (ItemStack item : items) {
                String slot = getSlotType(item);
                currentLoadout.put(slot, item);
            }
            finalSteps.add(new ArrayList<>(currentLoadout.values()));
        };

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.WOODEN_AXE).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.WOODEN_SWORD).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.LEATHER_HELMET).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.LEATHER_BOOTS).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.LEATHER_LEGGINGS).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.LEATHER_CHESTPLATE).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.WOODEN_AXE).withEnchant(XEnchantment.SHARPNESS, 1).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.LEATHER_HELMET).withEnchant(XEnchantment.PROTECTION, 1).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.LEATHER_BOOTS).withEnchant(XEnchantment.PROTECTION, 1).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.LEATHER_LEGGINGS).withEnchant(XEnchantment.PROTECTION, 1).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.LEATHER_CHESTPLATE).withEnchant(XEnchantment.PROTECTION, 1).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.WOODEN_SWORD).withEnchant(XEnchantment.SHARPNESS, 1).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.GOLDEN_HELMET).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.GOLDEN_BOOTS).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.GOLDEN_LEGGINGS).build()
        });

        addStep.accept(new ItemStack[] {
                ItemBuilder.create(XMaterial.GOLDEN_CHESTPLATE).build()
        });

        return finalSteps;
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
        return steps.get(clamped);
    }

    public Set<Material> managedMaterials() {
        return steps.stream()
                .flatMap(List::stream)
                .map(ItemStack::getType)
                .collect(Collectors.toSet());
    }
}