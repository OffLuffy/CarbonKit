package net.teamcarbon.carbonkit.commands.CarbonVote;

import net.milkbowl.vault.economy.EconomyResponse;
import net.teamcarbon.carbonkit.modules.CarbonVoteModule;
import net.teamcarbon.carbonkit.utils.CarbonVote.*;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonlib.Misc.TypeUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.events.voteEvents.VoteCastEvent;
import net.teamcarbon.carbonkit.events.voteEvents.VoteStartEvent;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonkit.utils.CarbonVote.TargetedVote.TargetedVoteType;
import net.teamcarbon.carbonkit.utils.CarbonVote.Vote.VoteType;
import net.teamcarbon.carbonkit.utils.CarbonVote.WeatherVote.WeatherType;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;

import java.util.HashMap;
import java.util.Locale;

@SuppressWarnings({"UnusedDeclaration"})
public class CarbonVoteCommand extends ModuleCmd {
	private CarbonVoteModule modInst = CarbonVoteModule.inst;
	public CarbonVoteCommand(Module module) { super(module, "carbonvote"); }
	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		if (args.length < 1 || MiscUtils.eq(args[0], "help")) {
			if (!mod.perm(sender, "help")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			} else if(!mod.perm(sender, "startvote.weather", "startvote.time", "startvote.ban",
					"startvote.kick", "startvote.mute", "startvote.trivia")) {
				if (CarbonVoteModule.isVoteOngoing()) {
					if (!mod.perm(sender, "vote." + CarbonVoteModule.getActiveVote().getType())) {
						sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					}
				} else {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				}
			} else {

				double weatherPrice = getMod().getConfig().getDouble("vote-prices.weather", 0.0),
						timePrice = getMod().getConfig().getDouble("vote-prices.time", 0.0),
						triviaPrice = getMod().getConfig().getDouble("vote-prices.trivia", 0.0),
						banPrice = getMod().getConfig().getDouble("vote-prices.ban", 0.0),
						kickPrice = getMod().getConfig().getDouble("vote-prices.kick", 0.0),
						mutePrice = getMod().getConfig().getDouble("vote-prices.mute", 0.0);

				boolean bypassWeather = mod.perm(sender, "bypassprice.weather"),
						bypassTime = mod.perm(sender, "bypassprice.time"),
						bypassTrivia = mod.perm(sender, "bypassprice.trivia"),
						bypassBan = mod.perm(sender, "bypassprice.ban"),
						bypassKick = mod.perm(sender, "bypassprice.kick"),
						bypassMute = mod.perm(sender, "bypassprice.mute");

				CustomMessage.printHeader(sender, "Vote Help");
				if (CarbonVoteModule.isVoteTypeEnabled(VoteType.WEATHER) && mod.perm(sender, "startvote.weather")) {
					sender.sendMessage(Clr.AQUA + "/cv w [clear|storm|rain]" + Clr.DARKAQUA + " - Vote to change weather"
							+ ((!bypassWeather && weatherPrice > 0) ? Clr.NOTE + " (" + weatherPrice + ")" : ""));
				}
				if (CarbonVoteModule.isVoteTypeEnabled(VoteType.TIME) && mod.perm(sender, "startvote.time")) {
					sender.sendMessage(Clr.AQUA + "/cv t [term, 24/12hr, or ticks]" + Clr.DARKAQUA + " - Vote to set time"
							+ ((!bypassTime && timePrice > 0) ? Clr.NOTE + " (" + timePrice + ")" : ""));
				}
				if (CarbonVoteModule.isVoteTypeEnabled(TargetedVoteType.BAN) && mod.perm(sender, "startvote.ban" )) {
					sender.sendMessage(Clr.AQUA + "/cv b [player]" + Clr.DARKAQUA + " - Vote to ban player"
							+ ((!bypassBan && banPrice > 0) ? Clr.NOTE + " (" + banPrice + ")" : ""));
				}
				if (CarbonVoteModule.isVoteTypeEnabled(TargetedVoteType.KICK) && mod.perm(sender, "startvote.kick" )) {
					sender.sendMessage(Clr.AQUA + "/cv k [player]" + Clr.DARKAQUA + " - Vote to kick player"
							+ ((!bypassKick && kickPrice > 0) ? Clr.NOTE + " (" + kickPrice + ")" : ""));
				}
				if (MiscUtils.checkPlugin("Essentials", true)) {
					if (CarbonVoteModule.isVoteTypeEnabled(TargetedVoteType.MUTE) && mod.perm(sender, "startvote.mute")) {
						sender.sendMessage(Clr.AQUA + "/cv m [player]" + Clr.DARKAQUA + " - Vote to mute player"
								+ ((!bypassMute && mutePrice > 0) ? Clr.NOTE + " (" + mutePrice + ")" : ""));
					}
				}
				if (CarbonVoteModule.isVoteTypeEnabled(VoteType.TRIVIA) && mod.perm(sender, "startvote.trivia" ) && getMod().isEnabled())
					sender.sendMessage(Clr.AQUA + "/cv tr" + Clr.DARKAQUA + " - Vote to start a round of trivia");
				if (CarbonVoteModule.isVoteOngoing()) {
					Vote v = CarbonVoteModule.getActiveVote();
					if (v instanceof TargetedVote &&((TargetedVote)v).getTarget().equals(sender)) // TODO <--- Make sure this is viable - CommandSender.equals(OfflinePlayer)
						return;
					HashMap<String, String> rep = new HashMap<>();
					String vt = v.getTypeName();
					rep.put("{VOTETYPE}", vt);
					if (sender.equals(v.getVoteStarter())) { // TODO <--- Make sure this is viable - CommandSender.equals(OfflinePlayer)
						rep.put("{YESPERCENT}", String.format(Locale.ENGLISH, "%.2f", v.getAgreePercentage(true)));
						rep.put("{NOPERCENT}", String.format(Locale.ENGLISH, "%.2f", 100f-v.getAgreePercentage(true)));
						sender.sendMessage(CustomMessage.CV_OWNER_HELP.pre(rep));
						return;
					}
					if (mod.perm(sender, "vote." + vt)) {
						sender.sendMessage(CustomMessage.CV_VOTER_HELP.pre(rep));
					}
				}
			}
		} else if (args.length > 0 && MiscUtils.eq(args[0], "test")) {
			// Calculates vote percentages to test the formula give various values
			// y = yes votes, n = no votes, x = non-voters, xw = non-vote weight
			// non-vote weight is negative for no vote, positive for yes. 0 for no effect
			float y=0, n=0, x=0, xw = -30;
			if (args.length > 1 && TypeUtils.isFloat(args[1])) y = Float.parseFloat(args[1]);
			if (args.length > 2 && TypeUtils.isFloat(args[2])) n = Float.parseFloat(args[2]);
			if (args.length > 3 && TypeUtils.isFloat(args[3])) x = Float.parseFloat(args[3]);
			if (args.length > 4 && TypeUtils.isFloat(args[4])) xw = Float.parseFloat(args[4]);
			float p = Vote.calc(y,n,x,xw);
			sender.sendMessage("Y: " + String.format("%.1f", p) + "% - N: " + String.format("%.1f", (100f-p)) + "%");
		} else {
			// Check if they're voting yes or no to an active vote
			if (TypeUtils.isBoolean(args[0])) {
				if (CarbonVoteModule.getActiveVote() != null) {
					Vote v = CarbonVoteModule.getActiveVote();
					if (v instanceof TargetedVote) {
						if (((TargetedVote)v).getTarget().equals(sender)) {
							sender.sendMessage(CustomMessage.CV_BLOCKED.pre());
							return;
						}
					}
					if (!mod.perm(sender, "vote." + v.getType().lname())) {
						sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
						return;
					}
					Player pl = (Player)sender;
					if (v.getAgrees().contains(pl) || v.getDisagrees().contains(pl)) {
						sender.sendMessage(CustomMessage.CV_ALREADY_VOTED.pre());
						return;
					}
					String type = CarbonVoteModule.getActiveVote().getType().name();
					VoteCastEvent vce = new VoteCastEvent(pl, v, TypeUtils.toBoolean(args[0]));
					CarbonKit.pm.callEvent(vce);
					if (!vce.isCancelled()) {
						CarbonVoteModule.getActiveVote().addVoter(pl, TypeUtils.toBoolean(args[0]));
						sender.sendMessage(Clr.LIME + MiscUtils.capFirst(type, true) + " vote cast");
						if (getMod().getConfig().getBoolean("broadcast-votes", true)) {
							HashMap<String, String> rep = new HashMap<>();
							rep.put("{VOTER}", sender.getName());
							rep.put("{VOTED}", TypeUtils.toBoolean(args[0]) ? "in favor of" : "against");
							rep.put("{VOTETYPE}", v.getTypeName());
							MiscUtils.permBroadcast(CarbonVoteModule.VMSG_PERM, MiscUtils.quickList(pl),
									CustomMessage.CV_VOTE_CAST_BROADCAST.pre(rep));
						}
					}
					return;
				} else {
					sender.sendMessage(CustomMessage.CV_NO_VOTE.pre());
					return;
				}
			}
			// Ready the replacement HashMap
			HashMap<String, String> rep = new HashMap<>();
			// Check to see if a vote is already active
			if (CarbonVoteModule.isVoteOngoing()) {
				rep.put("{VOTETYPE}", CarbonVoteModule.getActiveVote().getTypeName());
				sender.sendMessage(CustomMessage.CV_VOTE_EXISTS.pre(rep));
				return;
			}
			// Check if vote-type specified is valid
			if (CarbonVoteModule.getVoteType(args[0]) == null) {
				sender.sendMessage(CustomMessage.CV_INVALID_VOTETYPE.pre());
				return;
			}
			VoteType vt = CarbonVoteModule.getVoteType(args[0]);
			if (vt == VoteType.TARGETED) {
				TargetedVoteType tvt = CarbonVoteModule.getTargetedVoteType(args[0]);
				if (tvt != null)
					if (!CarbonVoteModule.isVoteTypeEnabled(tvt)) {
						sender.sendMessage(CustomMessage.CV_INVALID_VOTETYPE.pre());
						return;
					}
			} else if (!CarbonVoteModule.isVoteTypeEnabled(vt)) {
				sender.sendMessage(CustomMessage.CV_INVALID_VOTETYPE.pre());
				return;
			}
			if (vt == VoteType.TARGETED) {
				TargetedVoteType tvt = CarbonVoteModule.getTargetedVoteType(args[0]);
				if (tvt != null)
					rep.put("{VOTETYPE}", tvt.lname());
			} else {
				rep.put("{VOTETYPE}", vt.lname());
			}
			// Check if sender has perm to start this vote-type
			if (!mod.perm(sender, "startvote." + rep.get("{VOTETYPE}"))) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			// Check to see if this vote-type has been used too recently
			if (!mod.perm(sender, "bypasscooldown." + rep.get("{VOTETYPE}"))) {
				long since = (System.currentTimeMillis() / 1000L) - getMod().getData().getLong("last-" + rep.get("{VOTETYPE}") + "-vote", 0L);
				long intvl = getMod().getConfig().getInt("vote-interval-seconds." + rep.get("{VOTETYPE}"), 60);
				if (since < intvl) {
					long rem = intvl - since;
					if (rem >= 3600) { // Hour(s) remain
						long h = rem / 3600, m = rem % 3600, s = rem % 60;
						rep.put("{REMAININGTIME}", h + "hour" + (h != 1 ? "s" : "") + (m > 0 ? ", " + m + " minute" + (m != 1 ? "s" : "") : "") + (s > 0 ? ", " + s + " second" + (s != 1 ? "s" : "") : ""));
					}
					if (rem >= 60) { // Minute(s) remain
						long m = rem / 60, s = rem % 60;
						rep.put("{REMAININGTIME}", m + " minute" + (m != 1 ? "s" : "") + (s > 0 ? ", " + s + " second" + (s != 1 ? "s" : "") : ""));
					} else { // Second(s) remain
						rep.put("{REMAININGTIME}", rem + " seconds");
					}
					sender.sendMessage(CustomMessage.CV_MUST_WAIT.pre(rep));
					return;
				}
			}
			// Check if player has enough money to start the vote (don't withdraw yet)
			double votePrice = getMod().getConfig().getDouble("vote-prices." + rep.get("{VOTETYPE}"), 0.0);
			if (votePrice > 0.0 && !mod.perm(sender, "bypassprice." + rep.get("{VOTETYPE}"))) {
				if (!CarbonKit.econ.has((OfflinePlayer)sender, votePrice)) {
					rep.put("{VOTECOST}", CarbonKit.econ.format(votePrice));
					sender.sendMessage(CustomMessage.CV_NOT_ENOUGH_MONEY.pre(rep));
				}
			}
			// Check to see if there's enough players to start this vote-type
			if (vt == VoteType.TARGETED) {
				TargetedVoteType tvt = CarbonVoteModule.getTargetedVoteType(args[0]);
				if (!CarbonVoteModule.hasEnoughPlayers(vt)) {
					rep.put("{MOREPLAYERS}", CarbonVoteModule.additionalNeeded(tvt) + "");
					sender.sendMessage(CustomMessage.CV_NOT_ENOUGH_PLAYERS.pre(rep));
					return;
				}
			} else {
				if (!CarbonVoteModule.hasEnoughPlayers(vt)) {
					rep.put("{MOREPLAYERS}", CarbonVoteModule.additionalNeeded(vt) + "");
					sender.sendMessage(CustomMessage.CV_NOT_ENOUGH_PLAYERS.pre(rep));
					return;
				}
			}
			// Now handle vote-type specifics (help & starting the votes)
			if (MiscUtils.eq(args[0], "weather", "w")) {
				if (args.length > 1) {
					boolean start = true;
					if (votePrice > 0.0 && !mod.perm(sender, "bypassprice." + rep.get("{VOTETYPE}"))) {
						EconomyResponse er = CarbonKit.econ.withdrawPlayer((OfflinePlayer)sender, votePrice);
						start = er.transactionSuccess();
						rep.put("{VOTEPRICE}", votePrice+"");
						if (start) { sender.sendMessage(CustomMessage.CT_CHARGED.pre(rep)); }
					}
					if (start) {
						WeatherType wt = WeatherVote.getWeatherType(args[1]);
						WeatherVote wv = new WeatherVote((OfflinePlayer) sender, wt, ((Player) sender).getWorld());
						VoteStartEvent vse = new VoteStartEvent((Player) sender, wv);
						CarbonKit.pm.callEvent(vse);
						if (!vse.isCancelled()) CarbonVoteModule.startVote(wv);
					} else {
						sender.sendMessage(CustomMessage.CV_ECON_ERROR.pre());
					}
				} else {
					sender.sendMessage(Clr.RED + "Usage: /cv w [weathertype]");
					sender.sendMessage(Clr.DARKRED + "Valid types: clear, rain, storm");
				}
			} else if (MiscUtils.eq(args[0], "time", "t")) {
				if (args.length > 1) {
					boolean start = true;
					if (votePrice > 0.0 && !mod.perm(sender, "bypassprice." + rep.get("{VOTETYPE}"))) {
						EconomyResponse er = CarbonKit.econ.withdrawPlayer((OfflinePlayer)sender, votePrice);
						start = er.transactionSuccess();
						rep.put("{VOTEPRICE}", votePrice+"");
						if (start) { sender.sendMessage(CustomMessage.CT_CHARGED.pre(rep)); }
					}
					if (start) {
						long time = CarbonVoteModule.parseTime(args[1]);
						TimeVote tv = new TimeVote((OfflinePlayer)sender, time, ((Player)sender).getWorld());
						VoteStartEvent vse = new VoteStartEvent((Player)sender, tv);
						CarbonKit.pm.callEvent(vse);
						if (!vse.isCancelled()) CarbonVoteModule.startVote(tv);
					} else {
						sender.sendMessage(CustomMessage.CV_ECON_ERROR.pre());
					}
				} else {
					sender.sendMessage(Clr.RED + "Usage: /cv t [time]");
					sender.sendMessage(Clr.DARKRED + "Time can be ticks, 12/24 hour format time (6pm, 4.30am, etc),");
					sender.sendMessage(Clr.DARKRED + "or a term (dawn, day, noon, evening, dusk, night, midnight)");
				}
			} else if (MiscUtils.eq(args[0], "trivia", "tr")) {
				if (getMod().isEnabled()) {
					TriviaVote trv = new TriviaVote((OfflinePlayer) sender);
					VoteStartEvent vse = new VoteStartEvent((Player) sender, trv);
					if (!vse.isCancelled()) CarbonVoteModule.startVote(trv);
				} else { sender.sendMessage(CustomMessage.CV_TRIVIA_DISABLED.pre()); }
			} else if (MiscUtils.eq(args[0], "ban", "b")) {
				if (args.length > 1) {
					if (Bukkit.getPlayer(args[0]) != null) {
						if (modInst.perm(Bukkit.getPlayer(args[0]), "exempt.ban-vote")) {
							rep.put("{ACTTION}", "banned");
							sender.sendMessage(CustomMessage.CV_EXEMPT.pre(rep));
						}
						boolean start = true;
						if (votePrice > 0.0 && !mod.perm(sender, "bypassprice." + rep.get("{VOTETYPE}"))) {
							EconomyResponse er = CarbonKit.econ.withdrawPlayer((OfflinePlayer)sender, votePrice);
							start = er.transactionSuccess();
							rep.put("{VOTEPRICE}", votePrice+"");
							if (start) { sender.sendMessage(CustomMessage.CT_CHARGED.pre(rep)); }
						}
						if (start) {
							BanVote bv = new BanVote((OfflinePlayer) sender, Bukkit.getPlayer(args[0]));
							VoteStartEvent vse = new VoteStartEvent((Player) sender, bv);
							if (!vse.isCancelled()) CarbonVoteModule.startVote(bv);
						} else {
							sender.sendMessage(CustomMessage.CV_ECON_ERROR.pre());
						}
					} else {
						sender.sendMessage(CustomMessage.GEN_PLAYER_NOT_FOUND.pre());
					}
				} else {
					sender.sendMessage(Clr.RED + "Usage: /cv b [player]");
				}
			} else if (MiscUtils.eq(args[0], "kick", "k") && MiscUtils.checkPlugin("Essentials", true)) {
				if (args.length > 1) {
					if (Bukkit.getPlayer(args[0]) != null) {
						if (modInst.perm(Bukkit.getPlayer(args[0]), "exempt.kick-vote")) {
							rep.put("{ACTTION}", "kicked");
							sender.sendMessage(CustomMessage.CV_EXEMPT.pre(rep));
						}
						boolean start = true;
						if (votePrice > 0.0 && !mod.perm(sender, "bypassprice." + rep.get("{VOTETYPE}"))) {
							EconomyResponse er = CarbonKit.econ.withdrawPlayer((OfflinePlayer)sender, votePrice);
							start = er.transactionSuccess();
							rep.put("{VOTEPRICE}", votePrice+"");
							if (start) { sender.sendMessage(CustomMessage.CT_CHARGED.pre(rep)); }
						}
						if (start) {
							KickVote kv = new KickVote((OfflinePlayer) sender, Bukkit.getPlayer(args[0]));
							VoteStartEvent vse = new VoteStartEvent((Player) sender, kv);
							if (!vse.isCancelled()) CarbonVoteModule.startVote(kv);
						} else {
							sender.sendMessage(CustomMessage.CV_ECON_ERROR.pre());
						}
					} else {
						sender.sendMessage(CustomMessage.GEN_PLAYER_NOT_FOUND.pre());
					}
				} else {
					sender.sendMessage(Clr.RED + "Usage: /cv k [player]");
				}
			} else if (MiscUtils.eq(args[0], "mute", "m")) {
				if (args.length > 1) {
					if (Bukkit.getPlayer(args[0]) != null) {
						if (modInst.perm(Bukkit.getPlayer(args[0]), "exempt.mute-vote")) {
							rep.put("{ACTTION}", "muted");
							sender.sendMessage(CustomMessage.CV_EXEMPT.pre(rep));
						}
						boolean start = true;
						if (votePrice > 0.0 && !mod.perm(sender, "bypassprice." + rep.get("{VOTETYPE}"))) {
							EconomyResponse er = CarbonKit.econ.withdrawPlayer((OfflinePlayer)sender, votePrice);
							start = er.transactionSuccess();
							rep.put("{VOTEPRICE}", votePrice+"");
							if (start) { sender.sendMessage(CustomMessage.CT_CHARGED.pre(rep)); }
						}
						if (start) {
							MuteVote mv = new MuteVote((OfflinePlayer) sender, Bukkit.getPlayer(args[0]));
							VoteStartEvent vse = new VoteStartEvent((Player) sender, mv);
							if (!vse.isCancelled()) CarbonVoteModule.startVote(mv);
						} else {
							sender.sendMessage(CustomMessage.CV_ECON_ERROR.pre());
						}
					} else {
						sender.sendMessage(CustomMessage.GEN_PLAYER_NOT_FOUND.pre());
					}
				} else {
					sender.sendMessage(Clr.RED + "Usage: /cv m [player]");
				}
			}
		}
	}
}
