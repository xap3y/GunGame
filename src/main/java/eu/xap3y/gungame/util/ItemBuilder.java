package eu.xap3y.gungame.util;

import com.cryptomorin.xseries.XMaterial;
import eu.xap3y.xagui.adapter.ParseUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemBuilder {
    private XMaterial material = XMaterial.STONE_SWORD;
    private int amount = 1;
    private String displayName;
    private final List<String> lore = new ArrayList<>();
    private final List<ItemFlag> flags = new ArrayList<>();
    private boolean unbreakable = false;
    private final List<EnchantSpec> enchants = new ArrayList<>();
    private Integer customModelData = null;

    private ItemBuilder(String displayName) {
        this.displayName = displayName;
    }

    private ItemBuilder(XMaterial material) {
        this.material = material;
    }

    public static ItemBuilder create(String displayName) {
        return new ItemBuilder(displayName);
    }

    public static ItemBuilder create(XMaterial material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder withMaterial(XMaterial material) {
        this.material = material;
        return this;
    }

    public ItemBuilder withAmount(int amount) {
        this.amount = Math.max(1, amount);
        return this;
    }

    public ItemBuilder withLore(String... lines) {
        lore.addAll(Arrays.asList(lines));
        return this;
    }

    public ItemBuilder withFlag(ItemFlag... flags) {
        this.flags.addAll(Arrays.asList(flags));
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ItemBuilder withEnchant(Enchantment enchantment, int level) {
        enchants.add(new EnchantSpec(enchantment, level, true));
        return this;
    }

    public ItemBuilder withEnchant(Enchantment enchantment, int level, boolean unsafe) {
        enchants.add(new EnchantSpec(enchantment, level, unsafe));
        return this;
    }

    public ItemBuilder withCustomModelData(Integer customModelData) {
        this.customModelData = customModelData;
        return this;
    }

    public ItemStack build() {
        ItemStack item = material.parseItem();
        if (item == null) {
            item = new ItemStack(Material.STONE);
        }
        item.setAmount(amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (displayName != null) {
                meta.displayName(ParseUtil.parseText(displayName));
            }
            if (!lore.isEmpty()) {
                List<Component> coloredLore = lore.stream().map(ParseUtil::parseText).toList();
                meta.lore(coloredLore);
            }
            if (!flags.isEmpty()) {
                meta.addItemFlags(flags.toArray(new ItemFlag[0]));
            }
            meta.setUnbreakable(unbreakable);
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }
            enchants.forEach(e -> meta.addEnchant(e.enchant, e.level, e.unsafe));
            item.setItemMeta(meta);
        }
        return item;
    }

    private record EnchantSpec(Enchantment enchant, int level, boolean unsafe) {}
}
