package templates;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;

@SuppressWarnings("UnusedDeclaration")
public class CommandTemplate extends ModuleCmd {

	public CommandTemplate(Module module) { super(module, "CmdLabel"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		sender.sendMessage("Not yet implemented");
	}

}
