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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.*;

@SuppressWarnings("UnusedDeclaration")
public class CarbonPerksModule extends Module {
	public enum TrailEffect {
		SMOKE(0.5),
		ENDER_SIGNAL(0.0, "ender", "enderdust"),
		FIREWORKS_SPARK(0.5, "firework", "spark"),
		CRIT(0.5, "hit", "ouch"),
		MAGIC_CRIT(0.5, "mhit", "mcrit"),
		POTION_SWIRL(0.5, "confetti", "colorstrips", "colourstrips"),
		SPELL(0.5, "swirl"),
		INSTANT_SPELL(0.5, "star", "starspell"),
		WITCH_MAGIC(0.5, "wmagic", "witch"),
		NOTE(0.5, "music"),
		PORTAL(0.5),
		FLYING_GLYPH(0.5, "glyph", "text"),
		FLAME(0.5),
		LAVA_POP(0.5, "lava"),
		FOOTSTEP(0.2, "footsteps", "footprints", "footprint"),
		SPLASH(0.5),
		SMALL_SMOKE(0.5, "void", "dust"),
		COLOURED_DUST(0.5, "rainbow", "rainbowdust", "coloreddust"),
		WATERDRIP(0.5, "water", "drip", "moist"),
		SLIME(0.5),
		HEART(0.5, "hearts", "love"),
		VILLAGER_THUNDERCLOUD(0.5, "thunder", "cloud", "clouds"),
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
					if (MiscUtils.eq(query, te.name())) return te;
					if (te.getAliases().size() > 0) {
						for (String a : te.getAliases()) {
							if (MiscUtils.eq(query, a)) {
								return te;
							}
						}
					}
				}
			}
		} catch (Exception e) { (new CarbonException(CarbonKit.inst, "net.teamcarbon", e)).printStackTrace(); return null; }
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
