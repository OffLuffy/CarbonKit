package net.teamcarbon.carbonkit.commands.Misc;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.CarbonException;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class HelpCommand extends ModuleCmd {

	public HelpCommand(Module module) { super(module, "help"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		final String PRE = "carbonkit.help.";
		FileConfiguration h = CarbonKit.getConfig(ConfType.HELP);
		ConfigurationSection cats = h.getConfigurationSection("categories");
		if (args.length > 0) {
			String cat = args[0];
			for (String c : cats.getKeys(false)) {
				if (MiscUtils.eq(cat, c)) {
					send(sender, cats.getConfigurationSection(c), PRE + c);
					return;
				}
			}
			sender.sendMessage(Clr.RED + "The specified category was not found!");
		} else { send(sender, h.getConfigurationSection("root-menu"), PRE + "rootmenu"); }
	}


	private void send(CommandSender sender, ConfigurationSection sect, String perm) {
		try {
			if (!sect.getBoolean("require-permission", true) || MiscUtils.perm(sender, perm)) {
				if (sect.contains("header"))
					sender.sendMessage(Clr.trans(sect.getString("header")));
				if (sect.contains("lines"))
					for (String s : sect.getStringList("lines"))
						if (s != null && !s.isEmpty()) { sender.sendMessage(Clr.trans(s)); }
				if (sect.contains("extra-perm-lines")) {
					ConfigurationSection ep = sect.getConfigurationSection("extra-perm-lines");
					if (ep != null && !ep.getKeys(false).isEmpty())
						for (String s : ep.getKeys(false))
							if (!ep.getStringList(s).isEmpty() && MiscUtils.perm(sender, s.replace("_", ".")))
								for (String l : ep.getStringList(s))
									if (l != null && !l.isEmpty())
										sender.sendMessage(Clr.trans(l));
				}
				if (sect.contains("footer"))
					sender.sendMessage(Clr.trans(sect.getString("footer")));
			} else {
				for (String s : sect.getStringList("no-perm-lines")) { if (s != null && !s.isEmpty()) { sender.sendMessage(Clr.trans(s)); } }
			}
		} catch (Exception e) { (new CarbonException(CarbonKit.inst, e)).printStackTrace(); }
	}

}
