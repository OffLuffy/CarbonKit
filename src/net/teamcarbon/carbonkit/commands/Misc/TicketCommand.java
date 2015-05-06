package net.teamcarbon.carbonkit.commands.Misc;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import org.bukkit.entity.Player;

import java.util.Locale;

@SuppressWarnings("UnusedDeclaration")
public class TicketCommand extends ModuleCmd {

	private String l;
	private CommandSender s;

	public TicketCommand(Module module) { super(module, "ticket"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		l = label.toLowerCase();
		s = sender;
		if (!MiscUtils.perm(sender, "carbonkit.ticket.create")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		if (args.length < 1) {
			showHelp();
		} else {
			if (MiscUtils.eq(args[0], "new", "create", "n", "c")) {
				if (args.length < 5) {
					sender.sendMessage(Clr.RED + "Your message is too short or missing! /" + l + " " + args[0].toLowerCase() + " [msg]");
					return;
				}
				int tid = mod.getData().getInt("ticket-data.next-ticket-id", 0);
				boolean idExists = mod.getData().contains("ticket-data.tickets." + tid);
				while (idExists) { idExists = mod.getData().contains("ticket-data.tickets." + (++tid)); }
				String path = "ticket-data.tickets." + tid + ".";
				String msg = MiscUtils.stringFromArray(" ", 1, args);
				String author = (sender instanceof Player) ? ((Player) sender).getUniqueId().toString() : "CONSOLE";
				mod.getData().set(path + "author", author);
				mod.getData().set(path + "message", msg);
				mod.getData().set(path + "time", System.currentTimeMillis());
				mod.getData().set(path + "witnesses", MiscUtils.stringFromArray(", ", Bukkit.getOnlinePlayers().toArray()));
				CarbonKit.saveConfig(ConfType.DATA);
			} else if (MiscUtils.eq(args[0], "append", "a")) {

			} else if (MiscUtils.eq(args[0], "delete", "remove", "del", "rem", "d", "r")) {

			} else if (MiscUtils.eq(args[0], "list", "view", "l", "v")) {

			} else if (MiscUtils.eq(args[0], "listall", "viewall", "la", "va")) {

			} else if (MiscUtils.eq(args[0], "info", "i")) {

			} else {
				showHelp();
			}
		}
		sender.sendMessage("Not yet implemented");
	}

	/* short-hand to send the block of help text */
	private void showHelp() {
		if (s == null) return;
		String p = "carbonkit.ticket.";
		if (MiscUtils.perm(s, p + "create", p + "append.others", p + "append.self", p + "delete.others",
				p + "delete.self", p + "info.others", p + "info.self", p + "view.all", p + "view")) {
			CustomMessage.printHeader(s, "Ticket Help");
			s.sendMessage(Clr.NOTE + "Tickets can be used to report infractions, suggest new ideas, etc. If" +
					"reporting rule breakers, try to include a time of the incident.");
			CustomMessage.printDivider(s);
			h("new [msg]", "Creates a new ticket", p + "create");
			h("append [ID] [msg]", "Adds text to existing ticket", p + "append.others", p + "append.self");
			h("delete [ID]", "Deletes a ticket", p + "delete.others", p + "delete.self");
			h("info [ID]", "View info about a ticket", p + "info.others", p + "info.self");
			h("listall", "Lists all tickets", p + "view.all");
			h("list", "Lists your tickets", p + "view");
			CustomMessage.printFooter(s);
		} else {
			s.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
		}
	}
	/* short-hand to perm-check and print a command help line */
	private void h(String args, String desc, String... perms) {
		if (perms.length == 0 || MiscUtils.perm(s, perms)) {
			if (l == null || l.isEmpty()) l = "ticket";
			String cp = Clr.AQUA + "/%s %s " + Clr.GRAY + "- %s";
			s.sendMessage(String.format(Locale.ENGLISH, cp, l, args, desc));
		}
	}
}
