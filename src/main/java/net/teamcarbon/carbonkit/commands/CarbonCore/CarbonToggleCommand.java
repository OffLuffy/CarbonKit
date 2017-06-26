package net.teamcarbon.carbonkit.commands.CarbonCore;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.modules.CarbonCoreModule;
import net.teamcarbon.carbonkit.utils.MiscUtils;
import net.teamcarbon.carbonkit.utils.TypeUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonkit.utils.Messages.Clr;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class CarbonToggleCommand extends ModuleCmd {

	public CarbonToggleCommand(Module module) { super(module, "carbontoggle"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!mod.perm(sender, "toggle", "list")) {
			sender.sendMessage(mod.getCoreMsg("no-perm", false));
			return;
		}
		if (args.length > 0) {
			if (!mod.perm(sender, "toggle")) {
				sender.sendMessage(mod.getCoreMsg("no-perm", false));
				return;
			}
			Module m = Module.getModule(args[0]);
			if (m != null) {
				if (m instanceof CarbonCoreModule) {
					sender.sendMessage(Clr.RED + "This module cannot be disabled!");
					return;
				}
				boolean state = !m.isEnabled();
				if (args.length > 1 && TypeUtils.isBoolean(args[1]))
					state = TypeUtils.toBoolean(args[1]);
				HashMap<String, String> rep = new HashMap<>();
				rep.put("{MODULENAME}", m.getName());
				sender.sendMessage(mod.getCoreMsg(
						"module-" + (m.isEnabled() == state ? "already-" : "") + (state ? "enabled" : "disabled"),
						true, rep));
				m.setEnabled(state);
				CarbonKit.log.info(sender.getName() + " has " + (state ? "en" : "dis") + "abled the " + m.getName() + " module");
			} else {
				sender.sendMessage(mod.getCoreMsg("no-module", true));
			}
		} else {
			if (!mod.perm(sender, "list")) {
				sender.sendMessage(mod.getCoreMsg("no-perm", false));
				return;
			}
			MiscUtils.printHeader(sender, "CarbonKit Modules");
			for (Module m : Module.getAllModules())
				if (!(m instanceof CarbonCoreModule))
					sender.sendMessage((m.isEnabled()? Clr.LIME + "[ON] ":Clr.RED + "[OFF] ") + Clr.AQUA + m.getName());
		}
	}
}
