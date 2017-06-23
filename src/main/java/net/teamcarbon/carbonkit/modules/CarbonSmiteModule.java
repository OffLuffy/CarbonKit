package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.utils.EntHelper.EntityGroup;
import net.teamcarbon.carbonkit.utils.UserStore;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.commands.CarbonSmite.CarbonSmiteCommand;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.GoldenSmite.Smite;
import net.teamcarbon.carbonkit.utils.GoldenSmite.Smite.SmiteType;
import net.teamcarbon.carbonkit.utils.MiscUtils;

import static org.bukkit.Material.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@SuppressWarnings({"UnusedDeclaration", "MismatchedQueryAndUpdateOfCollection"})
public class CarbonSmiteModule extends Module {
	public static CarbonSmiteModule inst;
	public CarbonSmiteModule() throws DuplicateModuleException { super(CarbonKit.inst, "CarbonSmite", "csmite", "smite", "cs"); }
	public static List<Entity> killed = new ArrayList<>();
	private static List<Projectile> launchedProj = new ArrayList<>();
	public void initModule() {
		inst = this;
		if (!killed.isEmpty()) killed.clear();
		if (!launchedProj.isEmpty()) launchedProj.clear();
		for (Player p : Bukkit.getOnlinePlayers()) { loadPlayer(p); }
		addCmd(new CarbonSmiteCommand(this));
		registerListeners();
	}
	public void disableModule() {
		if (!killed.isEmpty()) killed.clear();
		if (!launchedProj.isEmpty()) launchedProj.clear();
		unregisterListeners();
	}
	public void reloadModule() {
		disableModule();
		CarbonKit.inst.reloadConfig();
		initModule();
	}
	protected boolean needsListeners() { return true; }

	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler
	public void projLaunch(ProjectileLaunchEvent e) {
		if (!isEnabled()) return;
		if (e.getEntity().getShooter() instanceof Player) {
			OfflinePlayer shooter = (OfflinePlayer)e.getEntity().getShooter();
			if (e.getEntity() instanceof Arrow && hasArrowsEnabled(shooter) && perm((Player) shooter, "use.arrows"))
				launchedProj.add(e.getEntity());
			if (e.getEntity() instanceof Snowball && hasSnowballsEnabled(shooter) && perm((Player) shooter, "use.snowballs"))
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
		if (!perm(e.getPlayer(), "use.axe"))
			return;
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR){
			if (e.getPlayer().getInventory().getItemInMainHand().getType() == GOLD_AXE){
				if (e.getClickedBlock() != null)
					if (MiscUtils.objEq(e.getClickedBlock().getType(),
							WOOD_BUTTON, STONE_BUTTON, LEVER, DIODE, DIODE_BLOCK_OFF, DIODE_BLOCK_ON, REDSTONE_COMPARATOR,
							REDSTONE_COMPARATOR_OFF, REDSTONE_COMPARATOR_ON, WOODEN_DOOR, FENCE_GATE, TRAP_DOOR, CHEST, TRAPPED_CHEST,
							ENDER_CHEST, WORKBENCH, FURNACE, BREWING_STAND, ENCHANTMENT_TABLE, DROPPER, HOPPER, DISPENSER,
							HOPPER_MINECART, STORAGE_MINECART, POWERED_MINECART, NOTE_BLOCK, ANVIL, BEACON, JUKEBOX, COMMAND))
						return;
				int rng = getConfig().getInt("range", 100);
				Block b = e.getPlayer().getTargetBlock(new HashSet<Material>(), rng);
				Smite.createSmite(e.getPlayer(), b.getLocation(), SmiteType.AXE);
			}
		}
	}

	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	private void loadPlayer(Player p) {
		UserStore us = CarbonKit.getPlayerData(p.getUniqueId());
		setArrowsEnabled(p, us.getBoolean(getName() + ".arrow-smite", false));
		setSnowballsEnabled(p, us.getBoolean(getName() + ".snowball-smite", false));
		if (us.contains(getName() + ".affected-entities")) {
			for (EntityGroup eg : EntityGroup.values())
				setGroupEnabled(p, eg, us.getStringList(getName() + ".affected-entities").contains(eg.lname()));
		} else { for (EntityGroup eg : EntityGroup.values()) setGroupEnabled(p, eg, false); }
	}

	public static boolean hasArrowsEnabled(OfflinePlayer pl) {
		UserStore us = CarbonKit.getPlayerData(pl.getUniqueId());
		return us.getBoolean(inst.getName() + ".arrow-smite", false);
	}
	public static boolean hasSnowballsEnabled(OfflinePlayer pl) {
		UserStore us = CarbonKit.getPlayerData(pl.getUniqueId());
		return us.getBoolean(inst.getName() + ".snowball-smite", false);
	}
	public static void setArrowsEnabled(OfflinePlayer pl, boolean enabled) {
		UserStore us = CarbonKit.getPlayerData(pl.getUniqueId());
		us.set(inst.getName() + ".arrow-smite", enabled);
		us.save();
	}
	public static void setSnowballsEnabled(OfflinePlayer pl, boolean enabled) {
		UserStore us = CarbonKit.getPlayerData(pl.getUniqueId());
		us.set(inst.getName() + ".snowball-smite", enabled);
		us.save();
	}
	public static void toggleArrows(OfflinePlayer pl) { setArrowsEnabled(pl, !hasArrowsEnabled(pl)); }
	public static void toggleSnowballs(OfflinePlayer pl) { setSnowballsEnabled(pl, !hasSnowballsEnabled(pl)); }
	public static boolean isGroupEnabled(OfflinePlayer pl, EntityGroup eg) {
		UserStore us = CarbonKit.getPlayerData(pl.getUniqueId());
		return us.getStringList(inst.getName() + ".affected-entities").contains(eg.lname());
	}
	public static void setGroupEnabled(OfflinePlayer pl, EntityGroup eg, boolean enabled) {
		UserStore us = CarbonKit.getPlayerData(pl.getUniqueId());
		List<String> groups = us.getStringList(inst.getName() + ".affected-entities");
		if (enabled && !groups.contains(eg.lname())) groups.add(eg.lname());
		if (!enabled && groups.contains(eg.lname())) groups.remove(eg.lname());
		us.set(inst.getName() + ".affected-entities", groups);
		us.save();
	}
	public static void toggleGroup(OfflinePlayer pl, EntityGroup eg) { setGroupEnabled(pl, eg, !isGroupEnabled(pl, eg)); }
}
