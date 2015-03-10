package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.commands.CmdBlockTools.CmdBlockItemCommand;
import net.teamcarbon.carbonkit.commands.CmdBlockTools.CmdBlockKillCommand;
import net.teamcarbon.carbonkit.commands.CmdBlockTools.CmdBlockClearCommand;
import net.teamcarbon.carbonkit.commands.CmdBlockTools.CmdBlockSpawnCommand;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;

@SuppressWarnings("UnusedDeclaration")
public class CmdBlockToolsModule extends Module {
	public CmdBlockToolsModule() throws DuplicateModuleException { super("CmdBlockTools", "commandblocktools", "cbtools", "cblocktools", "cbt"); }
	public static CmdBlockToolsModule inst;
	public void initModule() {
		inst = this;
		addCmd(new CmdBlockClearCommand(this));
		addCmd(new CmdBlockItemCommand(this));
		addCmd(new CmdBlockKillCommand(this));
		addCmd(new CmdBlockSpawnCommand(this));
	}
	public void disableModule() {

	}
	public void reloadModule() {
		disableModule();
		CarbonKit.reloadDefConfig();
		CarbonKit.reloadConfig(ConfType.DATA);
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
