package me.offluffy.carbonkit.cmds;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.modules.ModuleEssPurge;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Log;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Clr;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CommandEssPurge extends ModuleCmd {
	public CommandEssPurge(Module module) {
		super(module, "essentialspurge");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!Lib.perm(sender,"carbonkit.purge.essentials")) {
			Messages.send(sender, Message.NO_PERM);
			return true;
		}
		try {
			if (args.length > 0) {
				if (Lib.eq(args[0],"confirm")) {
					if (!ModuleEssPurge.epConf.containsKey(sender.getName())) {
						sender.sendMessage(Clr.ERR + "There is nothing to confirm!");
						return true;
					} else
						args = ModuleEssPurge.epConf.get(sender.getName());
					ModuleEssPurge.purge(sender, args);
				} else {
					ModuleEssPurge.epConf.put(sender.getName(), args);
					String argString = "";
					for (String a : args)
						argString = argString + a;
					boolean em = false;
					if (argString.indexOf("-e") != -1)
						em = true;
					long threshold = ModuleEssPurge.parseMilliseconds(argString);
					String timeString = ModuleEssPurge.getTimeString(threshold);
					sender.sendMessage(Clr.TITLE + "EssentialsPurge Warning!");
					sender.sendMessage(Clr.ERR + "This will remove all " + ((em)?"empty users and ":"") + "users inactive for at least " + timeString);
					sender.sendMessage(Clr.ERR + "Use \"/ep confirm\" if you are SURE you want to do this!");
					return true;
				}
			} else {
				sender.sendMessage(Clr.NORM + "/epurge [-e] [#hours|#days|#months|#years]");
				sender.sendMessage(Clr.NORM + "The \"-e\" flag will also purge \"empty\" user files regardless of age");
				return true;
			}
		} catch (Exception e) {
			sender.sendMessage(Clr.ERR + "Purge failed. See the console for more details.");
			Log.warn("EssentialsPurge failed to purge! Details:");
			e.printStackTrace();
			Log.warn("========[ End of EssPurge Report ]========");
		}

		return true;
	}
	
	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("carbonkit.permission"));
		return perms;
	}

}
