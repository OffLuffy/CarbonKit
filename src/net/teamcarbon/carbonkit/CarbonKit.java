package net.teamcarbon.carbonkit;

import net.teamcarbon.carbonkit.events.coreEvents.FinishModuleLoadingEvent;
import net.teamcarbon.carbonkit.modules.*;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.UserStore;
import net.teamcarbon.carbonlib.*;
import net.teamcarbon.carbonlib.Misc.*;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.io.File;
import java.util.*;

@SuppressWarnings("UnusedDeclaration")
public class CarbonKit extends CarbonPlugin implements Listener {

	public static String NMS_VER;
	public static boolean checkOffline;
	private static List<Class<? extends Module>> modules;
	private static HashMap<UUID, UserStore> cachedUserData;
	private static CarbonKit inst;

	public enum ConfType {
		DATA("data.yml"), MESSAGES("messages.yml"), TRIVIA("trivia.yml"), HELP("help.yml"), NEWS("news.yml");
		private String fn;
		//private ConfigAccessor ca;
		private YamlConfig yc;
		private boolean init = false;
		ConfType(String fileName) { fn = fileName; }
		public void initConfType() {
			File dest = new File(CarbonKit.inst.getDataFolder(), fn);
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
		inst = (CarbonKit) getPlugin();
		NMS_VER = Bukkit.getServer().getClass().getPackage().getName();
		NMS_VER = NMS_VER.substring(NMS_VER.lastIndexOf('.') + 1);
		modules = new ArrayList<>();
		cachedUserData = new HashMap<>();
		Collections.addAll(modules,
				CarbonCoreModule.class, CarbonCraftingModule.class,
				CarbonWatcherModule.class/*, CarbonEssentialsModule.class, CarbonPerksModule.class*/,
				CarbonSmiteModule.class, CarbonToolsModule.class, CarbonSkullsModule.class, CarbonVoteModule.class,
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
		if (Module.getAllModules().size() > 0) { // Modules already loaded. Prep for reload
			CarbonCoreModule.inst.disableModule();
			Module.flushData();
		}
		reloadConf();
		for (ConfType ct : ConfType.values()) if (ct.isInitialized()) { ct.reloadConfig(); } else { ct.initConfType(); }
		checkOffline = getConf().getBoolean("core.match-offline-players", true);
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
				(new CarbonException(inst, e)).printStackTrace();
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
		pm().callEvent(new FinishModuleLoadingEvent(enabledModules, disabledModules));
	}

	public static CarbonKit inst() { return inst; }
	public static Log log() { return inst.log; }

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
