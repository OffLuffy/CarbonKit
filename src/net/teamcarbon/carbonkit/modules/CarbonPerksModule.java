package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.CarbonPerks.TrailCommand;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.CarbonException;
import net.teamcarbon.carbonlib.LocUtils;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BlockIterator;

import java.util.*;

@SuppressWarnings({"UnusedDeclaration", "deprecation"})
public class CarbonPerksModule extends Module {
	private HashMap<Projectile, Byte> paintballs;
	private EntityType projType = EntityType.ARROW;
	public enum TrailEffect {
		SMOKE(0.5),
		ENDER_SIGNAL(0.0, "ender", "enderdust"),
		FIREWORKS_SPARK(0.5, "firework", "spark"),
		CRIT(0.5, "hit", "ouch"),
		MAGIC_CRIT(0.5, "mhit", "mcrit"),
		POTION_SWIRL(0.5, "confetti", "colorstrips"),
		SPELL(0.5, "swirl"),
		INSTANT_SPELL(0.5, "star", "starspell"),
		WITCH_MAGIC(0.5, "wmagic", "witch"),
		NOTE(0.5, "music"),
		PORTAL(0.5),
		FLYING_GLYPH(0.5, "glyph", "text"),
		FLAME(0.5),
		LAVA_POP(0.5, "lava"),
		FOOTSTEP(0.2, "footprint"),
		SPLASH(0.5),
		SMALL_SMOKE(0.5, "void", "dust"),
		COLOURED_DUST(0.5, "rainbow", "colordust"),
		WATERDRIP(0.5, "water", "drip", "moist"),
		SLIME(0.5),
		HEART(0.5, "love"),
		VILLAGER_THUNDERCLOUD(0.5, "thunder", "cloud"),
		HAPPY_VILLAGER(0.5, "happy", "grow"),
		TILE_BREAK(0.5, "break", "crumble");

		private double spread = 0;
		private List<String> aliases = new ArrayList<String>();
		public Effect effect;
		TrailEffect(double spread, String... aliases) {
			this.spread = spread;
			Collections.addAll(this.aliases, aliases);
			try {
				effect = Effect.valueOf(name());
			} catch (Exception e) { /*(new CarbonException(e)).printStackTrace();*/ }
		}
		public List<String> getAliases() { return aliases; }
		public double getSpread() { return spread; }
		public Effect getEffect() { return effect; }
		public boolean isEnabled() { return effect != null; }
		public String lname() { return name().toLowerCase(); }
		public static List<String> getAllowedNames() {
			List<String> allowed = new ArrayList<String>();
			for (TrailEffect te : values()) allowed.add(te.name());
			return allowed;
		}
		public static TrailEffect fromEffect(Effect e) {
			for (TrailEffect te : values()) {
				if (te.getEffect() == e) {
					return te;
				}
			}
			return null;
		}
	}
	private static HashMap<Player, List<TrailEffect>> fx = new HashMap<Player, List<TrailEffect>>();
	private static List<Player> enabledPlayers = new ArrayList<Player>();
	private static boolean essExists = false;
	private static Plugin ess = null;
	private Random r = new Random(System.currentTimeMillis());
	public CarbonPerksModule() throws DuplicateModuleException { super("CarbonPerks", "cperks", "cperk", "perks", "perk", "cp"); }
	public void initModule() {
		addCmd(new TrailCommand(this));
		for (Player p : Bukkit.getOnlinePlayers()) loadTrailData(p);
		if (MiscUtils.checkPlugin("Essentials", true)) {
			essExists = true;
			ess = MiscUtils.getPlugin("Essentials", true);
		}
		paintballs = new HashMap<Projectile, Byte>();
		registerListeners();
	}
	public void disableModule() {
		for (Player p : Bukkit.getOnlinePlayers()) saveTrailData(p);
		fx.clear();
		enabledPlayers.clear();
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
	public void onMove(PlayerMoveEvent e) {
		if (!isEnabled()) return;
		long time = System.currentTimeMillis();
		if (enabledPlayers.contains(e.getPlayer()) && fx.containsKey(e.getPlayer())) {
			if (essExists) {
				com.earth2me.essentials.Essentials ep = (com.earth2me.essentials.Essentials)ess;
				com.earth2me.essentials.User eu = ep.getUser(e.getPlayer());
				if (eu.isVanished()) {
					setTrailEnabled(e.getPlayer(), false);
					return;
				}
			}
			if (!LocUtils.isSameLoc(e.getTo(), e.getFrom())) {
				for (TrailEffect te : CarbonPerksModule.fx.get(e.getPlayer())) {
					double hrx = te.getSpread() * Math.random(),
							hrz = te.getSpread() * Math.random(),
							vr = te.getSpread() * Math.random();
					Location pl = e.getFrom().clone();
					pl.setY(pl.getY() + vr);
					pl.setX(pl.getX() + (r.nextBoolean() ? hrx : -hrx));
					pl.setZ(pl.getZ() + (r.nextBoolean() ? hrz : -hrz));
					pl.getWorld().playEffect(pl, te.getEffect(), 20);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (!isEnabled()) return;
		if (e.getItem() == null) return;
		Class<? extends Projectile> pc = Snowball.class;
		if (projType.getEntityClass().isInstance(Projectile.class)) pc = (Class<? extends Projectile>)projType.getEntityClass();
		if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getPlayer().isSneaking()) {
			if (e.getItem().getType() == Material.INK_SACK) {
				DyeColor dc = DyeColor.values()[(DyeColor.values().length - 1) - e.getItem().getData().getData()];
				Projectile p = e.getPlayer().launchProjectile(pc);
				p.setVelocity(p.getVelocity().multiply(3));
				paintballs.put(p, dc.getData());
				//e.getPlayer().sendMessage("You right clicked with dye color: " + dc.name().toLowerCase());
			}
			if (e.getItem().getType() == Material.SNOW_BALL) {
				e.setCancelled(true);
				Projectile p = e.getPlayer().launchProjectile(pc);
				p.setVelocity(p.getVelocity().multiply(3));
				paintballs.put(p, (byte) -1);
				//e.getPlayer().sendMessage("You right clicked with a water bottle");
			}
		}
	}

	@EventHandler
	public void projHit(ProjectileHitEvent e) {
		if (!isEnabled()) return;
		Projectile ent = e.getEntity();
		if (paintballs.containsKey(ent)) {
			Player shooter = (Player)ent.getShooter();
			BlockIterator i = new BlockIterator(ent.getWorld(), ent.getLocation().toVector(), ent.getVelocity().normalize(), 0.0, 4);
			Block hit = null;
			while (i.hasNext()) {
				hit = i.next();
				if (!isPermeable(hit.getType())) break;
			}
			if (hit != null) {
				boolean allow = true;
				if (MiscUtils.checkPlugin("WorldGuard", true)) {
					Plugin wgp = MiscUtils.getPlugin("WorldGuard", true);
					if (wgp != null) {
						com.sk89q.worldguard.bukkit.WorldGuardPlugin wg = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) wgp;
						if (!wg.canBuild(shooter, hit)) allow = false;
					}
				}
				if (allow && MiscUtils.checkPlugin("GriefPrevention", true)) {
					me.ryanhamshire.GriefPrevention.DataStore ds = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore;
					me.ryanhamshire.GriefPrevention.Claim claim = ds.getClaimAt(hit.getLocation(), false, null);
					me.ryanhamshire.GriefPrevention.PlayerData pd = ds.getPlayerData(shooter.getUniqueId());
					allow = claim == null || claim.ownerID.equals(shooter.getUniqueId()) || (pd != null && pd.ignoreClaims);
					if (!allow) {
						String msg = claim.allowBuild(shooter, Material.PAINTING);
						if (msg == null) {
							allow = true;
						} else {
							shooter.sendMessage(msg);
							return;
						}
					}
				}
				if (!allow) { return; }

				boolean changed = false;

				String pre = "carbonkit.perks.paintball.";
				byte bd = hit.getData();
				if (paintballs.get(ent) == (byte)-1) {
					switch (hit.getType()) {
						case WOOL:
							if (!MiscUtils.perm(shooter, pre + "wool.clean")) return;
							changed = bd != (byte) 0;
							hit.setData((byte)0);
							break;
						case CARPET:
							if (!MiscUtils.perm(shooter, pre + "carpet.clean")) return;
							changed = bd != (byte) 0;
							hit.setData((byte) 0);
							break;
						case STAINED_GLASS:
							if (!MiscUtils.perm(shooter, pre + "glass.clean")) return;
							changed = hit.getType() != Material.GLASS || bd != (byte) 0;
							hit.setType(Material.GLASS);
							hit.setData((byte) 0);
							break;
						case STAINED_GLASS_PANE:
							if (!MiscUtils.perm(shooter, pre + "thinglass.clean")) return;
							changed = hit.getType() != Material.THIN_GLASS || bd != (byte) 0;
							hit.setType(Material.THIN_GLASS);
							hit.setData((byte) 0);
							break;
						case STAINED_CLAY:
							if (!MiscUtils.perm(shooter, pre + "clay.clean")) return;
							changed = hit.getType() != Material.HARD_CLAY || bd != (byte) 0;
							hit.setType(Material.HARD_CLAY);
							hit.setData((byte) 0);
							break;
						case LOG:
						case LOG_2:
							if (!MiscUtils.perm(shooter, pre + "logs.clean")) return;
							changed = hit.getType() != Material.LOG || bd != (byte) 0;
							hit.setType(Material.LOG);
							hit.setData((byte) (bd%4));
							break;
						/*case SPRUCE_DOOR:
						case BIRCH_DOOR:
						case JUNGLE_DOOR:
						case ACACIA_DOOR:
						case DARK_OAK_DOOR:
							if (!MiscUtils.perm(shooter, pre + "door.clean")) return;
							changed = hit.getType() != Material.WOODEN_DOOR;
							hit.setType(Material.WOODEN_DOOR);
							break;*/
						case WOOD_DOUBLE_STEP:
						case DOUBLE_STEP:
							if (hit.getType() == Material.DOUBLE_STEP  && !MiscUtils.perm(shooter, pre + "wood.purple")) return;
							if (!MiscUtils.perm(shooter, pre + "wood.clean")) return;
							changed = hit.getType() != Material.WOOD_DOUBLE_STEP || bd != (byte) 0;
							hit.setType(Material.WOOD_DOUBLE_STEP);
							hit.setData((byte) 0);
							break;
						case WOOD:
						case NETHER_BRICK:
							if (hit.getType() == Material.NETHER_BRICK && !MiscUtils.perm(shooter, pre + "wood.purple")) return;
							if (!MiscUtils.perm(shooter, pre + "wood.clean")) return;
							changed = hit.getType() != Material.WOOD || bd != (byte) 0;
							hit.setType(Material.WOOD);
							hit.setData((byte) 0);
							break;
						case SPRUCE_FENCE_GATE:
						case BIRCH_FENCE_GATE:
						case JUNGLE_FENCE_GATE:
						case ACACIA_FENCE_GATE:
						case DARK_OAK_FENCE_GATE:
							if (!MiscUtils.perm(shooter, pre + "gates.clean")) return;
							changed = hit.getType() != Material.FENCE_GATE;
							hit.setType(Material.FENCE_GATE);
							hit.setData(bd);
							break;
						case SPRUCE_FENCE:
						case BIRCH_FENCE:
						case JUNGLE_FENCE:
						case ACACIA_FENCE:
						case DARK_OAK_FENCE:
						case NETHER_FENCE:
							if (hit.getType() == Material.NETHER_FENCE && !MiscUtils.perm(shooter, pre + "fences.purple")) return;
							if (!MiscUtils.perm(shooter, pre + "fences.clean")) return;
							changed = hit.getType() != Material.FENCE;
							hit.setType(Material.FENCE);
							hit.setData(bd);
							break;
						case SPRUCE_WOOD_STAIRS:
						case BIRCH_WOOD_STAIRS:
						case JUNGLE_WOOD_STAIRS:
						case ACACIA_STAIRS:
						case DARK_OAK_STAIRS:
						case NETHER_BRICK_STAIRS:
							if (hit.getType() == Material.NETHER_BRICK_STAIRS && !MiscUtils.perm(shooter, pre + "wood.purple")) return;
							if (!MiscUtils.perm(shooter, pre + "wood.clean")) return;
							changed = hit.getType() != Material.WOOD_STAIRS;
							hit.setType(Material.WOOD_STAIRS);
							hit.setData(bd);
							break;
						case WOOD_STEP:
						case STEP:
							if (hit.getType() == Material.STEP && !MiscUtils.perm(shooter, pre + "wood.purple")) return;
							if (!MiscUtils.perm(shooter, pre + "wood.clean")) return;
							byte bdm = bd >= 8 ? (byte) 8 : (byte) 0;
							changed = hit.getType() != Material.WOOD_STEP || bd != (byte) 0;
							hit.setType(Material.WOOD_STEP);
							hit.setData(bdm);
							break;
						default: break;
					}
				} else {
					switch (hit.getType()) {
						case WOOL:
							if (!MiscUtils.perm(shooter, pre + "wool." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							changed = bd != paintballs.get(ent);
							hit.setData(paintballs.get(ent));
							break;
						case CARPET:
							if (!MiscUtils.perm(shooter, pre + "carpet." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							changed = bd != paintballs.get(ent);
							hit.setData(paintballs.get(ent));
							break;
						case GLASS:
							if (!MiscUtils.perm(shooter, pre + "glass." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							changed = hit.getType() != Material.STAINED_GLASS || bd != paintballs.get(ent);
							hit.setType(Material.STAINED_GLASS);
							hit.setData(paintballs.get(ent));
							break;
						case THIN_GLASS:
							if (!MiscUtils.perm(shooter, pre + "thinglass." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							changed = hit.getType() != Material.STAINED_GLASS_PANE || bd != paintballs.get(ent);
							hit.setType(Material.STAINED_GLASS_PANE);
							hit.setData(paintballs.get(ent));
							break;
						case STAINED_GLASS:
							if (!MiscUtils.perm(shooter, pre + "glass." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							changed = bd != paintballs.get(ent);
							hit.setData(paintballs.get(ent));
							break;
						case STAINED_GLASS_PANE:
							if (!MiscUtils.perm(shooter, pre + "thinglass." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							changed = bd != paintballs.get(ent);
							hit.setData(paintballs.get(ent));
							break;
						case HARD_CLAY:
							if (!MiscUtils.perm(shooter, pre + "clay." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							changed = hit.getType() != Material.STAINED_CLAY || bd != paintballs.get(ent);
							hit.setType(Material.STAINED_CLAY);
							hit.setData(paintballs.get(ent));
							break;
						case STAINED_CLAY:
							if (!MiscUtils.perm(shooter, pre + "clay." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							changed = bd != paintballs.get(ent);
							hit.setData(paintballs.get(ent));
							break;
						case LOG:
						case LOG_2:
							if (!MiscUtils.perm(shooter, pre + "logs." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							int rotMod = hit.getData() / 4;
							switch (paintballs.get(ent)) {
								case 14: // SPRUCE
									changed = hit.getType() != Material.LOG || bd != (byte) 1;
									hit.setType(Material.LOG);
									hit.setData((byte) (rotMod * 4 + 1));
									break;
								case 0: // BIRCH
									changed = hit.getType() != Material.LOG || bd != (byte) 2;
									hit.setType(Material.LOG);
									hit.setData((byte) (rotMod * 4 + 2));
									break;
								case 12: // JUNGLE
									changed = hit.getType() != Material.LOG || bd != (byte) 3;
									hit.setType(Material.LOG);
									hit.setData((byte) (rotMod * 4 + 3));
									break;
								case 1: // ACACIA
									changed = hit.getType() != Material.LOG_2 || bd != (byte) 0;
									hit.setType(Material.LOG_2);
									hit.setData(bd);
									break;
								case 15: // DARK OAK
									changed = hit.getType() != Material.LOG_2 || bd != (byte) 1;
									hit.setType(Material.LOG_2);
									hit.setData((byte) (rotMod * 4 + 1));
									break;
							}
							break;
						/*case WOODEN_DOOR:
						case SPRUCE_DOOR:
						case BIRCH_DOOR:
						case JUNGLE_DOOR:
						case ACACIA_DOOR:
						case DARK_OAK_DOOR:
							if (!MiscUtils.perm(shooter, pre + "doors." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							switch (paintballs.get(ent)) {
								case 14: // SPRUCE
									changed = hit.getType() != Material.SPRUCE_DOOR;
									hit.setType(Material.SPRUCE_DOOR);
									break;
								case 0: // BIRCH
									changed = hit.getType() != Material.BIRCH_DOOR;
									hit.setType(Material.BIRCH_DOOR);
									break;
								case 12: // JUNGLE
									changed = hit.getType() != Material.JUNGLE_DOOR;
									hit.setType(Material.JUNGLE_DOOR);
									break;
								case 1: // ACACIA
									changed = hit.getType() != Material.ACACIA_DOOR;
									hit.setType(Material.ACACIA_DOOR);
									break;
								case 15: // DARK OAK
									changed = hit.getType() != Material.DARK_OAK_DOOR;
									hit.setType(Material.DARK_OAK_DOOR);
									break;
							}
							break;*/
						case FENCE_GATE:
						case SPRUCE_FENCE_GATE:
						case BIRCH_FENCE_GATE:
						case JUNGLE_FENCE_GATE:
						case ACACIA_FENCE_GATE:
						case DARK_OAK_FENCE_GATE:
							if (!MiscUtils.perm(shooter, pre + "gates." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							switch (paintballs.get(ent)) {
								case 14: // SPRUCE
									changed = hit.getType() != Material.SPRUCE_FENCE_GATE;
									hit.setType(Material.SPRUCE_FENCE_GATE);
									hit.setData(bd);
									break;
								case 0: // BIRCH
									changed = hit.getType() != Material.BIRCH_FENCE_GATE;
									hit.setType(Material.BIRCH_FENCE_GATE);
									hit.setData(bd);
									break;
								case 12: // JUNGLE
									changed = hit.getType() != Material.JUNGLE_FENCE_GATE;
									hit.setType(Material.JUNGLE_FENCE_GATE);
									hit.setData(bd);
									break;
								case 1: // ACACIA
									changed = hit.getType() != Material.ACACIA_FENCE_GATE;
									hit.setType(Material.ACACIA_FENCE_GATE);
									hit.setData(bd);
									break;
								case 15: // DARK OAK
									changed = hit.getType() != Material.DARK_OAK_FENCE_GATE;
									hit.setType(Material.DARK_OAK_FENCE_GATE);
									hit.setData(bd);
									break;
							}
							break;
						case FENCE:
						case SPRUCE_FENCE:
						case BIRCH_FENCE:
						case JUNGLE_FENCE:
						case ACACIA_FENCE:
						case DARK_OAK_FENCE:
						case NETHER_FENCE:
							if (hit.getType() == Material.NETHER_FENCE && !MiscUtils.perm(shooter, pre + "fences.purple")) return;
							if (!MiscUtils.perm(shooter, pre + "fences." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							switch (paintballs.get(ent)) {
								case 14: // SPRUCE
									changed = hit.getType() != Material.SPRUCE_FENCE;
									hit.setType(Material.SPRUCE_FENCE);
									break;
								case 0: // BIRCH
									changed = hit.getType() != Material.BIRCH_FENCE;
									hit.setType(Material.BIRCH_FENCE);
									break;
								case 12: // JUNGLE
									changed = hit.getType() != Material.JUNGLE_FENCE;
									hit.setType(Material.JUNGLE_FENCE);
									break;
								case 1: // ACACIA
									changed = hit.getType() != Material.ACACIA_FENCE;
									hit.setType(Material.ACACIA_FENCE);
									break;
								case 15: // DARK OAK
									changed = hit.getType() != Material.DARK_OAK_FENCE;
									hit.setType(Material.DARK_OAK_FENCE);
									break;
								case 10: // NETHERBRICK
									changed = hit.getType() != Material.NETHER_FENCE;
									hit.setType(Material.NETHER_FENCE);
									break;
							}
							break;
						case WOOD_STAIRS:
						case SPRUCE_WOOD_STAIRS:
						case BIRCH_WOOD_STAIRS:
						case JUNGLE_WOOD_STAIRS:
						case ACACIA_STAIRS:
						case DARK_OAK_STAIRS:
						case NETHER_BRICK_STAIRS:
							if (hit.getType() == Material.NETHER_BRICK_STAIRS && !MiscUtils.perm(shooter, pre + "fences.purple")) return;
							if (!MiscUtils.perm(shooter, pre + "wood." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							switch (paintballs.get(ent)) {
								case 14: // SPRUCE
									changed = hit.getType() != Material.SPRUCE_WOOD_STAIRS;
									hit.setType(Material.SPRUCE_WOOD_STAIRS);
									hit.setData(bd);
									break;
								case 0: // BIRCH
									changed = hit.getType() != Material.BIRCH_WOOD_STAIRS;
									hit.setType(Material.BIRCH_WOOD_STAIRS);
									hit.setData(bd);
									break;
								case 12: // JUNGLE
									changed = hit.getType() != Material.JUNGLE_WOOD_STAIRS;
									hit.setType(Material.JUNGLE_WOOD_STAIRS);
									hit.setData(bd);
									break;
								case 1: // ACACIA
									changed = hit.getType() != Material.ACACIA_STAIRS;
									hit.setType(Material.ACACIA_STAIRS);
									hit.setData(bd);
									break;
								case 15: // DARK OAK
									changed = hit.getType() != Material.DARK_OAK_STAIRS;
									hit.setType(Material.DARK_OAK_STAIRS);
									hit.setData(bd);
									break;
								case 10: // NETHERBRICK
									changed = hit.getType() != Material.NETHER_BRICK_STAIRS;
									hit.setType(Material.NETHER_BRICK_STAIRS);
									hit.setData(bd);
							}
							break;
						case WOOD_STEP:
						case STEP:
							if (hit.getType() == Material.STEP && !MiscUtils.perm(shooter, pre + "wood.purple")) return;
							if (!MiscUtils.perm(shooter, pre + "wood." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							int bdm = bd >= 8 ? 8 : 0; // data modifier, determines if slab is inverted or not
							switch (paintballs.get(ent)) {
								case 14: // SPRUCE
									changed = hit.getType() != Material.WOOD_STEP || bd != (byte) 9;
									hit.setType(Material.WOOD_STEP);
									hit.setData((byte) (1+bdm));
									break;
								case 0: // BIRCH
									changed = hit.getType() != Material.WOOD_STEP || bd != (byte) 10;
									hit.setType(Material.WOOD_STEP);
									hit.setData((byte) (2+bdm));
									break;
								case 12: // JUNGLE
									changed = hit.getType() != Material.WOOD_STEP || bd != (byte) 11;
									hit.setType(Material.WOOD_STEP);
									hit.setData((byte) (3+bdm));
									break;
								case 1: // ACACIA
									changed = hit.getType() != Material.WOOD_STEP || bd != (byte) 12;
									hit.setType(Material.WOOD_STEP);
									hit.setData((byte) (4+bdm));
									break;
								case 15: // DARK OAK
									changed = hit.getType() != Material.WOOD_STEP || bd != (byte) 13;
									hit.setType(Material.WOOD_STEP);
									hit.setData((byte) (5+bdm));
									break;
								case 10: // NETHERBRICK
									changed = hit.getType() != Material.STEP;
									hit.setType(Material.STEP);
									hit.setData((byte) (6+bdm));
									break;
							}
							break;
						case WOOD_DOUBLE_STEP:
						case DOUBLE_STEP:
							if (hit.getType() == Material.DOUBLE_STEP && !MiscUtils.perm(shooter, pre + "wood.purple")) return;
							if (!MiscUtils.perm(shooter, pre + "wood." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							switch (paintballs.get(ent)) {
								case 14: // SPRUCE
									changed = hit.getType() != Material.WOOD_DOUBLE_STEP || bd != (byte) 1;
									hit.setType(Material.WOOD_DOUBLE_STEP);
									hit.setData((byte) 1);
									break;
								case 0: // BIRCH
									changed = hit.getType() != Material.WOOD_DOUBLE_STEP || bd != (byte) 2;
									hit.setType(Material.WOOD_DOUBLE_STEP);
									hit.setData((byte) 2);
									break;
								case 12: // JUNGLE
									changed = hit.getType() != Material.WOOD_DOUBLE_STEP || bd != (byte) 3;
									hit.setType(Material.WOOD_DOUBLE_STEP);
									hit.setData((byte) 3);
									break;
								case 1: // ACACIA
									changed = hit.getType() != Material.WOOD_DOUBLE_STEP || bd != (byte) 4;
									hit.setType(Material.WOOD_DOUBLE_STEP);
									hit.setData((byte) 4);
									break;
								case 15: // DARK OAK
									changed = hit.getType() != Material.WOOD_DOUBLE_STEP || bd != (byte) 5;
									hit.setType(Material.WOOD_DOUBLE_STEP);
									hit.setData((byte) 5);
									break;
								case 10: // NETHERBRICK
									changed = hit.getType() != Material.DOUBLE_STEP || bd != (byte) 6;
									hit.setType(Material.DOUBLE_STEP);
									hit.setData((byte) 6);
									break;
							}
							break;
						case WOOD:
						case NETHER_BRICK:
							if (hit.getType() == Material.NETHER_BRICK && !MiscUtils.perm(shooter, pre + "wood.purple")) return;
							if (!MiscUtils.perm(shooter, pre + "wood." + DyeColor.getByData(paintballs.get(ent)).name().toLowerCase())) return;
							switch (paintballs.get(ent)) {
								case 14: // SPRUCE
									changed = hit.getType() != Material.WOOD || bd != (byte) 1;
									hit.setType(Material.WOOD);
									hit.setData((byte) 1);
									break;
								case 0: // BIRCH
									changed = hit.getType() != Material.WOOD || bd != (byte) 2;
									hit.setType(Material.WOOD);
									hit.setData((byte) 2);
									break;
								case 12: // JUNGLE
									changed = hit.getType() != Material.WOOD || bd != (byte) 3;
									hit.setType(Material.WOOD);
									hit.setData((byte) 3);
									break;
								case 1: // ACACIA
									changed = hit.getType() != Material.WOOD || bd != (byte) 4;
									hit.setType(Material.WOOD);
									hit.setData((byte) 4);
									break;
								case 15: // DARK OAK
									changed = hit.getType() != Material.WOOD || bd != (byte) 5;
									hit.setType(Material.WOOD);
									hit.setData((byte) 5);
									break;
								case 10: // NETHERBRICK
									changed = hit.getType() != Material.NETHER_BRICK;
									hit.setType(Material.NETHER_BRICK);
									break;
							}
							break;
						default: break;
					}
				}
				if (changed) {
					if (shooter.getGameMode() != GameMode.CREATIVE || MiscUtils.perm(shooter, pre + "infinite")) {
						ItemStack is = new ItemStack(Material.INK_SACK, 1, (short) (DyeColor.values().length - paintballs.get(ent)));
						Inventory inv = shooter.getInventory();
						if (inv.contains(is)) {
							ItemStack newStack = inv.getItem(inv.first(is));
							newStack.setAmount(newStack.getAmount()-1);
							if (newStack.getAmount() == 0) inv.remove(inv.first(is));
							else inv.setItem(inv.first(is), newStack);
						}
						//if (shooter.getInventory().containsAtLeast(is, 2)) { shooter.getInventory().remove(is); }
					}
				}
			}
			ent.remove();
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e){
		if (e.getDamager().getType() == EntityType.SNOWBALL) {

		}
	}

	/*@EventHandler
	public void projHit(ProjectileHitEvent e) {
		if (!isEnabled()) return;
		if (e.getEntity().getShooter() instanceof Player) {
			if (e.getEntity().getType() == EntityType.ARROW) {
				FireworkUtils.playFirework(e.getEntity().getLocation(), FireworkUtils.generateRandom());
			} else if (e.getEntity().getType() == EntityType.SNOWBALL) {
				FireworkUtils.playFirework(e.getEntity().getLocation(), FireworkUtils.generateRandom());
			}
		}
	}

	@EventHandler
	public void entityDamaged(EntityDamageByEntityEvent e) {
		if (!isEnabled()) return;
		if (e.getDamager() instanceof Projectile) {
			Projectile proj = (Projectile) e.getDamager();
			ProjectileSource shooter = proj.getShooter();
			if (shooter instanceof Player) {
				Player pl = (Player) shooter;
				if (enabledPlayers.contains(pl)) {
					if (proj instanceof Arrow) {
						if (!getConfig().getStringList("firework-bow-damage-percent.disabled-worlds").contains(pl.getWorld().getName())) {
							double dmgMod = 1.0;
							if (e.getEntity() instanceof Player) {
								dmgMod = getConfig().getDouble("firework-bow-damage-percent.players", 100.0) / 100.0;
							} else if (e.getEntity() instanceof LivingEntity) {
								dmgMod = getConfig().getDouble("firework-bow-damage-percent.mobs", 120.0) / 100.0;
							}
							e.setDamage(e.getDamage() * dmgMod);
						}
					} else if (proj instanceof Snowball) {
						if (!getConfig().getStringList("firework-bow-damage-percent.disabled-worlds").contains(pl.getWorld().getName())) {
							double dmgMod = 0.0;
							if (e.getEntity() instanceof Player) {
								dmgMod = getConfig().getDouble("fireworks-snowball-damage.players", 0.0);
							} else if (e.getEntity() instanceof LivingEntity) {
								dmgMod = getConfig().getDouble("fireworks-snowball-damage.mobs", 4.0);
							}
							e.setDamage(e.getDamage() + dmgMod);
						}
					}
				}
			}
		}
	}*/

	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		if (!isEnabled()) return;
		loadTrailData(e.getPlayer());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if (!isEnabled()) return;
		saveTrailData(e.getPlayer());
		clearCachedData(e.getPlayer());
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	// For paintballs, checks if the BlockIterator should ignore this Material and look for the next solid block
	private boolean isPermeable(Material mat) {
		return mat == Material.AIR || MiscUtils.objEq(mat, Material.VINE, Material.COCOA, Material.LADDER,
				Material.SIGN, Material.SIGN_POST, Material.WALL_SIGN, Material.BANNER, Material.STANDING_BANNER,
				Material.WALL_BANNER, Material.SAPLING, Material.WATER, Material.STATIONARY_WATER, Material.LAVA,
				Material.STATIONARY_LAVA, Material.RAILS, Material.ACTIVATOR_RAIL, Material.DETECTOR_RAIL,
				Material.POWERED_RAIL, Material.LONG_GRASS, Material.WEB, Material.YELLOW_FLOWER,
				Material.BROWN_MUSHROOM, Material.RED_MUSHROOM, Material.TORCH, Material.FIRE, Material.REDSTONE,
				Material.LEVER, Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.STONE_BUTTON,
				Material.WOOD_BUTTON, Material.STONE_PLATE, Material.WOOD_PLATE, Material.REDSTONE_COMPARATOR_OFF,
				Material.REDSTONE_COMPARATOR_ON, Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON, Material.MELON_STEM,
				Material.PUMPKIN_STEM, Material.WATER_LILY, Material.NETHER_WARTS, Material.TRIPWIRE,
				Material.TRIPWIRE_HOOK, Material.CARROT, Material.CROPS, Material.POTATO, Material.FLOWER_POT,
				Material.DOUBLE_PLANT, Material.RED_ROSE);
	}

	/**
	 * Attempts to parse a String query to an Effect if the TrailEffect is allowed
	 * @param query The String query (the TrailEffect name or alias)
	 * @return Returns an TrailEffect if found, null otherwise
	 */
	public static TrailEffect getEffect(String query) {
		try {
			if (query == null) return null;
			for (TrailEffect te : TrailEffect.values()) {
				if (te.isEnabled()) {
					if (MiscUtils.eq(query, te.name(), te.name()+"s")) return te;
					if (te.getAliases().size() > 0) {
						for (String a : te.getAliases()) { if (MiscUtils.eq(query, a, a+"s")) { return te; } }
					}
				}
			}
		} catch (Exception e) { (new CarbonException(CarbonKit.inst, e)).printStackTrace(); return null; }
		return null;
	}

	/**
	 * Sets the Player's Effect and enables effects if not already enabled
	 * @param p The Player whose effect to set
	 * @param e The Effects to set it to. A null value or empty list will clear Effects
	 * @see #clearCachedData(Player)
	 */
	public static void setTrailEffects(Player p, List<TrailEffect> e) { setTrailEffects(p, e, true);}

	/**
	 * Sets the Player's Effect and enables effects if not already enabled
	 * @param p The Player whose effect to set
	 * @param e The Effects to set it to. A null value or empty list will clear Effects
	 * @param enabled Whether to set the Player's TrailEffects as active or not
	 * @see #clearCachedData(Player)
	 */
	public static void setTrailEffects(Player p, List<TrailEffect> e, boolean enabled) {
		if (e == null || e.isEmpty()) {
			clearCachedData(p);
		} else {
			int maxEffects = CarbonKit.getDefConfig().getInt("CarbonPerks.max-trails", 3);
			if (!MiscUtils.perm(p, "carbonkit.perks.trails.multiple")) maxEffects = 1;
			for (TrailEffect effect : new ArrayList<TrailEffect>(e))
				if (!MiscUtils.perm(p, "carbonkit.perks.trails.set." + effect.name().toLowerCase().replace("_", "")))
					e.remove(effect);
			if (e.size() > maxEffects)
				e = e.subList(0,maxEffects-1);
			fx.put(p, e);
			setTrailEnabled(p, enabled);
		}
		saveTrailData(p);
	}

	/**
	 * Adds an Effect to the current trails if there is less than the max-trails already applied
	 * @param p The Player whom to add an Effect to
	 * @param te The Effect to add
	 */
	public static void addTrailEffect(Player p, TrailEffect te) {
		int maxEffects = CarbonKit.getDefConfig().getInt("CarbonPerks.max-trails", 3);
		if (!MiscUtils.perm(p, "carbonkit.perks.trails.multiple")) maxEffects = 1;
		if (!MiscUtils.perm(p, "carbonkit.perks.trails.set." + te.name().toLowerCase().replace("_", ""))) return;
		if (getTrailEffects(p).size() < maxEffects) {
			List<TrailEffect> effects = getTrailEffects(p);
			effects.add(te);
			setTrailEffects(p, effects);
			saveTrailData(p);
		}
	}

	/**
	 * Removes an Effect from the current trails if it is one of the Player's trails
	 * @param p The Player whom to remove an Effect from
	 * @param te The Effect to remove
	 */
	public static void removeTrailEffect(Player p, TrailEffect te) {
		if (getTrailEffects(p).contains(te)) {
			List<TrailEffect> effects = getTrailEffects(p);
			effects.remove(te);
			setTrailEffects(p, effects);
			saveTrailData(p);
		}
	}

	/**
	 * Clears all trail effects from the Player and disables trails
	 * @param p The Player whose effects to clear
	 */
	public static void clearCachedData(Player p) {
		if (fx.containsKey(p)) { fx.remove(p); }
		if (isTrailEnabled(p)) setTrailEnabled(p, false);
	}

	/**
	 * Fetches a copy of the List of Effects applied to the specified Player
	 * @param p The Player whose Effects to fetch
	 * @return Returns a List&lt;Effect&gt; with the Player's applied effects (may be an empty List)
	 */
	public static List<TrailEffect> getTrailEffects(Player p) {
		if (fx.containsKey(p))
			return new ArrayList<TrailEffect>(fx.get(p));
		else return new ArrayList<TrailEffect>();
	}

	/**
	 * Indicates if the Player has enabled their particle trail
	 * @param p The Player to check
	 * @return Returns true if the Player is using a particle trail
	 */
	public static boolean isTrailEnabled(Player p) { return enabledPlayers.contains(p); }

	/**
	 * Sets whether the Player's particle trail is enabled
	 * @param p The Player to set
	 * @param enabled Whether or not the Player's particle trail is enabled
	 */
	public static void setTrailEnabled(Player p, boolean enabled) {
		if (enabled && !isTrailEnabled(p)) { enabledPlayers.add(p); }
		else if (!enabled && isTrailEnabled(p)) { enabledPlayers.remove(p); }
		saveTrailData(p);
	}

	/**
	 * Toggles whether the Player's particle trail is enabled or not
	 * @param p The Player to set
	 */
	public static void toggleTrailEnabled(Player p) {
		setTrailEnabled(p, !isTrailEnabled(p));
		saveTrailData(p);
	}

	/**
	 * Saves the List of Effects the Player has applied to disk
	 * @param p The Player whose trail Effects to save
	 */
	public static void saveTrailData(Player p) {
		String id = p.getUniqueId().toString();
		String cpfx = "CarbonPerks.effects.", cpe = "CarbonPerks.enabled-effects";
		if(getTrailEffects(p).size() > 0) {
			List<String> fxNames = new ArrayList<String>();
			for (TrailEffect e : getTrailEffects(p)) { fxNames.add(e.name().replace("_", "")); }
			data().set(cpfx + id, fxNames);
			if (data().getStringList(cpe).contains(id) != isTrailEnabled(p)) {
				List<String> enabled = data().getStringList(cpe);
				if (!isTrailEnabled(p) && enabled.contains(id)) enabled.remove(id);
				if (isTrailEnabled(p) && !enabled.contains(id)) enabled.add(id);
				data().set(cpe, enabled);
			}
		} else {
			data().set(cpfx + id, null);
			List<String> enabled = data().getStringList(cpe);
			if (enabled.contains(id)) enabled.remove(id);
			data().set(cpe, enabled);
		}
		CarbonKit.saveConfig(ConfType.DATA);
	}

	/**
	 * Loads a List of TrailEffects from disk for the specified Player and stores it
	 * @param p The Player whose TrailEffects to load
	 */
	public static void loadTrailData(Player p) {
		String id = p.getUniqueId().toString();
		String cpfx = "CarbonPerks.effects.", cpe = "CarbonPerks.enabled-effects";
		List<String> effects = new ArrayList<String>();
		if(data().contains(cpfx + id)) effects = data().getStringList(cpfx + id);
		List<TrailEffect> parsed = new ArrayList<TrailEffect>();
		for (String e : effects) { if (getEffect(e) != null) parsed.add(getEffect(e)); }
		setTrailEffects(p, parsed, data().getStringList(cpe).contains(id));
		saveTrailData(p); // Save again in case valid effects have changed since last load
	}

	// Short-hand method for load/save methods to access data file
	private static FileConfiguration data() { return CarbonKit.getConfig(ConfType.DATA); }

	/**
	 * Fetches a random allowed TrailEffect
	 * @return Returns a random TrailEffect or null if it fails to parse one
	 */
	public static TrailEffect getRandomEffect() {
		// Loops over random allowed Effect names and tries to parse an Effect from one. Done in a
		// loop to prevent failing to parse Effects that aren't supported in some Bukkit versions
		for (int i = 0; i < 20; i++) {
			try {
				return TrailEffect.values()[MiscUtils.rand(0, TrailEffect.values().length-1)];
			} catch(Exception e) { /* Failed to get effect. Probably older version of Bukkit */ }
		}
		return null; // give up after 20 tries
	}

	/**
	 * Fetches a random allowed TrailEffect from a list of TrailEffects
	 * @param effects The List of TrailEffects to choose from
	 * @return Returns a TrailEffect from the List provided
	 */
	public static TrailEffect getRandomEffect(List<TrailEffect> effects) { return effects.get(MiscUtils.rand(0, effects.size())); }

	/**
	 * Fetches a copy of the list of String names representing the TrailEffects which are allowed
	 * @return Returns a list of all the allowed TrailEffect names
	 */
	public static List<String> getEffectNames() { return new ArrayList<String>(TrailEffect.getAllowedNames()); }
}
