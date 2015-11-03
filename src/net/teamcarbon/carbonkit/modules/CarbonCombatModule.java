package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class CarbonCombatModule extends Module {
	public CarbonCombatModule() throws DuplicateModuleException { super("CarbonCombat", "ccombat", "combat", "carbonpvp", "cpvp", "pvp"); }
	private static HashMap<Player, Long> pvpEnabled = new HashMap<Player, Long>();
	public void initModule() {
		if (!pvpEnabled.isEmpty()) pvpEnabled.clear();
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
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler(ignoreCancelled = true)
	public void entityDamageEntity(EntityDamageByEntityEvent e) {
		if (!isEnabled()) return;
		Entity target = e.getEntity(), source = e.getDamager();
		if (!(target instanceof Player)) return;
		if (!(source instanceof Player)) {
			if (source instanceof Projectile) {
				ProjectileSource ps = ((Projectile) source).getShooter();
				if (ps instanceof Player) {
					source = (Entity) ps;
				}
			}
		} else return;
		Player tp = (Player) target, sp = (Player) source;
	}
	
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
