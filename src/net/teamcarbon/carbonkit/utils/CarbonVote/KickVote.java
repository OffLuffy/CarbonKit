package net.teamcarbon.carbonkit.utils.CarbonVote;

import net.teamcarbon.carbonkit.modules.CarbonVoteModule;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonlib.Misc.MiscUtils;

import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings("UnusedDeclaration")
public class KickVote extends TargetedVote {
	public KickVote(OfflinePlayer player, OfflinePlayer target) {
		super(player, target, TargetedVoteType.KICK);
	}
	protected void votePass() {
		HashMap<String, String> rep = new HashMap<>();
		rep.put("{TARGET}", target.getName());
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, MiscUtils.quickList((Player) target), CustomMessage.CV_KICK_VOTE_PASSED.pre(rep));
		if (target.isOnline()) ((Player)target).kickPlayer(CustomMessage.CV_KICK_MESSAGE.noPre(rep));
	}
	protected void voteFail() {
		HashMap<String, String> rep = new HashMap<>();
		rep.put("{YESPERCENT}", String.format(Locale.ENGLISH, "%.2f", getAgreePercentage(true)));
		rep.put("{NOPERCENT}", String.format(Locale.ENGLISH, "%.2f", (100 - getAgreePercentage(true))));
		rep.put("{VOTETYPE}", "Kick");
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, MiscUtils.quickList((Player)target), CustomMessage.CV_VOTE_FAILED.pre(rep));
	}
	protected void broadcastStart() {
		HashMap<String, String> rep = new HashMap<>();
		rep.put("{VOTETYPE}", getTargetedVoteType().lname());
		rep.put("{VOTEREASON}", "to " + getTargetedVoteType().lname() + " " + target.getName());
		MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, CustomMessage.CV_VOTE_STARTED.pre(rep));
	}
}
