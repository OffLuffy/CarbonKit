package net.teamcarbon.carbonkit;

import net.milkbowl.vault.chat.Chat;
import net.teamcarbon.carbonkit.events.coreEvents.FinishModuleLoadingEvent;
import net.teamcarbon.carbonkit.modules.*;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.*;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.teamcarbon.carbonlib.Misc.CarbonException;
import net.teamcarbon.carbonlib.Misc.ConfigAccessor;
import net.teamcarbon.carbonlib.Misc.Log;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class CarbonKit extends JavaPlugin implements Listener {

	public static String NMS_VER;
	public static Log log;
	public static boolean checkOffline;
	private static List<Class<? extends Module>> modules;

	public enum ConfType {
		DATA("data.yml"), MESSAGES("messages.yml"), TRIVIA("trivia.yml"), HELP("help.yml"), NEWS("news.yml");
		private String fn;
		private ConfigAccessor ca;
		private boolean init = false;
		ConfType(String fileName) { fn = fileName; }
		public void initConfType() {
			ca = new ConfigAccessor(CarbonKit.inst, fn);
			init = true;
		}
		public FileConfiguration getConfig() { return ca.config(); }
		public void saveConfig() { ca.save(); }
		public void reloadConfig() { ca.reload(); }
		public boolean isInitialized() { return init; }
	}
	public static CarbonKit inst;
	public static PluginManager pm;
	public static Permission perms;
	public static Economy econ;
	public static Chat chat;

	/* ====================================
	=====[         OVERRIDES         ]=====
	===================================== */

	@SuppressWarnings("unchecked")
	@Override
	public void onEnable() {
		NMS_VER = Bukkit.getServer().getClass().getPackage().getName();
		NMS_VER = NMS_VER.substring(NMS_VER.lastIndexOf('.') + 1);
		modules = new ArrayList<Class<? extends Module>>();
		Collections.addAll(modules, CarbonCoreModule.class, CarbonCraftingModule.class,
				CarbonWatcherModule.class, CarbonEssentialsModule.class, CarbonPerksModule.class,
				CarbonSmiteModule.class, CarbonToolsModule.class, CarbonSkullsModule.class, CarbonVoteModule.class,
				CarbonTriviaModule.class, CarbonNewsModule.class/*, EssentialsAssistModule.class*/);
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			public void run() {
				enablePlugin();
			}
		}, 5L);
	}
	@Override
	public void onDisable() {
		saveAllConfigs();
		CarbonCoreModule.inst.disableModule();
	}

	/* ====================================
	=====[         LISTENERS         ]=====
	===================================== */

	/* ====================================
	=====[          METHODS          ]=====
	===================================== */

	private void enablePlugin() {
		try {
			CarbonException.setGlobalPluginScope(this, "net.teamcarbon");
			long time = System.currentTimeMillis();
			CarbonLib.notifyHook(this);
			inst = this;
			pm = Bukkit.getPluginManager();
			saveDefaultConfig();
			log = new Log(this, "core.enable-debug-messages");
			log.debug("Log initialized and files loaded after " + (System.currentTimeMillis() - time) + "ms");
			if (!setupPermissions() || !setupEconomy() || !setupChat()) {
				log.severe("Couldn't find Vault! Disabling CarbonKit.");
				pm.disablePlugin(this);
				return;
			}
			log.debug("Hooked to Vault after " + (System.currentTimeMillis() - time) + "ms");
			loadPlugin(time);
		} catch (Exception e) {
			System.out.println("===[ An exception occured while trying to enable CarbonKit ]===");
			(new CarbonException(this, e)).printStackTrace();
			log.severe("=====================================");
		}
	}

	/**
	 * Loads data (or reloads if it has already been loaded)
	 * @param startTime The time the load process began (for logging purposes)
	 */
	public static void loadPlugin(long startTime) {
		if (Module.getAllModules().size() > 0) { // Modules already loaded. Prep for reload
			CarbonCoreModule.inst.disableModule();
			Module.flushData();
		}
		CarbonKit.inst.reloadConfig();
		checkOffline = getDefConfig().getBoolean("core.match-offline-players", true);
		for (ConfType ct : ConfType.values()) if (ct.isInitialized()) { ct.reloadConfig(); } else { ct.initConfType(); }
		CustomMessage.loadMessages();
		List<Long> times = new ArrayList<Long>();
		List<Module> enabledModules = new ArrayList<Module>(), disabledModules = new ArrayList<Module>();
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
						/*log.debug(m.getName() + " enabled after " + (dtime - startTime) + "ms, took " + (dtime - mtime) + "ms to load.");
						if (!log.isDebugEnabled())
							log.info(m.getName() + " module initialized");*/
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
		log.debug("Enabled for NMS version " + NMS_VER + " in " + (System.currentTimeMillis() - startTime) + "ms." + avgText);
		pm.callEvent(new FinishModuleLoadingEvent(enabledModules, disabledModules));
	}
	public static FileConfiguration getDefConfig() { return CarbonKit.inst.getConfig(); }
	public static FileConfiguration getConfig(ConfType ct) { return ct.getConfig(); }
	public static void saveConfig(ConfType ct) { ct.saveConfig(); }
	public static void saveDefConfig() { inst.saveConfig(); }
	public static void saveAllConfigs() {
		inst.saveConfig();
		for (ConfType ct : ConfType.values()) ct.saveConfig();
	}
	public static void reloadDefConfig() { CarbonKit.inst.reloadConfig(); }
	public static void reloadConfig(ConfType ct) { ct.reloadConfig(); }
	public static void reloadAllConfigs() {
		inst.reloadConfig();
		for (ConfType ct : ConfType.values()) ct.reloadConfig();
	}
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> pp = Bukkit.getServicesManager().getRegistration(Permission.class);
		if (pp != null)
			perms = pp.getProvider();
		MiscUtils.setPerms(perms);
		return perms != null;
	}
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> ep = getServer().getServicesManager().getRegistration(Economy.class);
		if (ep != null)
			econ = ep.getProvider();
		return econ != null;
	}
	private boolean setupChat() {
		RegisteredServiceProvider<Chat> ep = getServer().getServicesManager().getRegistration(Chat.class);
		if (ep != null)
			chat = ep.getProvider();
		return chat != null;
	}
}
