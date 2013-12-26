package templates;

import me.offluffy.carbonkit.utils.DuplicateModuleException;
import me.offluffy.carbonkit.utils.Module;

public class ModuleTemplate extends Module {
	public ModuleTemplate() throws DuplicateModuleException {
		super("Template", "temp");
	}

	@Override
	public void initModule() {}

	@Override
	public void disableModule() {}

	@Override
	protected boolean hasListeners() { return false; }

	@Override
	public boolean hasDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
}
