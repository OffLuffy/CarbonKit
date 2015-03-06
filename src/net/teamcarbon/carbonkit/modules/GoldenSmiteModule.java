package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.utils.EntHelper.EntityGroup;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.GoldenSmite.GoldenSmiteCommand;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.Smite;
import net.teamcarbon.carbonkit.utils.Smite.SmiteType;
import net.teamcarbon.carbonlib.MiscUtils;

import static org.bukkit.Material.*;

import java.util.*;

@SuppressWarnings({"UnusedDeclaration", "MismatchedQueryAndUpdateOfCollection"})
public class GoldenSmiteModule extends Module {
	public GoldenSmiteModule() throws DuplicateModuleException { super("GoldenSmite", "gsmite", "gs"); }
	public static List<Entity> killed;
	private static List<UUID> enabledSmiteArrows;
	private static List<UUID> enabledSmiteSnowballs;
	private static List<Projectile> launchedProj;
	private static HashMap<UUID, List<EntityGroup>> enabledGroups;
	public static GoldenSmiteModule inst;
	public void initModule() {
		inst = this;
		if (killed == null) killed = new ArrayList<Entity>(); else killed.clear();
		if (enabledSmiteArrows == null) enabledSmiteArrows = new ArrayList<UUID>(); else enabledSmiteArrows.clear();
		if (enabledSmiteSnowballs == null) enabledSmiteSnowballs = new ArrayList<UUID>(); else enabledSmiteSnowballs.clear();
		if (launchedProj == null) launchedProj = new ArrayList<Projectile>(); else launchedProj.clear();
		if (enabledGroups == null) enabledGroups = new HashMap<UUID, List<EntityGroup>>(); else enabledGroups.clear();
		for (Player p : Bukkit.getOnlinePlayers()) { loadPlayer(p); }
		addCmd(new GoldenSmiteCommand(this));
		registerListeners();
	}
	public void disableModule() {
		enabledSmiteArrows.clear();
		enabledSmiteSnowballs.clear();
		launchedProj.clear();
		enabledGroups.clear();
		unregisterListeners();
	}
	public void reloadModule() {
		disableModule();
		CarbonKit.reloadDefConfig();
		CarbonKit.reloadConfig(ConfType.DATA);
		initModule();
	}
	protected boolean needsListeners() { return true; }
	public boolean hasAllDependencies() { return true; }

	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler
	public void projLaunch(ProjectileLaunchEvent e) {
		if (!isEnabled()) return;
		if (e.getEntity().getShooter() instanceof Player) {
			OfflinePlayer shooter = (OfflinePlayer)e.getEntity().getShooter();
			if (e.getEntity() instanceof Arrow && hasArrowsEnabled(shooter) && MiscUtils.perm((Player) shooter, "carbonkit.goldensmite.use.arrows"))
				launchedProj.add(e.getEntity());
			if (e.getEntity() instanceof Snowball && hasSnowballsEnabled(shooter) && MiscUtils.perm((Player) shooter, "carbonkit.goldensmite.use.snowballs"))
				launchedProj.add(e.getEntity());
		}
	}
	@EventHandler
	public void projHit(ProjectileHitEvent e) {
		if (!isEnabled()) return;
		if ((e.getEntity() instanceof Arrow || e.getEntity() instanceof Snowball) && (launchedProj.contains(e.getEntity()) && e.getEntity().getShooter() instanceof Player)) {
			Location l = e.getEntity().getLocation();
			Smite.createSmite((Player) e.getEntity().getShooter(), l, SmiteType.PROJECTILE);
			launchedProj.remove(e.getEntity());
		}
	}
	@EventHandler
	public void playerJoin(PlayerJoinEvent e) {
		if (!isEnabled()) return;
		loadPlayer(e.getPlayer());
	}
	@EventHandler
	public void playerQuit(PlayerQuitEvent e) {
		if (!isEnabled()) return;
		UUID id = e.getPlayer().getUniqueId();
		if (enabledGroups.containsKey(id)) {
			if (!getData().getConfigurationSection("enabled-kills").contains(id.toString()))
				CarbonKit.getConfig(ConfType.DATA).set(getName() + ".enabled-kills", getGroupsAsStrings(e.getPlayer()));
			enabledGroups.remove(e.getPlayer().getUniqueId());
		}
		CarbonKit.saveConfig(ConfType.DATA);
	}
	@EventHandler
	public void deathEvent(EntityDeathEvent e) {
		if (!isEnabled()) return;
		if (e.getEntity().getType().equals(EntityType.PLAYER)) {
			if (killed.contains(e.getEntity()) && getConfig().getBoolean("removePlayerDrops", false)) {
				e.getDrops().clear();
				e.setDroppedExp(0);
			}
		} else if (killed.contains(e.getEntity()) && getConfig().getBoolean("removeMobDrops", true)) {
			e.getDrops().clear();
			e.setDroppedExp(0);
		}
	}
	@EventHandler
	public void interact(PlayerInteractEvent e) {
		if (!isEnabled()) return;
		if (isEnabled()){
			if (!MiscUtils.perm(e.getPlayer(), "carbonkit.goldensmite.use.axe"))
				return;
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR){
				if (e.getPlayer().getItemInHand().getType() == GOLD_AXE){
					if (e.getClickedBlock() != null)
						if (MiscUtils.objEq(e.getClickedBlock().getType(),
								WOOD_BUTTON, STONE_BUTTON, LEVER, DIODE, DIODE_BLOCK_OFF, DIODE_BLOCK_ON, REDSTONE_COMPARATOR,
								REDSTONE_COMPARATOR_OFF, REDSTONE_COMPARATOR_ON, WOODEN_DOOR, FENCE_GATE, TRAP_DOOR, CHEST, TRAPPED_CHEST,
								ENDER_CHEST, WORKBENCH, FURNACE, BREWING_STAND, ENCHANTMENT_TABLE, DROPPER, HOPPER, DISPENSER,
								HOPPER_MINECART, STORAGE_MINECART, POWERED_MINECART, NOTE_BLOCK, ANVIL, BEACON, JUKEBOX, COMMAND))
							return;
					int rng = getConfig().getInt("range", 100);
					Location l = e.getPlayer().getTargetBlock(null, rng).getLocation();
					if (e.getPlayer().getTargetBlock(null, rng).getType() == Material.AIR)
						return;
					Smite.createSmite(e.getPlayer(), l, SmiteType.AXE);
				}
			}
		}
	}

	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	private void loadPlayer(Player p) {
		String id = p.getUniqueId().toString();
		setArrowsEnabled(p, getData().getStringList("enabled-arrows").contains(id));
		setSnowballsEnabled(p, getData().getStringList("enabled-snowballs").contains(id));
		if (getData().getConfigurationSection("enabled-kills").contains(id)) {
			for (EntityGroup eg : EntityGroup.values())
				if (getData().getStringList("enabled-kills." + id).contains(eg.lname()))
					setGroupEnabled(p, eg, true);
		} else { for (EntityGroup eg : EntityGroup.values()) setGroupEnabled(p, eg, false); }
	}

	public static boolean hasArrowsEnabled(OfflinePlayer pl) { return enabledSmiteArrows.contains(pl.getUniqueId()); }
	public static boolean hasSnowballsEnabled(OfflinePlayer pl) { return enabledSmiteSnowballs.contains(pl.getUniqueId()); }
	public static void setArrowsEnabled(OfflinePlayer pl, boolean b) {
		UUID id = pl.getUniqueId();
		if (b && !enabledSmiteArrows.contains(id))
			enabledSmiteArrows.add(id);
		else if (!b && enabledSmiteArrows.contains(id))
			enabledSmiteArrows.remove(id);
		List<String> list = inst.getData().getStringList("enabled-arrows");
		if (b && !list.contains(id.toString()))
			list.add(id.toString());
		else if (!b && list.contains(id.toString()))
			list.remove(id.toString());
		CarbonKit.getConfig(ConfType.DATA).set(inst.getName() + ".enabled-arrows", list);
		CarbonKit.saveConfig(ConfType.DATA);
	}
	public static void setSnowballsEnabled(OfflinePlayer pl, boolean b) {
		UUID id = pl.getUniqueId();
		if (b && !enabledSmiteSnowballs.contains(id))
			enabledSmiteSnowballs.add(id);
		else if (!b && enabledSmiteSnowballs.contains(id))
			enabledSmiteSnowballs.remove(id);
		List<String> list = inst.getData().getStringList("enabled-snowballs");
		if (b && !list.contains(id.toString()))
			list.add(id.toString());
		else if (!b && list.contains(id.toString()))
			list.remove(id.toString());
		CarbonKit.getConfig(ConfType.DATA).set(inst.getName() + ".enabled-snowballs", list);
		CarbonKit.saveConfig(ConfType.DATA);
	}
	public static void toggleArrows(OfflinePlayer pl) { setArrowsEnabled(pl, !hasArrowsEnabled(pl)); }
	public static void toggleSnowballs(OfflinePlayer pl) { setSnowballsEnabled(pl, !hasSnowballsEnabled(pl)); }
	public static boolean isGroupEnabled(OfflinePlayer pl, EntityGroup eg) {
		return enabledGroups.containsKey(pl.getUniqueId()) && enabledGroups.get(pl.getUniqueId()) != null
				&& enabledGroups.get(pl.getUniqueId()).contains(eg);
	}
	public static void setGroupEnabled(OfflinePlayer pl, EntityGroup eg, boolean b) {
		UUID id = pl.getUniqueId();
		if (!enabledGroups.containsKey(id))
			enabledGroups.put(id, new ArrayList<EntityGroup>());
		if (b && !enabledGroups.get(id).contains(eg))
			enabledGroups.get(id).add(eg);
		else if (!b && enabledGroups.get(id).contains(eg))
			enabledGroups.get(id).remove(eg);
		if (enabledGroups.get(id).isEmpty())
			enabledGroups.remove(id);
		CarbonKit.getConfig(ConfType.DATA).set(inst.getName() + ".enabled-kills." + id, getGroupsAsStrings(pl));
		CarbonKit.saveConfig(ConfType.DATA);
	}
	public static void toggleGroup(OfflinePlayer pl, EntityGroup eg) { setGroupEnabled(pl, eg, !isGroupEnabled(pl, eg)); }
	private static List<String> getGroupsAsStrings(OfflinePlayer pl) {
		if (enabledGroups.containsKey(pl.getUniqueId()) && enabledGroups.get(pl.getUniqueId()) != null
				&& !enabledGroups.get(pl.getUniqueId()).isEmpty()) {
			List<String> s = new ArrayList<String>();
			for (EntityGroup eg : enabledGroups.get(pl.getUniqueId()))
				s.add(eg.lname());
			return s;
		} else return null;
	}
}
