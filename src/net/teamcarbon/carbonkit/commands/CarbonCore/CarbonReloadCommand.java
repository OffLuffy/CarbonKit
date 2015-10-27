package net.teamcarbon.carbonkit.commands.CarbonCore;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Misc.MiscUtils;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class CarbonReloadCommand extends ModuleCmd {

	public CarbonReloadCommand(Module module) { super(module, "carbonreload"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!MiscUtils.perm(sender, "carbonkit.reload")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		HashMap<String, String> rep = new HashMap<String, String>();
		if (args.length > 0) {
			if (Module.getModule(args[0]) != null) {
				CarbonKit.reloadAllConfigs();
				Module.getModule(args[0]).reloadModule();
				rep.put("{MODULE}", Module.getModule(args[0]).getName());
				sender.sendMessage(CustomMessage.CORE_RELOADED.pre(rep));
			} else {
				sender.sendMessage(CustomMessage.CORE_NOT_MODULE.pre());
			}
		} else {
			CarbonKit.loadPlugin(System.currentTimeMillis());
			rep.put("{MODULE}", "all modules");
			sender.sendMessage(CustomMessage.CORE_RELOADED.pre(rep));
		}
	}

}
