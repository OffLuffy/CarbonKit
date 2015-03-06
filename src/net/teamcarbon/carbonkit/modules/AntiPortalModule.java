package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.PortalType;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.MiscUtils;

import java.util.HashMap;
import java.util.UUID;

@SuppressWarnings("UnusedDeclaration")
public class AntiPortalModule extends Module { // TODO Potentially expand to manage Nether portals
	public AntiPortalModule() throws DuplicateModuleException { super("AntiPortal", "aportal", "ap"); }
	private static HashMap<Player, UUID> protItems;
	private static HashMap<Player, Long> protTimes, msgCooldowns;
	private static HashMap<Player, UUID> killTracker;
	public void initModule() {
		if (protItems == null) protItems = new HashMap<Player, UUID>(); else protItems.clear();
		if (protTimes == null) protTimes = new HashMap<Player, Long>(); else protTimes.clear();
		if (killTracker == null) killTracker = new HashMap<Player, UUID>(); else killTracker.clear();
		if (msgCooldowns == null) msgCooldowns = new HashMap<Player, Long>(); else msgCooldowns.clear();
		registerListeners();
	}
	public void disableModule() {
		protItems.clear();
		protTimes.clear();
		killTracker.clear();
		msgCooldowns.clear();
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
	public void playerQuit(PlayerQuitEvent e) {
		if (!isEnabled()) return;
		if (msgCooldowns.containsKey(e.getPlayer()))
			msgCooldowns.remove(e.getPlayer());
		if (protItems.containsKey(e.getPlayer()))
			protItems.remove(e.getPlayer());
		if (protTimes.containsKey(e.getPlayer()))
			protTimes.remove(e.getPlayer());
	}
	@EventHandler
	public void entityDeath(EntityDeathEvent e) {
		if (!isEnabled()) return;
		if (e.getEntity().getType().equals(EntityType.ENDER_DRAGON)) {
			if (getConfig().getBoolean("protect-egg", true)) {
				if (getConfig().getInt("egg-protect-seconds", 30) > 0) {
					if (e.getEntity().getLastDamageCause().getCause().equals(DamageCause.ENTITY_ATTACK)) {
						Entity damager = ((EntityDamageByEntityEvent)e.getEntity().getLastDamageCause()).getDamager();
						if (damager.getType().equals(EntityType.PLAYER))
							killTracker.put((Player)damager, e.getEntity().getUniqueId());
						else if (damager.getType().equals(EntityType.ARROW) && ((Arrow)damager).getShooter() instanceof Player)
							killTracker.put(((Player)(((Arrow)damager).getShooter())), e.getEntity().getUniqueId());
					}
				}
			}
		}
	}
	@EventHandler
	public void itemPickup(PlayerPickupItemEvent e) {
		if (!isEnabled()) return;
		if (getConfig().getBoolean("protect-egg", true)) {
			if (protItems.containsValue(e.getItem().getUniqueId())) {
				if (protItems.containsKey(e.getPlayer()) && protItems.get(e.getPlayer()).equals(e.getItem().getUniqueId())) {
					protItems.remove(e.getPlayer());
					if (protTimes.containsKey(e.getPlayer())) protTimes.remove(e.getPlayer());
					return;
				}
				Player owner = null;
				for (Player p : protItems.keySet())
					if (protItems.get(p).equals(e.getItem().getUniqueId()))
						owner = p;
				if (owner == null) return;
				if (!protTimes.containsKey(owner) || System.currentTimeMillis() > protTimes.get(owner)) {
					protItems.remove(e.getPlayer());
					if (protTimes.containsKey(e.getPlayer())) protTimes.remove(e.getPlayer());
					return;
				}
				e.setCancelled(true);
				if (!msgCooldowns.containsKey(e.getPlayer()) || msgCooldowns.get(e.getPlayer()) < System.currentTimeMillis()) {
					HashMap<String, String> rep = new HashMap<String, String>();
					rep.put("{PLAYER}", owner.getName());
					long time = (protTimes.get(owner)-System.currentTimeMillis())/1000L;
					rep.put("{TIME}", time + " second" + ((time != 1) ? "s" : ""));
					e.getPlayer().sendMessage(MiscUtils.massReplace(CustomMessage.AP_ITEM_RESERVED.pre(), rep));
					msgCooldowns.put(e.getPlayer(), System.currentTimeMillis() + 5000L);
				}
			}
		}
	}
	@EventHandler
	public void portalForm(EntityCreatePortalEvent e) {
		if (!isEnabled()) return;
		LivingEntity ent = e.getEntity();
		Location loc = ent.getLocation();
		if (getConfig().getStringList("worlds").contains(ent.getWorld().getName())) {
			e.setCancelled(true);
			if (e.getPortalType().equals(PortalType.ENDER)) {
				if (ent.getType().equals(EntityType.ENDER_DRAGON)) {
					if (getConfig().getBoolean("generate-egg", false)) {
						Material mat = Material.getMaterial(getConfig().getString("platform-mat", "GLOWSTONE").toUpperCase());
						if (mat == null || !mat.isSolid() || mat.hasGravity())
							mat = Material.GLOWSTONE;
						Location nl = getLocAbove(loc.getWorld().getHighestBlockAt(loc), 2);
						nl.getBlock().setType(mat);
						nl = getLocAbove(nl, 1);
						nl.getBlock().setType(Material.DRAGON_EGG);
					} else if (getConfig().getBoolean("drop-egg", true)) {
						Item drop = ent.getWorld().dropItemNaturally(loc, new ItemStack(Material.DRAGON_EGG, 1));
						if (getConfig().getBoolean("protect-egg", true)) {
							if (getConfig().getInt("egg-protect-seconds", 30) > 0) {
								if (killTracker.containsValue(ent.getUniqueId())) {
									for (Player pl : killTracker.keySet()) {
										if (killTracker.get(pl).equals(ent.getUniqueId())) {
											protItems.put(pl, drop.getUniqueId());
											Long time = System.currentTimeMillis() + (getConfig().getInt("egg-protect-seconds", 30)*1000L);
											protTimes.put(pl, time);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	private Location getLocAbove(Location loc, int above) {
		Location nl = loc.clone();
		nl.setY(loc.getBlockY()+above);
		return nl;
	}
	private Location getLocAbove(Block block, int above) { return getLocAbove(block.getLocation(), above); }
}
