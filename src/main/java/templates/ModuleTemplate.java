package templates;

import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;

@SuppressWarnings("UnusedDeclaration")
public class ModuleTemplate extends Module {
	public ModuleTemplate() throws DuplicateModuleException { super("Template", "temp"); }
	public void initModule() {
		registerListeners();
	}
	public void disableModule() {
		unregisterListeners();
	}
	public void reloadModule() {
		disableModule();
		initModule();
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
