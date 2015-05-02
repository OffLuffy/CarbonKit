package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonlib.Messages.Clr;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class CarbonCraftingModule extends Module {
	public CarbonCraftingModule() throws DuplicateModuleException { super("CarbonCrafting", "ccrafting", "carboncraft", "ccraft", "cc"); }
	public static CarbonCraftingModule inst;
	//private static List<Recipe> addedRecipes;
	public void initModule() {
		inst = this;
		//if (addedRecipes == null) addedRecipes = new ArrayList<Recipe>(); else addedRecipes.clear();
		loadRecipes();
		registerListeners();
	}
	public void disableModule() {
		//MiscUtils.removeRecipes(addedRecipes);
		//addedRecipes.clear();
		unregisterListeners();
	}
	public void reloadModule() {
		disableModule();
		CarbonKit.reloadDefConfig();
		CarbonKit.reloadConfig(ConfType.DATA);
		initModule();
	}
	protected boolean needsListeners() { return false; }
	public boolean hasAllDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	private static ItemStack applyData(String path, ItemStack is) {
		ItemStack item = is.clone();
		if (inst.getConfig().contains(path)) {
			ConfigurationSection sect = inst.getConfig().getConfigurationSection(path);
			ItemMeta im = item.getItemMeta();
			item.setAmount(sect.getInt("amount", 1));
			item.setDurability((short)sect.getInt("durability", 0));
			if (sect.contains("name") && !sect.getString("name").isEmpty())
				im.setDisplayName(Clr.trans(sect.getString("name")));
			if (sect.contains("lore") && !sect.getStringList("lore").isEmpty()) {
				List<String> lore = sect.getStringList("lore");
				if (!lore.isEmpty()) {
					List<String> cLore = new ArrayList<String>();
					for (String s : lore) if (!s.isEmpty()) cLore.add(Clr.trans(s));
					im.setLore(cLore);
				}
			}
			if (sect.contains("enchants")) {
				if (!sect.getConfigurationSection("enchants").getKeys(false).isEmpty()){
					HashMap<Enchantment, Integer> enchants = new HashMap<Enchantment, Integer>();
					for (String e : sect.getConfigurationSection("enchants").getKeys(false)) {
						int lvl = sect.getInt("enchants." + e, 1);
						if (MiscUtils.getEnchant(e) != null)
							enchants.put(MiscUtils.getEnchant(e), lvl);
						else
							CarbonKit.log.warn("Invalid enchant: " + e + " for item: " + is.getType().name() + ", skipping enchantment");
					}
					if (enchants != null && enchants.size() > 0)
						for (Enchantment ench : enchants.keySet())
							im.addEnchant(ench, enchants.get(ench), true);
				}
			}
			item.setItemMeta(im);
		}
		return item;
	}

	public static void loadRecipes() {
		ConfigurationSection shaped = inst.getConfig().getConfigurationSection("shaped-recipes");
		ConfigurationSection shapeless = inst.getConfig().getConfigurationSection("shapeless-recipes");
		ConfigurationSection furnace = inst.getConfig().getConfigurationSection("furnace-recipes");
		for (String r : shaped.getKeys(false)) {
			if (MiscUtils.getMaterial(r) != null) {
				ItemStack item = new ItemStack(MiscUtils.getMaterial(r));
				item = applyData("shaped-recipes." + r, item);
				ShapedRecipe recipe = new ShapedRecipe(item);
				char[] chars = new char[]{'A','B','C','D','E','F','G','H','I'};
				Material[] mats = new Material[9];
				String[] rows = new String[3];
				for (int i = 0; i < 3; i++) {
					String row = shaped.getStringList(r + ".recipe").get(i);
					String[] items = row.split(",");
					for (int j = 0; j < items.length; j++)
						mats[i*3+j] = MiscUtils.getMaterial(items[j]);
				}
				for (int c = 0; c < 3; c++) {
					String row = "";
					for (int d = 0; d < 3; d++) {
						if (mats[c*3+d] == null) row += " ";
						else row += chars[c*3+d];
					}
					rows[c] = row;
				}
				recipe.shape(rows);
				for (int e = 0; e < 9; e++)
					if (rows[0].contains(chars[e]+"") || rows[1].contains(chars[e]+"") || rows[2].contains(chars[e]+""))
						recipe.setIngredient(chars[e], mats[e]);
				Bukkit.addRecipe(recipe);
				//addedRecipes.add(recipe);
			} else {
				CarbonKit.log.warn("Invalid item: " + r + ", unable to register shaped recipe");
			}
		}
		for (String r : furnace.getKeys(false)) {
			if (MiscUtils.getMaterial(r) != null) {
				if (furnace.contains(r + ".source") && MiscUtils.getMaterial(furnace.getString(r + ".source")) != null) {
					ItemStack item = new ItemStack(MiscUtils.getMaterial(r));
					Material source = MiscUtils.getMaterial(furnace.getString(r + ".source"));
					item = applyData("furnace-recipes." + r, item);
					FurnaceRecipe recipe = new FurnaceRecipe(item, source);
					Bukkit.addRecipe(recipe);
					//addedRecipes.add(recipe);
				} else {
					CarbonKit.log.warn("Invalid furnace source for item: " + r + ", unable to register furnace recipe");
				}
			} else {
				CarbonKit.log.warn("Invalid item: " + r + ", unable to register furnace recipe");
			}
		}
		nextRecipe:for (String r : shapeless.getKeys(false)) {
			if (MiscUtils.getMaterial(r) != null) {
				ItemStack item = new ItemStack(MiscUtils.getMaterial(r));
				item = applyData("shapeless-recipes." + r, item);
				ShapelessRecipe recipe = new ShapelessRecipe(item);
				if (shapeless.contains(r + ".recipe") && !shapeless.getStringList(r + ".recipe").isEmpty()) {
					int mats = 0;
					for (String s : shapeless.getStringList(r + ".recipe")) {
						String[] ing = s.split(" ");
						int amount = 1, data = 0;
						Material mat;
						if (ing.length == 1) {
							if (MiscUtils.getMaterial(ing[0]) != null)
								mat = MiscUtils.getMaterial(ing[0]);
							else {
								CarbonKit.log.warn("Invalid ingredient: " + ing[0] + " in item: " + r + ", unable to register shapeless recipe");
								continue nextRecipe;
							}
						} else if (ing.length == 2) {
							if (MiscUtils.isInteger(ing[0])) {
								amount = Integer.parseInt(ing[0]);
								if (MiscUtils.getMaterial(ing[1]) != null)
									mat = MiscUtils.getMaterial(ing[1]);
								else {
									CarbonKit.log.warn("Invalid ingrdient: " + s + " in item: " + r + ", unable to register shapeless recipe");
									continue nextRecipe;
								}
							} else if (MiscUtils.getMaterial(ing[0]) != null)
								mat = MiscUtils.getMaterial(ing[0]);
							else {
								CarbonKit.log.warn("Invalid ingrdient: " + s + " in item: " + r + ", unable to register shapeless recipe");
								continue nextRecipe;
							}
							if (mat != null && MiscUtils.isInteger(ing[1]))
								data = Integer.parseInt(ing[1]);
							if (mat == null) {
								CarbonKit.log.warn("Invalid ingrdient: " + s + " in item: " + r + ", unable to register shapeless recipe");
								continue nextRecipe;
							}
						} else if (ing.length == 3) {
							if (MiscUtils.isInteger(ing[0])) {
								amount = Integer.parseInt(ing[0]);
							} else {
								CarbonKit.log.warn("Invalid ingrdient: " + s + " in item: " + r + ", unable to register shapeless recipe");
								continue nextRecipe;
							}
							if (MiscUtils.getMaterial(ing[1]) != null)
								mat = MiscUtils.getMaterial(ing[1]);
							else {
								CarbonKit.log.warn("Invalid ingrdient: " + s + " in item: " + r + ", unable to register shapeless recipe");
								continue nextRecipe;
							}
							if (MiscUtils.isInteger(ing[2]))
								data = Integer.parseInt(ing[2]);
							else {
								CarbonKit.log.warn("Invalid ingrdient: " + s + " in item: " + r + ", unable to register shapeless recipe");
								continue nextRecipe;
							}
						} else {
							CarbonKit.log.warn("Invalid ingrdient: " + s + " in item: " + r + ", unable to register shapeless recipe");
							continue nextRecipe;
						}
						if (amount > 9 || mats + amount > 9) {
							CarbonKit.log.warn("Too many ingredients in item: " + r + " (can have up to 9), unable to register shapeless recipe");
							continue nextRecipe;
						} else mats += amount;
						if (mat == null) {
							CarbonKit.log.warn("Invalid ingrdient: " + s + " in item: " + r + ", unable to register shapeless recipe");
							continue nextRecipe;
						}
						recipe.addIngredient(amount, mat, data);
					}
				} else {
					CarbonKit.log.warn("No ingredients for item: " + r + ", unable to register shapeless recipe");
				}
				Bukkit.addRecipe(recipe);
				//addedRecipes.add(recipe);
			} else {
				CarbonKit.log.warn("Invalid item: " + r + ", unable to register shapeless recipe");
			}
		}
	}
}
