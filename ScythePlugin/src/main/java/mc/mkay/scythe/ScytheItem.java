package mc.mkay.scythe;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ScytheItem {

    // Custom model data matching the scythe texture pack
    // Uses MACE base (1.21 mace item) but with sword attack cooldown via attribute override
    public static final int CUSTOM_MODEL_DATA = 100001;
    public static final String NBT_KEY = "mkay_scythe";

    public static ItemStack build(org.bukkit.plugin.Plugin plugin) {
        // Using MACE as the base item — matches your texture (mace + sword scythe combo)
        ItemStack scythe = new ItemStack(Material.MACE);
        ItemMeta meta = scythe.getItemMeta();

        // Name — purple cherry blossom style
        meta.displayName(
            Component.text("✿ ", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text("Bloom's Scythe", NamedTextColor.DARK_PURPLE)
                    .decoration(TextDecoration.BOLD, true)
                    .decoration(TextDecoration.ITALIC, false))
                .append(Component.text(" ✿", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false))
        );

        // Lore — thematic, cherry blossom SMP vibes
        meta.lore(List.of(
            Component.empty(),
            Component.text("  The relic chose only one.", NamedTextColor.DARK_PURPLE)
                .decoration(TextDecoration.ITALIC, true),
            Component.text("  Petals fall where others don't.", NamedTextColor.LIGHT_PURPLE)
                .decoration(TextDecoration.ITALIC, true),
            Component.empty(),
            Component.text(" ⚔ ", NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text("Right-Click", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true))
                .append(Component.text(" → Blossom Burst", NamedTextColor.DARK_PURPLE)
                    .decoration(TextDecoration.ITALIC, false)),
            Component.empty(),
            Component.text(" Bound to: ", NamedTextColor.DARK_GRAY)
                .decoration(TextDecoration.ITALIC, false)
                .append(Component.text("mkaymc", NamedTextColor.LIGHT_PURPLE)
                    .decoration(TextDecoration.ITALIC, false)
                    .decoration(TextDecoration.BOLD, true))
        ));

        // Custom model data — point this to your scythe texture
        meta.setCustomModelData(CUSTOM_MODEL_DATA);

        // Mark it as the scythe via PDC so we can detect it in events
        meta.getPersistentDataContainer().set(
            new NamespacedKey(plugin, NBT_KEY),
            PersistentDataType.BOOLEAN,
            true
        );

        scythe.setItemMeta(meta);

        // ── Enchantments ──────────────────────────────────────────
        // Sword enchants only — Sharpness (not Smite), Fire Aspect,
        // Sweeping Edge, Looting, Unbreaking, Mending
        scythe.addUnsafeEnchantment(Enchantment.SHARPNESS,       5);
        scythe.addUnsafeEnchantment(Enchantment.FIRE_ASPECT,     2);
        scythe.addUnsafeEnchantment(Enchantment.SWEEPING_EDGE,   3);
        scythe.addUnsafeEnchantment(Enchantment.LOOTING,         3);
        scythe.addUnsafeEnchantment(Enchantment.UNBREAKING,      3);
        scythe.addUnsafeEnchantment(Enchantment.MENDING,         1);

        return scythe;
    }

    /** Check if an ItemStack is the Scythe */
    public static boolean isScythe(ItemStack item, org.bukkit.plugin.Plugin plugin) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
            .has(new NamespacedKey(plugin, NBT_KEY), PersistentDataType.BOOLEAN);
    }
}
