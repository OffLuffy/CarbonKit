package me.offluffy.carbonkit.modules;

import me.offluffy.carbonkit.utils.DuplicateModuleException;
import me.offluffy.carbonkit.utils.Module;

public class ModuleCore extends Module {
	public ModuleCore() throws DuplicateModuleException {
		super("Core", "carbonkit", "ckcore");
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
