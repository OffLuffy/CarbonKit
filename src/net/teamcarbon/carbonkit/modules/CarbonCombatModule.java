package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class CarbonCombatModule extends Module {
	public CarbonCombatModule() throws DuplicateModuleException { super("CarbonCombat", "ccombat", "combat", "carbonpvp", "cpvp", "pvp"); }
	private static HashMap<Player, Long> pvpEnabled;
	public void initModule() {
		if (pvpEnabled != null) pvpEnabled.clear(); else pvpEnabled = new HashMap<Player, Long>();
		registerListeners();
	}
	public void disableModule() {
		unregisterListeners();
	}
	public void reloadModule() {
		disableModule();
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

	public static boolean isPvpEnabled(Player p) { return pvpEnabled.containsKey(p); }

	public static long getPvpEnabledTime(Player p) { return isPvpEnabled(p)?pvpEnabled.get(p):-1; }

	public static void setPvpEnabled(Player p, boolean enabled) {
		if (isPvpEnabled(p) && !enabled) { pvpEnabled.remove(p); }
		else if (!isPvpEnabled(p) && enabled) { pvpEnabled.put(p, System.currentTimeMillis()); }
	}

	public static void togglePvpEnabled(Player p) { setPvpEnabled(p, !isPvpEnabled(p));}
}
