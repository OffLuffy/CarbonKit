package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.AnvilInventory;

import java.util.Set;

@SuppressWarnings("UnusedDeclaration")
public class AnvilEnchants extends Module {
	public AnvilEnchants() throws DuplicateModuleException { super("AnvilEnchants", "aenchants", "anvilench", "aench", "ae"); }
	public void initModule() {
		registerListeners();
	}
	public void disableModule() {
		unregisterListeners();
	}
	public void reloadModule() {
		disableModule();
		initModule();
	}
	protected boolean needsListeners() { return true; }
	public boolean hasAllDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler
	public void inventoryOpen(InventoryOpenEvent e) {
		if (e.getPlayer() instanceof Player && e.getInventory() instanceof AnvilInventory) {
			//e.setCancelled(true);
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	private static boolean conflictsWithAny(Enchantment e, Set<Enchantment> enchs) {
		for (Enchantment ench : enchs) { if (ench.conflictsWith(e)) return false; } return true;
	}
}
