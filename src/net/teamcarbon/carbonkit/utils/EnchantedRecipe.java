package net.teamcarbon.carbonkit.utils;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Recipe;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class EnchantedRecipe {
	HashMap<Enchantment, Integer> enchants;
	Recipe recipe;
	public EnchantedRecipe(Recipe recipe, HashMap<Enchantment, Integer> enchants) {
		this.recipe = recipe;
		this.enchants = enchants;
	}
	public Recipe getRecipe() { return recipe; }
	public HashMap<Enchantment, Integer> getEnchants() { return enchants; }
}
