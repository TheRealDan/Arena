package me.therealdan.battlearena.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Icon {

    public static ItemStack build(FileConfiguration data, String path) {
        return build(data, path, false);
    }

    public static ItemStack build(FileConfiguration data, String path, boolean enchant) {
        return build(
                Material.valueOf(data.getString(path + ".Material")),
                data.getInt(path + ".Durability"),
                enchant,
                data.getString(path + ".Name"),
                data.getStringList(path + ".Lore")
        );
    }

    public static ItemStack build(Material material, int durability, boolean enchant, String name, String... description) {
        List<String> lore = new ArrayList<>();
        for (String line : description)
            if (line != null)
                lore.add(line);

        return build(material, durability, enchant, name, lore);
    }

    public static ItemStack build(Material material, int durability, boolean enchant, String name, List<String> description) {
        List<String> lore = new ArrayList<>();
        for (String line : description)
            if (line != null)
                lore.add(ChatColor.translateAlternateColorCodes('&', line));

        ItemStack icon = new ItemStack(material);
        ItemMeta itemMeta = icon.getItemMeta();
        itemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        itemMeta.setLore(lore);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        icon.setItemMeta(itemMeta);
        icon.setDurability((short) durability);
        if (enchant) icon.addEnchantment(Enchantment.DURABILITY, 1);

        return icon;
    }
}