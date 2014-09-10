package me.offluffy.carbonkit.modules;

import java.util.ArrayList;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.cmds.CommandGoldenSmite;
import me.offluffy.carbonkit.utils.DuplicateModuleException;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Log;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.Smite;
import me.offluffy.carbonkit.utils.Smite.SmiteType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ModuleGoldenSmite extends Module {
	public List<String> enabledArrowList;
	public List<Arrow> arrows;
	public static List<Entity> killed;
	public ConfigurationSection kill;
	public ModuleGoldenSmite() throws DuplicateModuleException { super("GoldenSmite", "gsmite", "goldsmite", "gs"); }
	
	public enum EntType {
		HOSTILE("Hostiles","hostile","hm","hmob","hostilemob","hostiles","hostilemobs","hmobs"),
		NEUTRAL("Neutrals","neutral","nm","nmob","neutralmob","neutrals","neutralmobs","nmobs"),
		PASSIVE("Passives","passive","pm","pmob","passivemob","passives","passivemobs","pmobs"),
		TAMED("Tamed","pets","tm","tamed","pet","tame","tamedmobs","tamemobs","tamedmob","tamemob","petmob","petmobs"),
		PLAYER("Players","player","pl","players","person","people","human","humans","steve","steves"),
		DROP("Drops","drops","items","itemdrops","drop");
		private String name;
		private String[] aliases;
		EntType(String name, String ... aliases) {
			this.name = name;
			this.aliases = aliases;
		}
		public String getName() { return name; }
		public String[] getAliases() { return aliases; }
	}

	@Override
	public void initModule() {
		enabledArrowList = new ArrayList<String>();
		arrows = new ArrayList<Arrow>();
		killed = new ArrayList<Entity>();
		kill = CarbonKit.data.getConfigurationSection(getName() + ".enabled-kills");
		addCmd(new CommandGoldenSmite(this));
	}

	@Override
	public void disableModule() {}

	@Override
	protected boolean hasListeners() { return true; }

	@Override
	public boolean hasDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void interactEvent(PlayerInteractEvent e) {
		if (isEnabled()){
			if (!Lib.perm(e.getPlayer(), "gsmite.use.axe"))
				return;
			if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR){
				if (e.getPlayer().getItemInHand().getType() == Material.GOLD_AXE){
					Material[] noSmite = new Material[] {
						Material.WOOD_BUTTON, Material.STONE_BUTTON, Material.LEVER, Material.DIODE,
						Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.REDSTONE_COMPARATOR,
						Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON, Material.WOODEN_DOOR,
						Material.FENCE_GATE, Material.TRAP_DOOR, Material.CHEST, Material.TRAPPED_CHEST,
						Material.ENDER_CHEST, Material.WORKBENCH, Material.FURNACE, Material.BREWING_STAND,
						Material.ENCHANTMENT_TABLE, Material.DROPPER, Material.HOPPER, Material.DISPENSER,
						Material.HOPPER_MINECART, Material.STORAGE_MINECART, Material.POWERED_MINECART,
						Material.NOTE_BLOCK, Material.ANVIL, Material.BEACON, Material.JUKEBOX, Material.COMMAND
					};
					for (Material ml : noSmite)
						if (e.getClickedBlock() != null)
							if (e.getClickedBlock().getType() == ml) 
								return; // Exit if clickable block
					int rng = CarbonKit.config.getInt(getName() + ".range", 100);
					Location l = e.getPlayer().getTargetBlock(null, rng).getLocation();
					if (e.getPlayer().getTargetBlock(null, rng).getType() == Material.AIR)
						return; // Exit if out of range
					Smite.createSmite(e.getPlayer(), l, SmiteType.AXE);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void joinEvent(PlayerJoinEvent e) {
		if (Lib.perm(e.getPlayer(), "gsmite.remember")) {
			if (CarbonKit.data.getStringList("gsmite.enabled-arrow").contains(e.getPlayer().getName()))
				enabledArrowList.add(e.getPlayer().getName());
		}
	}

	@EventHandler
	public void quitEvent(PlayerQuitEvent e) {
		if (enabledArrowList.contains(e.getPlayer().getName()))
			enabledArrowList.remove(e.getPlayer().getName());
	}

	@EventHandler
	public void deathEvent(EntityDeathEvent e){
		if (killed.contains(e.getEntity())) {
			if (CarbonKit.config.getBoolean(getName() + ".removeDrops")) {
				e.getDrops().clear();
			}
		}
	}

	@EventHandler
	public void projLaunchEvent(ProjectileLaunchEvent e) {
		if (isEnabled()){
			Entity ent = e.getEntity();
			if (ent instanceof Arrow) {
				if (!arrows.contains((Arrow)ent)) {
					Arrow arrow = (Arrow)ent;
					if (arrow.getShooter() instanceof Player) {
						Player shooter = (Player)arrow.getShooter();
						if (enabledArrowList.contains(shooter.getName()))
							arrows.add((Arrow)ent);
					}
				}
			}
		}
	}

	@EventHandler
	public void projHitEvent(ProjectileHitEvent e) {
		if (isEnabled()){
			Entity ent = e.getEntity();
			if (ent instanceof Arrow) {
				Arrow ar = (Arrow)ent;
				if (arrows.contains(ar)) {
					Player plr = (Player)ar.getShooter();
					if (enabledArrowList.contains(plr.getName()))
						Smite.createSmite(plr, ar.getLocation(), SmiteType.ARROW);
					arrows.remove(ar);
				}
			}
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
	
	public static void clearKills() {
		Log.debug("Killed list contains " + killed.size() + " entities. Clearing it.");
		killed.clear();
	}
}
