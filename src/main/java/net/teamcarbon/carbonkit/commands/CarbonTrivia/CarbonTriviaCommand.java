package net.teamcarbon.carbonkit.commands.CarbonTrivia;

import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonkit.utils.CarbonTrivia.TriviaRound;
import net.teamcarbon.carbonkit.utils.Messages.Clr;
import net.teamcarbon.carbonkit.utils.MiscUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.utils.Module;

@SuppressWarnings("UnusedDeclaration")
public class CarbonTriviaCommand extends ModuleCmd {
	public CarbonTriviaCommand(Module module) { super(module, "carbontrivia"); }
	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length > 0) {
			if (MiscUtils.eq(args[0], "start")) {
				if (!mod.perm(sender, "start")) {
					sender.sendMessage(mod.getCoreMsg("no-perm", false));
					return;
				}
				if (TriviaRound.getActiveRound() != null) {
					sender.sendMessage(mod.getMsg("already-running", true));
					return;
				}
				if (args.length > 1) {
					StringBuilder sb = new StringBuilder(args[1]);
					for (int i = 2; i < args.length; i++) {
						sb.append(" ");
						sb.append(args[i]);
					}
					String startedBy = sb.toString();
					TriviaRound.newTriviaRound(startedBy);
				} else { TriviaRound.newTriviaRound(sender); }
				return;
			} else if (MiscUtils.eq(args[0], "end")) {
				if (!mod.perm(sender, "end")) {
					sender.sendMessage(mod.getCoreMsg("no-perm", false));
					return;
				}
				if (TriviaRound.getActiveRound() != null) {
					TriviaRound.getActiveRound().stopTrivia();
				} else {
					sender.sendMessage(mod.getMsg("no-trivia", false));
				}
				return;
			} else if (MiscUtils.eq(args[0], "cancel")) {
				if (!mod.perm(sender, "cancel")) {
					sender.sendMessage(mod.getCoreMsg("no-perm", false));
					return;
				}
				if (TriviaRound.getActiveRound() != null) {
					TriviaRound.getActiveRound().cancelTrivia();
				} else {
					sender.sendMessage(mod.getMsg("no-trivia", false));
				}
				return;
			}
		}
		help(sender);
	}

	private void help(CommandSender sender) {
		if (mod.perm(sender, "help")) {
			MiscUtils.printHeader(sender, "CarbonTrivia");
			if (mod.perm(sender, "start"))
				sender.sendMessage(Clr.LIME + "/ctr start" + Clr.DARKAQUA + " - Start a trivia round");
			if (mod.perm(sender, "end"))
				sender.sendMessage(Clr.LIME + "/ctr end" + Clr.DARKAQUA + " - End an ongoing trivia round");
			if (mod.perm(sender, "cancel"))
				sender.sendMessage(Clr.LIME + "/ctr cancel" + Clr.DARKAQUA + " - Cancel an ongoing trivia round (no rewards)");
		} else { sender.sendMessage(mod.getCoreMsg("no-perm", false)); }
	}

}
