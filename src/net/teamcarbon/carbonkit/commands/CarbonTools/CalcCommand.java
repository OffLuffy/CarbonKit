package net.teamcarbon.carbonkit.commands.CarbonTools;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

@SuppressWarnings("UnusedDeclaration")
public class CalcCommand extends ModuleCmd {
	public CalcCommand(Module module) { super(module, "calc"); }
	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!mod.perm(sender, "calc")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		String equation = MiscUtils.stringFromArray(" ", args);
		try {
			ScriptEngineManager mgr = new ScriptEngineManager();
			ScriptEngine engine = mgr.getEngineByName("JavaScript");
			sender.sendMessage(Clr.AQUA + equation + " = " + Clr.LIME + engine.eval(equation));
		} catch(Exception e) {
			sender.sendMessage(Clr.RED + "Failed to solve: " + equation);
		}
	}
}
