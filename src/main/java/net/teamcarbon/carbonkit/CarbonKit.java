package net.teamcarbon.carbonkit;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.teamcarbon.carbonkit.events.coreEvents.FinishModuleLoadingEvent;
import net.teamcarbon.carbonkit.modules.*;
import net.teamcarbon.carbonkit.utils.*;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class CarbonKit extends JavaPlugin implements Listener {

	public static CarbonKit inst;
	public static String NMS_VER;
	public static boolean checkOffline;
	public static Log log;
	public static PluginManager pm;
	public static Economy econ;
	public static Permission perm;

	private static List<Class<? extends Module>> modules;
	private static HashMap<UUID, UserStore> cachedUserData;

	public enum ConfType {
		DATA("data.yml"), MESSAGES("messages.yml"), TRIVIA("trivia.yml"), HELP("help.yml"), NEWS("news.yml");
		private String fn;
		private YamlConfig yc;
		private boolean init = false;
		ConfType(String fileName) { fn = fileName; }
		public void initConfType() {
			File dest = new File(inst.getDataFolder(), fn);
			yc = new YamlConfig(CarbonKit.inst, dest, "yml/" + fn);
			init = true;
		}
		public FileConfiguration getConfig() { return yc; }
		public void saveConfig() { yc.save(); }
		public void reloadConfig() { yc.reload(); }
		public boolean isInitialized() { return init; }
	}

	/* ====================================
	=====[         OVERRIDES         ]=====
	===================================== */

	public String getDebugPath() { return "core.enable-debug-messages"; }

	public void enablePlugin() {
		inst = this;
		NMS_VER = Bukkit.getServer().getClass().getPackage().getName();
		NMS_VER = NMS_VER.substring(NMS_VER.lastIndexOf('.') + 1);
		modules = new ArrayList<>();
		cachedUserData = new HashMap<>();
		Collections.addAll(modules,
				CarbonCoreModule.class, CarbonWatcherModule.class,
				CarbonSmiteModule.class, CarbonToolsModule.class, CarbonVoteModule.class,
				CarbonTriviaModule.class, CarbonNewsModule.class, EssentialsAssistModule.class
		);
		long time = System.currentTimeMillis();
		saveDefaultConfig();
		log.debug("Hooked to Vault after " + (System.currentTimeMillis() - time) + "ms");
		loadPlugin(time);
	}
	public void disablePlugin() {
		saveAllConfigs();
		CarbonCoreModule.inst.disableModule();
	}

	/* ====================================
	=====[          METHODS          ]=====
	===================================== */

	/**
	 * Loads data (or reloads if it has already been loaded)
	 * @param startTime The time the load process began (for logging purposes)
	 */
	public void loadPlugin(long startTime) {
		if (Module.getAllModules().size() > 0) {	// Modules already loaded. Prep for reload
			CarbonCoreModule.inst.disableModule();	// Disables all modules by disabling core module
			Module.flushData();						// Flushes static data stored in each module
		}
		reloadConfig();
		for (ConfType ct : ConfType.values()) if (ct.isInitialized()) { ct.reloadConfig(); } else { ct.initConfType(); }
		checkOffline = getConfig().getBoolean("core.match-offline-players", true);
		CustomMessage.loadMessages();
		List<Long> times = new ArrayList<>();
		List<Module> enabledModules = new ArrayList<>(), disabledModules = new ArrayList<>();
		for (Class<? extends Module> mc : modules) {
			String name = mc.getSimpleName();
			try {
				long mtime = System.currentTimeMillis();
				Module m = mc.newInstance();
				name = m.getName();
				if (m.isEnabled()) {
					enabledModules.add(m);
					if (!(m instanceof CarbonCoreModule)) {
						long dtime = System.currentTimeMillis();
						log.debug(m.getName() + " enabled after " + (dtime - startTime) + "ms, took " + (dtime - mtime) + "ms to load.");
						if (!log.isDebugEnabled()) log.info(m.getName() + " module initialized");
						times.add(dtime - mtime);
					}
				} else { disabledModules.add(m); }
			} catch (Exception e) {
				log.severe("===[ An exception occurred while trying to enable module: " + name + " ]===");
				e.printStackTrace();
				log.severe("=====================================");
			}
		}
		String avgText = " No modules were enabled.";
		if (times.size() > 0) {
			long avg = 0L;
			for (Long l : times)
				avg += l;
			avg = avg / times.size();
			avgText = " Average module load time was " + avg + "ms.";
		}
		Collection<? extends Player> opl = Bukkit.getOnlinePlayers();
		flushPlayerDataCache();
		if (opl.size() > 0) {
			log.debug("Caching data for " + opl.size() + " player" + (opl.size() != 0 ? "s" : ""));
			for (Player pl : Bukkit.getOnlinePlayers()) {
				cachedUserData.put(pl.getUniqueId(), new UserStore(pl));
			}
		}
		log.debug("Enabled for NMS version " + NMS_VER + " in " + (System.currentTimeMillis() - startTime) + "ms." + avgText);
		pm.callEvent(new FinishModuleLoadingEvent(enabledModules, disabledModules));
	}

	private static boolean setupPerm() {
		RegisteredServiceProvider<Permission> pp = Bukkit.getServicesManager().getRegistration(Permission.class);
		RegisteredServiceProvider<Economy> ep = Bukkit.getServicesManager().getRegistration(Economy.class);
		if (pp != null) perm = pp.getProvider();
		if (ep != null) econ = ep.getProvider();
		MiscUtils.setPerms(perm);
		return perm != null && econ != null;
	}

	// User Data
	public static boolean isPlayerDataCached(UUID id) { return cachedUserData.containsKey(id); }
	public static void cachePlayerData(UUID id) { if (!isPlayerDataCached(id)) cachedUserData.put(id, new UserStore(id)); }
	public static void uncachePlayerData(UUID id) { if (isPlayerDataCached(id)) cachedUserData.remove(id); }
	public static UserStore getPlayerData(UUID id) {
		if (isPlayerDataCached(id)) return cachedUserData.get(id);
		UserStore us = new UserStore(id);
		cachedUserData.put(id, us);
		return us;
	}
	public static void flushPlayerDataCache() { cachedUserData.clear(); }

	// Config
	public static FileConfiguration getConfig(ConfType ct) { return ct.getConfig(); }
	public static void saveConfig(ConfType ct) { ct.saveConfig(); }
	public static void saveAllConfigs() {
		inst.saveConfig();
		for (ConfType ct : ConfType.values()) ct.saveConfig();
	}
	public static void reloadConfig(ConfType ct) { ct.reloadConfig(); }
	public static void reloadAllConfigs() {
		inst.reloadConfig();
		for (ConfType ct : ConfType.values()) ct.reloadConfig();
	}
}
