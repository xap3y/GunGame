package eu.xap3y.gungame.util;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XItemFlag;
import com.cryptomorin.xseries.XMaterial;
import eu.xap3y.xagui.adapter.ParseUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ItemBuilder {
    private XMaterial material = XMaterial.STONE_SWORD;
    private int amount = 1;
    private String displayName;
    private final List<String> lore = new ArrayList<>();
    private final List<XItemFlag> flags = new ArrayList<>();
    private boolean unbreakable = true;
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

    public ItemBuilder withFlag(XItemFlag... flags) {
        this.flags.addAll(Arrays.asList(flags));
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    public ItemBuilder withEnchant(XEnchantment enchantment, int level) {
        enchants.add(new EnchantSpec(enchantment, level, true));
        return this;
    }

    public ItemBuilder withEnchant(XEnchantment enchantment, int level, boolean unsafe) {
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
            item = new ItemStack(XMaterial.STONE.or(XMaterial.BARRIER).get());
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
                List<ItemFlag> flagList = new ArrayList<>();
                flags.forEach((flag) -> {
                    ItemFlag flagParsed = flag.get();
                    if (flagParsed == null) return;
                    flagList.add(flagParsed);
                });
                ItemFlag[] flagArray = flagList.toArray(new ItemFlag[0]);
                meta.addItemFlags(flagArray);
            }
            try {
                meta.setUnbreakable(unbreakable);
            } catch (NoSuchMethodError ignored) {
                try {
                    String obc = Bukkit.getServer().getClass().getPackage().getName();
                    String version = obc.substring(obc.lastIndexOf('.') + 1);

                    Class<?> craftItemStack = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack");
                    Method asNMSCopy = craftItemStack.getMethod("asNMSCopy", ItemStack.class);
                    Method asBukkitCopy = craftItemStack.getMethod("asBukkitCopy", Class.forName("net.minecraft.server." + version + ".ItemStack"));

                    Object nmsStack = asNMSCopy.invoke(null, item);

                    Class<?> nmsItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
                    Method getTag = nmsItemStackClass.getMethod("getTag");
                    Method setTag = nmsItemStackClass.getMethod("setTag", Class.forName("net.minecraft.server." + version + ".NBTTagCompound"));

                    Object tag = getTag.invoke(nmsStack);
                    if (tag == null) {
                        tag = Class.forName("net.minecraft.server." + version + ".NBTTagCompound").getConstructor().newInstance();
                    }

                    Method setBoolean = tag.getClass().getMethod("setBoolean", String.class, boolean.class);
                    setBoolean.invoke(tag, "Unbreakable", unbreakable);
                    setTag.invoke(nmsStack, tag);

                    return (ItemStack) asBukkitCopy.invoke(null, nmsStack);
                } catch (Throwable t) {
                    return item;
                }
            }

            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }
            enchants.forEach(e -> {
                Enchantment ench = e.enchant.get();
                if (ench == null) return;
                meta.addEnchant(ench, e.level, e.unsafe);
            });
            item.setItemMeta(meta);
        }
        return item;
    }

    private record EnchantSpec(XEnchantment enchant, int level, boolean unsafe) {}
}
