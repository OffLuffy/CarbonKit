package net.teamcarbon.carbonkit.commands.Core;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.modules.CoreModule;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.MiscUtils;

import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class CarbonReloadCommand extends ModuleCmd {

	public CarbonReloadCommand(Module module) { super(module, "carbonreload"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!MiscUtils.perm(sender, "carbonkit.reload")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		CarbonKit.loadPlugin(System.currentTimeMillis());
		sender.sendMessage(CustomMessage.CORE_RELOADED.pre());
	}

}
