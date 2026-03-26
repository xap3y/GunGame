package eu.xap3y.gungame.service;

import com.cryptomorin.xseries.XEnchantment;
import com.cryptomorin.xseries.XMaterial;
import eu.xap3y.gungame.GunGame;
import eu.xap3y.gungame.util.ItemBuilder;
import eu.xap3y.gungame.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Level;

/**
 * Loads gungame steps from the plugin config (getConfig()).
 *
 * Public APIs:
 *  - Map<Integer, ItemStack[]> loadSteps()                // returns parsed steps keyed by numeric step
 *  - void registerSteps(BiConsumer<Integer, ItemStack[]> registerStep)
 *
 * Parsing rules:
 *  - Each step under "steps" is a YAML list of item-spec strings
 *  - item-spec format: MATERIAL[;ENCHANT[-LEVEL]]*
 *  - LEVEL defaults to 1 when omitted or unparsable
 *  - Material lookup: XMaterial.matchXMaterial(String)
 *  - Enchant lookup: XEnchantment.of(String)
 */
public final class GunGameYamlLoader {

    private GunGameYamlLoader() {}

    /**
     * Parse the plugin config and return a sorted map of stepNumber -> ItemStack[].
     *
     * Throws IllegalArgumentException if the "steps" section is missing.
     */
    public static LinkedHashMap<Integer, ItemStack[]> loadSteps() {

        GunGame.getTexter().debugLog("GunGameYamlLoader.loadSteps()");
        FileConfiguration cfg = GunGame.getInstance().getConfig();
        ConfigurationSection stepsSection = cfg.getConfigurationSection("steps");
        if (stepsSection == null) {
            throw new IllegalArgumentException("Missing 'steps' section in plugin config");
        }

        // Collect keys and sort numerically when possible
        List<String> rawKeys = new ArrayList<>(stepsSection.getKeys(false));
        rawKeys.sort((a, b) -> {
            Integer ia = tryParseInt(a);
            Integer ib = tryParseInt(b);
            if (ia != null && ib != null) return ia.compareTo(ib);
            if (ia != null) return -1;
            if (ib != null) return 1;
            return a.compareTo(b);
        });

        LinkedHashMap<Integer, ItemStack[]> result = new LinkedHashMap<>();
        for (String rawKey : rawKeys) {
            Integer stepNum = tryParseInt(rawKey);
            if (stepNum == null) {
                Bukkit.getLogger().log(Level.WARNING, "[GunGame] Skipping non-numeric step key: {0}", rawKey);
                continue;
            }

            GunGame.getTexter().console("[c] Processing step " + stepNum + " with raw key '" + rawKey + "'");

            // read the list under the key (supports both lists and single string)
            List<String> itemLines = stepsSection.getStringList(rawKey);
            if (itemLines.isEmpty()) {
                GunGame.getTexter().console("[c] No list found for step " + stepNum + ", trying single string");
                String single = stepsSection.getString(rawKey);
                if (single != null && !single.trim().isEmpty()) {
                    itemLines = Collections.singletonList(single.trim());
                }
            }

            if (itemLines == null || itemLines.isEmpty()) {
                Bukkit.getLogger().log(Level.INFO, "[GunGame] Step {0} has no items (skipping)", stepNum);
                continue;
            }

            List<ItemStack> parsed = new ArrayList<>();
            for (String rawLine : itemLines) {
                if (rawLine == null) continue;
                String line = rawLine.trim();
                if (line.isEmpty()) continue;

                try {
                    Optional<ItemStack> maybe = parseItemLine(line);
                    if (maybe.isPresent()) {
                        GunGame.getTexter().debugLog("[" + stepNum + "] Parsed-item: " + maybe.get().getType() + " | + " + maybe.get().getEnchantments().size() + " ench size");
                        GunGame.getTexter().console("[c] Parsed item for step " + stepNum + ": " + maybe.get().getType() + " with " + maybe.get().getEnchantments().size() + " enchantments");
                        parsed.add(Utils.removeAttributes(maybe.get()));
                    } else {
                        GunGame.getTexter().console("[c] Skipped invalid item-spec in step " + stepNum + ": '" + line + "'");
                        Bukkit.getLogger().log(Level.WARNING, "[GunGame] Skipped unknown item-spec '{0}' in step {1}", new Object[]{line, stepNum});
                    }
                } catch (Exception ex) {
                    Bukkit.getLogger().log(Level.WARNING, "[GunGame] Error parsing item-spec '{0}' in step {1}: {2}", new Object[]{line, stepNum, ex.getMessage()});
                }
            }

            if (!parsed.isEmpty()) {
                result.put(stepNum, parsed.toArray(new ItemStack[0]));
            } else {
                Bukkit.getLogger().log(Level.INFO, "[GunGame] Step {0} produced no valid items, skipped", stepNum);
            }
        }

        return result;
    }

    /**
     * Convenience: register steps by calling the provided BiConsumer with stepNumber and ItemStack[].
     */
    public static void registerSteps(BiConsumer<Integer, ItemStack[]> registerStep) {
        LinkedHashMap<Integer, ItemStack[]> steps = loadSteps();
        for (Map.Entry<Integer, ItemStack[]> e : steps.entrySet()) {
            registerStep.accept(e.getKey(), e.getValue());
        }
    }

    /**
     * Legacy compatibility: call loadSteps() and call addStep.accept(...) in increasing order.
     * Kept for code that expects Consumer<ItemStack[]>.
     *
     * Note: using this Consumer directly can cause "cumulative" behavior if the consumer
     * mutates a shared structure incorrectly. Prefer registerSteps(BiConsumer).
     */
    public static void loadFromFile(java.util.function.Consumer<ItemStack[]> addStep) {
        LinkedHashMap<Integer, ItemStack[]> steps = loadSteps();
        for (ItemStack[] items : steps.values()) {
            addStep.accept(items);
        }
    }

    // -------- parsing helpers --------

    /**
     * Parse a single item-spec line into an ItemStack.
     * Format: MATERIAL[;ENCHANT[-LEVEL]]*
     */
    private static Optional<ItemStack> parseItemLine(String line) {
        String[] parts = line.split(";");
        if (parts.length == 0) return Optional.empty();

        String materialName = parts[0].trim();
        if (materialName.isEmpty()) return Optional.empty();

        Optional<XMaterial> matOpt = XMaterial.matchXMaterial(materialName);
        if (!matOpt.isPresent()) {
            matOpt = XMaterial.matchXMaterial(materialName.toUpperCase(Locale.ROOT));
        }
        if (!matOpt.isPresent()) {
            Bukkit.getLogger().log(Level.WARNING, "[GunGame] Material not found: {0}", materialName);
            return Optional.empty();
        }

        XMaterial xm = matOpt.get();
        GunGame.getTexter().console("[c] Parsed material " + xm.name() + " from '" + materialName + "' in line: " + line);

        ItemBuilder builder = ItemBuilder.create(xm);

        for (int i = 1; i < parts.length; i++) {
            String enchPart = parts[i].trim();
            if (enchPart.isEmpty()) continue;

            String enchName;
            int level = 1;
            int dashIdx = enchPart.lastIndexOf('-');
            if (dashIdx > 0 && dashIdx < enchPart.length() - 1) {
                enchName = enchPart.substring(0, dashIdx).trim();
                String levelStr = enchPart.substring(dashIdx + 1).trim();
                try {
                    level = Integer.parseInt(levelStr);
                } catch (NumberFormatException ex) {
                    level = 1;
                }
            } else {
                enchName = enchPart;
            }

            Optional<XEnchantment> enchOpt = XEnchantment.of(enchName);
            if (!enchOpt.isPresent()) {
                enchOpt = XEnchantment.of(enchName.toUpperCase(Locale.ROOT));
            }
            if (!enchOpt.isPresent()) {
                Bukkit.getLogger().log(Level.WARNING, "[GunGame] Unknown enchantment '{0}' for item '{1}' - skipping", new Object[]{enchName, line});
                continue;
            }

            builder = builder.withEnchant(enchOpt.get(), level);
        }

        return Optional.of(builder.build());
    }

    private static Integer tryParseInt(String s) {
        if (s == null) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            // YAML keys can be quoted strings like '1' — try trimming quotes
            String trimmed = s.trim();
            if (trimmed.length() >= 2 && ((trimmed.startsWith("'") && trimmed.endsWith("'")) || (trimmed.startsWith("\"") && trimmed.endsWith("\"")))) {
                try {
                    return Integer.parseInt(trimmed.substring(1, trimmed.length() - 1));
                } catch (NumberFormatException ignored) {}
            }
            return null;
        }
    }
}