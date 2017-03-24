package net.teamcarbon.carbonkit.utils.CarbonEssentials;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.modules.disabled.CarbonEssentialsModule;
import net.teamcarbon.carbonlib.Misc.ConfigAccessor;
import net.teamcarbon.carbonlib.Misc.LocUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class CarbonPlayer {

	CarbonEssentialsModule modInst = CarbonEssentialsModule.inst;

	private static HashMap<OfflinePlayer, CarbonPlayer> loaded = new HashMap<>();

	private OfflinePlayer player;
	private ConfigAccessor config;

	// CarbonCombat Data
	private boolean pvp;
	private long lastConflict;

	// CarbonEsentials Data
	private Player tpRequester;
	private TeleportRequest tpr;
	private boolean vanish, interact, god, fly, mute, jail, notp, frozen;
	private Location preJailLoc;
	private HashMap<String, Location> homes = new HashMap<>();
	private List<UUID> tpIgnore = new ArrayList<>();

	// CarbonPerks Data
	private boolean effectsEnabled;

	// CarbonSmite Data
	private boolean arrow, snowball;
	private List<EntityType> killTypes = new ArrayList<>();

	// CarbonWatcher Data
	private boolean watching;

	/* ********************************
	 * **        CONSTRUCTORS        **
	 * ******************************** */

		public CarbonPlayer(OfflinePlayer pl) {
			player = pl;
			config = new ConfigAccessor(CarbonKit.inst(), "userData" + File.pathSeparator + pl.getUniqueId().toString() + ".yml", "user.yml");

			// Load CarbonCombat Data
			pvp = config.config().getBoolean("CarbonCombat.pvp-enabled", false);
			lastConflict = config.config().getLong("CarbonCombat.last-conflict-time", 0L);

			// Load CarbonEssentials Data
			vanish = config.config().getBoolean("CarbonEssentials.vanished", false);
			interact = config.config().getBoolean("CarbonEssentials.interact", true);
			god = config.config().getBoolean("CarbonEssentials.god", false);
			fly = config.config().getBoolean("CarbonEssentials.fly", false);
			mute = config.config().getBoolean("CarbonEssentials.mute", false);
			jail = config.config().getBoolean("CarbonEssentials.jail", false);
			notp = config.config().getBoolean("CarbonEssentials.ignore-all-tps", false);
			frozen = config.config().getBoolean("CarbonEssentials.frozen", false);
			preJailLoc = LocUtils.fromStr(config.config().getString("CarbonEssentials.pre-jail-location", null));
			if (config.config().contains("CarbonEssentials.homes")) {
				ConfigurationSection homesSect = config.config().getConfigurationSection("CarbonEssentials.homes");
				for (String s : homesSect.getKeys(false)) {
					homes.put(s.toLowerCase(), LocUtils.fromStr(homesSect.getString(s, null)));
				}
			}
			if (config.config().contains("CarbonEssentials.ignore-tps")) {
				for (String s : config.config().getStringList("CarbonEssentials.ignore-tps")) {
					tpIgnore.add(UUID.fromString(s));
				}
			}

			// Load CarbonSmite Data
			arrow = config.config().getBoolean("CarbonSmite.arrow-enabled", false);
			snowball = config.config().getBoolean("CarbonSmite.snowball-enabled", false);
			if (config.config().contains("CarbonSmite.kill-types") && config.config().isList("CarbonSmite.kill-types")) {
				for (String s : config.config().getStringList("CarbonSmite.kill-types")) {
					EntityType et = EntityType.valueOf(s);
					killTypes.add(et);
				}
			}

			// Load CarbonWatcher Data
			watching = config.config().getBoolean("CarbonWatcher.watching", false);

		savePlayerData();
		loaded.put(pl, this);
	}

	/* ********************************
	 * **          GETTERS           **
	 * ******************************** */

	public OfflinePlayer getPlayer() { return player; }

	// CarbonCombat Getters
	public boolean hasPvpEnabled() { return pvp; }
	public long getLastConflictTime() { return lastConflict; }

	// CarbonEssentials Getters
	public Player getTeleportRequester() { return tpRequester; }
	public boolean isVanished() { return vanish; }
	public boolean isInteracting() { return interact; }
	public boolean hasGodEnabled() { return god; }
	public boolean hasFlyEnabled() { return fly; }
	public boolean isMuted() { return mute; }
	public boolean isJailed() { return jail; }
	public boolean ignoreTeleportRequests() { return notp; }
	public boolean isFrozen() { return frozen; }
	public HashMap<String, Location> getHomes() { return new HashMap<>(homes); }
	public Location getHomeLocation(String name) { return homes.containsKey(name) ? homes.get(name) : null; }
	public List<UUID> getTeleportIgnored() { return new ArrayList<>(tpIgnore); }
	public boolean isTeleportIgnored(UUID uuid) { return tpIgnore.contains(uuid); }
	public boolean isTeleportIgnored(Player pl) { return tpIgnore.contains(pl.getUniqueId()); }
	public Location getPreJailLocation() { return preJailLoc; }

	// CarbonPerks Getters
	public boolean isEffectsEnabled() { return effectsEnabled; }

	// CarbonSmite Getters
	private boolean isArrowSmiteEnabled() { return arrow; }
	private boolean isSnowballSmiteEnabled() { return snowball; }
	private boolean isKillingType(EntityType type) { return killTypes.contains(type); }
	private List<EntityType> getKillTypes() { return new ArrayList<>(killTypes); }

	// CarbonWatcher Getters
	private boolean isCommandWatching() { return watching; }

	/* ********************************
	 * **          MUTATORS          **
	 * ******************************** */

	public void setInteract(boolean interact) { this.interact = interact; savePlayerData(); }
	public void setGodEnabled(boolean god) { this.god = god; savePlayerData(); }
	public void setFlyEnabled(boolean fly) { this.fly = fly; savePlayerData(); }
	public void setMuted(boolean mute) { this.mute = mute; savePlayerData(); }
	public void setIgnoringTeleportRequests(boolean notp) { this.notp = notp; savePlayerData(); }

	public void jail(String jailName) {
		// TODO Jail player
		savePlayerData();
	}
	public void unjail() {
		// TODO Unjail player
		if (player.isOnline()) ((Player) player).teleport(preJailLoc);
		savePlayerData();
	}

	public void toggleVanish() { setVanish(!isVanished()); }
	public void setVanish(boolean vanish) { if (vanish) vanish(); else unvanish(); }
	public void vanish() {
		if (!player.isOnline()) return;
		Player p1 = (Player) player;
		//p1.setPlayerListName("");
		for (Player p2 : Bukkit.getOnlinePlayers()) {
			if (!modInst.perm(p2, "seevanished")) p2.hidePlayer(p1);
		}
		this.vanish = true;
		savePlayerData();
	}
	public void unvanish() {
		if (!player.isOnline()) return;
		Player p1 = (Player) player;
		//p1.setPlayerListName(p1.getName());
		for (Player p2 : Bukkit.getOnlinePlayers()) { p2.showPlayer(p1); }
		this.vanish = false;
		savePlayerData();
	}

	public void toggleFreeze() { setFrozen(!frozen); }
	public void setFrozen(boolean frozen) { if (frozen) unfreeze(); else freeze(); }
	public void freeze() {
		frozen = true;
		// TODO Freeze player
		savePlayerData();
	}
	public void unfreeze() {
		frozen = false;
		// TODO Unfreeze player
		savePlayerData();
	}

	/* ********************************
	 * **          PRIVATE           **
	 * ******************************** */

	private void flush() {
		homes.clear();
		tpIgnore.clear();
		// TODO Clear data from lists/maps
	}

	private void savePlayerData() { config.save(); }

	private void reloadPlayerData() { config.reload(); }

	/* ********************************
	 * **           STATIC           **
	 * ******************************** */

	public static CarbonPlayer getCarbonPlayer(OfflinePlayer pl, boolean loadOffline) {
		if (pl.isOnline() && loaded.containsKey(pl)) { return loaded.get(pl); }
		if ((!pl.isOnline() && loadOffline) || (pl.isOnline() && !loaded.containsKey(pl))) return new CarbonPlayer(pl);
		return null;
	}

	public static void flushCarbonPlayer(OfflinePlayer pl) {
		if (loaded.containsKey(pl)) {
			loaded.get(pl).flush();
			loaded.remove(pl);
		}
	}

}
