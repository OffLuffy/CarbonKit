package net.teamcarbon.carbonkit.utils.votetypes;

import net.teamcarbon.carbonkit.modules.CarbonVoteModule;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.TriviaRound;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings("UnusedDeclaration")
public class TriviaVote extends Vote {
	public TriviaVote(OfflinePlayer player) { super(player, VoteType.TRIVIA); }
	@Override
	protected void votePass() {
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, CustomMessage.CV_TRIVIA_VOTE_PASSED.pre());
		CommandSender sender = null;
		if (getVoteStarter() instanceof CommandSender) sender = ((CommandSender)getVoteStarter());
		TriviaRound.newTriviaRound(sender);
	}
	@Override
	protected void voteFail() {
		HashMap<String, String> rep = new HashMap<String, String>();
		rep.put("{YESPERCENT}", String.format(Locale.ENGLISH, "%.2f", getAgreePercentage(true)));
		rep.put("{NOPERCENT}", String.format(Locale.ENGLISH, "%.2f", (100-getAgreePercentage(true))));
		rep.put("{VOTETYPE}", "Trivia");
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, MiscUtils.massReplace(CustomMessage.CV_VOTE_FAILED.pre(),rep));
	}
	@Override
	protected void broadcastStart() {
		HashMap<String, String> rep = new HashMap<String, String>();
		rep.put("{VOTETYPE}", "trivia");
		rep.put("{VOTEREASON}", "to start trivia");
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, MiscUtils.massReplace(CustomMessage.CV_VOTE_STARTED.pre(), rep));
	}
}
