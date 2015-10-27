package net.teamcarbon.carbonkit.commands.CarbonTrivia;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonkit.utils.CarbonTrivia.TriviaRound;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
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
				if (!MiscUtils.perm(sender, "carbonkit.carbontrivia.start")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				if (TriviaRound.getActiveRound() != null) {
					sender.sendMessage(CustomMessage.CT_ALREADY_RUNNING.pre());
					return;
				}
				if (args.length > 1) {
					String startedBy = args[1];
					for (int i = 2; i < args.length; i++) { startedBy += " " + args[i]; }
					TriviaRound.newTriviaRound(startedBy);
				} else { TriviaRound.newTriviaRound(sender); }
				return;
			} else if (MiscUtils.eq(args[0], "end")) {
				if (!MiscUtils.perm(sender, "carbonkit.carbontrivia.end")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				if (TriviaRound.getActiveRound() != null) {
					TriviaRound.getActiveRound().stopTrivia();
				} else {
					sender.sendMessage(CustomMessage.CT_NO_TRIVIA.noPre());
				}
				return;
			} else if (MiscUtils.eq(args[0], "cancel")) {
				if (!MiscUtils.perm(sender, "carbonkit.carbontrivia.cancel")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				if (TriviaRound.getActiveRound() != null) {
					TriviaRound.getActiveRound().cancelTrivia();
				} else {
					sender.sendMessage(CustomMessage.CT_NO_TRIVIA.noPre());
				}
				return;
			}
		}
		help(sender);
	}

	private void help(CommandSender sender) {
		if (MiscUtils.perm(sender, "carbonkit.carbontrivia.help")) {
			CustomMessage.printHeader(sender, "CarbonTrivia");
			if (MiscUtils.perm(sender, "carbonkit.carbontrivia.start"))
				sender.sendMessage(Clr.LIME + "/ctr start" + Clr.DARKAQUA + " - Start a trivia round");
			if (MiscUtils.perm(sender, "carbonkit.carbontrivia.end"))
				sender.sendMessage(Clr.LIME + "/ctr end" + Clr.DARKAQUA + " - End an ongoing trivia round");
			if (MiscUtils.perm(sender, "carbonkit.carbontrivia.cancel"))
				sender.sendMessage(Clr.LIME + "/ctr cancel" + Clr.DARKAQUA + " - Cancel an ongoing trivia round (no rewards)");
		} else { sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre()); }
	}

}
