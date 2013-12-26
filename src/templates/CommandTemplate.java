package templates;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandTemplate extends ModuleCmd {

	public CommandTemplate(Module module) {
		super(module, "CmdLabel");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return true;
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("carbonkit.permission"));
		return perms;
	}

}
