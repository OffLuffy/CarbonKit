package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.commands.Core.CarbonKitReloadCommand;
import net.teamcarbon.carbonkit.commands.Core.CarbonKitToggleCommand;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;

@SuppressWarnings("UnusedDeclaration")
public class CoreModule extends Module {
	public CoreModule() throws DuplicateModuleException { super("CarbonKit", "ckit", "core", "ck"); }
	public static CoreModule inst;
	public void initModule() {
		inst = this;
		addCmd(new CarbonKitReloadCommand(this));
		addCmd(new CarbonKitToggleCommand(this));
		//addCmd(new CarbonKitHelpCommand(this));
	}
	public void disableModule() {
		for (Module m : Module.getAllModules())
			if (!(m instanceof CoreModule)) m.disableModule();
	}
	public void reloadModule() {
		CarbonKit.reloadAllConfigs();
		for (Module m : Module.getAllModules())
			if (m.isEnabled() && !(m instanceof CoreModule)) m.reloadModule();
	}
	protected boolean needsListeners() { return false; }
	public boolean hasAllDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
}
