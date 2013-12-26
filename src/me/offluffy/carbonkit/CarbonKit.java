package me.offluffy.carbonkit;

import java.util.ArrayList;
import java.util.List;

import me.offluffy.carbonkit.modules.ModuleBorders;
import me.offluffy.carbonkit.modules.ModuleCKWatcher;
import me.offluffy.carbonkit.modules.ModuleCmdBlockTools;
import me.offluffy.carbonkit.modules.ModuleCore;
import me.offluffy.carbonkit.modules.ModuleEssAssist;
import me.offluffy.carbonkit.modules.ModuleEssPurge;
import me.offluffy.carbonkit.modules.ModuleGPFlags;
import me.offluffy.carbonkit.modules.ModuleGoldenSmite;
import me.offluffy.carbonkit.modules.ModuleMisc;
import me.offluffy.carbonkit.modules.ModuleSkullShop;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Log;
import me.offluffy.carbonkit.utils.Module;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CarbonKit extends JavaPlugin {
	
	public static CarbonKit inst;
	public static PluginManager pm;
	public static FileConfiguration config, data, borders;
	public static Permission perms;
	public static Economy econ;
	
	private static String[] files = new String[]{"config.yml","data.yml","borders.yml"};
	
	private enum ModuleClass {
		CORE(ModuleCore.class), BORDERS(ModuleBorders.class), ESSASSIST(ModuleEssAssist.class),
		MISC(ModuleMisc.class), SKULLSHOP(ModuleSkullShop.class), CKWATCHER(ModuleCKWatcher.class),
		GOLDENSMITE(ModuleGoldenSmite.class), CBTOOLS(ModuleCmdBlockTools.class), GPFLAGS(ModuleGPFlags.class),
		EPURGE(ModuleEssPurge.class);
		Class<? extends Module> mClass;
		ModuleClass(Class<? extends Module> mClass) { this.mClass = mClass; }
		public Class<? extends Module> moduleClass() { return mClass; }
	}
	
	@Override
	public void onEnable() {
		long time = System.currentTimeMillis();
		inst = this;
		pm = Bukkit.getPluginManager();
		
		Lib.initFiles(files);
		config = Lib.loadFile(files[0]);
		data = Lib.loadFile(files[1]);
		borders = Lib.loadFile(files[2]);
		
		Log.debug("Files loaded after " + (System.currentTimeMillis()-time) + "ms");
		
		if (!setupPermissions() || !setupEconomy()) {
			Log.severe("Couldn't find Vault! Disabling CarbonKit.");
			pm.disablePlugin(this);
			return;
		}
		
		Log.debug("Hooked to Vault after " + (System.currentTimeMillis()-time) + "ms");
		
		List<Long> times = new ArrayList<Long>();
		for (ModuleClass mc : ModuleClass.values() ) {
			try {
				long mtime = System.currentTimeMillis();
				Module m = mc.moduleClass().newInstance();
				if (m instanceof ModuleCore || m.isEnabled()) {
					if (m.hasDependencies()) {
						m.initModule();
						long dtime = System.currentTimeMillis();
						Log.debug(m.getName() + " enabled after " + (dtime-time) + "ms, took " + (dtime-mtime) + "ms to load." );
						Log.info(m.getName() + " module initialized");
						times.add(dtime-mtime);
					} else {
						Log.warn(m.getName() + " is missing required dependencies.");
					}
				}
			} catch (Exception e) {
				Log.severe("An exception occurred while trying to enable a module!");
				e.printStackTrace();
			}
		}
		
		String avgText = " No modules were enabled.";
		if (times.size() > 0) {
			long avg = 0L;
			for (Long l : times)
				avg += l;
			avg = avg/times.size();
			avgText = " Average module load time was " + avg + "ms.";
		}
		Log.debug("Enabled in " + (System.currentTimeMillis()-time) + "ms." + avgText);
	}
	
	@Override
	public void onDisable() {
		Log.log("Disabled");
	}
	
	private boolean setupPermissions() {
		RegisteredServiceProvider<Permission> pp = Bukkit.getServicesManager().getRegistration(Permission.class);
		if (pp != null)
			perms = pp.getProvider();
		return perms != null;
	}
	
	private boolean setupEconomy() {
		RegisteredServiceProvider<Economy> ep = getServer().getServicesManager().getRegistration(Economy.class);
		if (ep != null)
			econ = ep.getProvider();
		return econ != null;
	}
}
