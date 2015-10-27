package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.commands.CarbonCore.CarbonReloadCommand;
import net.teamcarbon.carbonkit.commands.CarbonCore.CarbonToggleCommand;
import net.teamcarbon.carbonkit.events.coreEvents.FinishModuleLoadingEvent;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonlib.Misc.LagMeter;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

@SuppressWarnings("UnusedDeclaration")
public class CarbonCoreModule extends Module {

	public CarbonCoreModule() throws DuplicateModuleException { super("CarbonKit", "ckit", "core", "ck"); }
	public static CarbonCoreModule inst;
	public void initModule() {
		inst = this;
		addCmd(new CarbonReloadCommand(this));
		addCmd(new CarbonToggleCommand(this));
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(CarbonKit.inst, new LagMeter(), 20L, 1L);
	}
	public void disableModule() {
		for (Module m : Module.getAllModules())
			if (!(m instanceof CarbonCoreModule)) m.disableModule();
	}
	public void reloadModule() {
		CarbonKit.reloadAllConfigs();
		for (Module m : Module.getAllModules())
			if (m.isEnabled() && !(m instanceof CarbonCoreModule)) m.reloadModule();
	}
	protected boolean needsListeners() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/
	@EventHandler
	public void finishedLoading(FinishModuleLoadingEvent e) {
		String enabled = "enabled: ", disabled = "disabled: ";
		enabled += MiscUtils.stringFromArray(", ", e.getEnabledModules());
		disabled += MiscUtils.stringFromArray(", ", e.getDisabledModules());
		CarbonKit.log.info("CarbonKit finished loading modules.");
		CarbonKit.log.log(Clr.LIME + enabled);
		CarbonKit.log.log(Clr.RED + disabled);
	}

	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
}
