package net.teamcarbon.carbonkit.commands.CarbonCore;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.modules.CarbonCoreModule;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonlib.Misc.TypeUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class CarbonToggleCommand extends ModuleCmd {

	public CarbonToggleCommand(Module module) { super(module, "carbontoggle"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!mod.perm(sender, "toggle", "list")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		if (args.length > 0) {
			if (!mod.perm(sender, "toggle")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
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
				if (state) {
					if (!m.isEnabled()) {
						sender.sendMessage(CustomMessage.CORE_MODULE_ENABLED.pre(rep));
					} else {
						sender.sendMessage(CustomMessage.CORE_MODULE_ALREADY_ENABLED.pre(rep));
					}
				} else if (!state) {
					if (m.isEnabled()) {
						sender.sendMessage(CustomMessage.CORE_MODULE_DISABLED.pre(rep));
					} else {
						sender.sendMessage(CustomMessage.CORE_MODULE_ALREADY_DISABLED.pre(rep));
					}
				}
				m.setEnabled(state);
				CarbonKit.log.info(sender.getName() + " has " + (state ? "en" : "dis") + "abled the " + m.getName() + " module");
			} else {
				sender.sendMessage(CustomMessage.CORE_NOT_MODULE.pre());
			}
		} else {
			if (!mod.perm(sender, "list")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			CustomMessage.printHeader(sender, "CarbonKit Modules");
			for (Module m : Module.getAllModules())
				if (!(m instanceof CarbonCoreModule))
					sender.sendMessage((m.isEnabled()? Clr.LIME + "[ON] ":Clr.RED + "[OFF] ") + Clr.AQUA + m.getName());
		}
	}
}
