package net.teamcarbon.carbonkit.commands.CarbonCore;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class CarbonReloadCommand extends ModuleCmd {

	public CarbonReloadCommand(Module module) { super(module, "carbonreload"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!mod.perm(sender, "reload")) {
			sender.sendMessage(mod.getCoreMsg("no-perm", false));
			return;
		}
		HashMap<String, String> rep = new HashMap<>();
		CarbonKit.reloadAllConfigs();
		if (args.length > 0) {
			Module m = Module.getModule(args[0]);
			if (m != null) {
				m.reloadModule();
				rep.put("{MODULE}", m.getName());
				sender.sendMessage(mod.getCoreMsg("reloaded", true, rep));
			} else {
				sender.sendMessage(mod.getCoreMsg("no-module", true));
			}
		} else {
			CarbonKit.inst.loadPlugin(System.currentTimeMillis());
			rep.put("{MODULE}", "all modules");
			sender.sendMessage(mod.getCoreMsg("reloaded", true, rep));
		}
	}

}
