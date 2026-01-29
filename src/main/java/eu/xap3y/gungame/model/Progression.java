package eu.xap3y.gungame.model;

import com.cryptomorin.xseries.XMaterial;
import eu.xap3y.gungame.util.ItemBuilder;
import eu.xap3y.gungame.util.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Progression {
    private final List<List<ItemStack>> steps;

    public Progression() {

        // 0

        this.steps = List.of(Collections.singletonList(XMaterial.WOODEN_AXE.parseItem()),

                // 1
                Collections.singletonList(XMaterial.WOODEN_SWORD.parseItem()),

                // 2
                new ArrayList<>() {{
                    add(XMaterial.LEATHER_HELMET.parseItem());
                    add(XMaterial.WOODEN_SWORD.parseItem());
                }},

                // 3
                new ArrayList<>() {{
                    add(XMaterial.LEATHER_HELMET.parseItem());
                    add(XMaterial.LEATHER_BOOTS.parseItem());
                    add(XMaterial.WOODEN_SWORD.parseItem());
                }},

                // 4
                new ArrayList<>() {{
                    add(XMaterial.LEATHER_HELMET.parseItem());
                    add(XMaterial.LEATHER_BOOTS.parseItem());
                    add(XMaterial.LEATHER_LEGGINGS.parseItem());
                    add(XMaterial.WOODEN_SWORD.parseItem());
                }},

                // 5
                new ArrayList<>() {{
                    add(XMaterial.LEATHER_HELMET.parseItem());
                    add(XMaterial.LEATHER_BOOTS.parseItem());
                    add(XMaterial.LEATHER_CHESTPLATE.parseItem());
                    add(XMaterial.LEATHER_LEGGINGS.parseItem());
                    add(XMaterial.WOODEN_SWORD.parseItem());
                }},

                // 6
                new ArrayList<>() {{
                    add(XMaterial.LEATHER_HELMET.parseItem());
                    add(XMaterial.LEATHER_BOOTS.parseItem());
                    add(XMaterial.LEATHER_CHESTPLATE.parseItem());
                    add(XMaterial.LEATHER_LEGGINGS.parseItem());
                    add(ItemBuilder.create(XMaterial.WOODEN_AXE).withEnchant(Utils.getLegacyEnchantmentByName("SHARPNESS", "DAMAGE_ALL"), 1).build());
                }},

                // 7
                new ArrayList<>() {{
                    add(XMaterial.LEATHER_HELMET.parseItem());
                    add(XMaterial.LEATHER_BOOTS.parseItem());
                    add(XMaterial.LEATHER_CHESTPLATE.parseItem());
                    add(XMaterial.LEATHER_LEGGINGS.parseItem());
                    add(ItemBuilder.create(XMaterial.WOODEN_AXE).withEnchant(Utils.getLegacyEnchantmentByName("SHARPNESS", "DAMAGE_ALL"), 2).build());
                }});
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