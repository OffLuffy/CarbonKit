package me.offluffy.carbonkit.modules;

import me.offluffy.carbonkit.cmds.CommandCmdBlockClear;
import me.offluffy.carbonkit.cmds.CommandCmdBlockItem;
import me.offluffy.carbonkit.cmds.CommandCmdBlockKill;
import me.offluffy.carbonkit.cmds.CommandCmdBlockSpawn;
import me.offluffy.carbonkit.utils.DuplicateModuleException;
import me.offluffy.carbonkit.utils.Module;

public class ModuleCmdBlockTools extends Module {
	public ModuleCmdBlockTools() throws DuplicateModuleException {
		super("CommandBlockTools", "cmdblocktools", "cbtools", "cbt");
	}

	@Override
	public void initModule() {
		addCmd(new CommandCmdBlockClear(this));
		addCmd(new CommandCmdBlockItem(this));
		addCmd(new CommandCmdBlockKill(this));
		addCmd(new CommandCmdBlockSpawn(this));
	}

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
