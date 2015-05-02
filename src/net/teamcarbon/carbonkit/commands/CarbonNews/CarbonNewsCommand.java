package net.teamcarbon.carbonkit.commands.CarbonNews;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.modules.CarbonNewsModule;
import net.teamcarbon.carbonkit.tasks.BroadcastTask;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import org.bukkit.entity.Player;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class CarbonNewsCommand extends ModuleCmd {

	private String[] allPerms = {
			"set.enabled", "listsets", "listmessages", "set.delay", "set.requireperms",
			"set.prefix", "set.postfix", "set.random", "set.sendtoconsole", "set.colorconsole",
			"set.sendtoplayer"
	};
	private String[] allSetPerms = {
			"set.enabled", "set.delay", "set.requireperms", "set.prefix", "set.postfix", "set.random",
			"set.sendtoconsole", "set.colorconsole", "set.sendtoplayer"
	};

	public CarbonNewsCommand(Module module) { super(module, "carbonnews"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 0 || MiscUtils.eq(args[0], "help")) { // '/cn help' or '/cn'
			printHelp(sender);
		} else if (MiscUtils.eq(args[0], "toggle")) { // '/cn toggle <set> [on|off]'
			if (!perm(sender, "set.enabled")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			if (args.length > 1) {
				if (!CarbonNewsModule.isMessageSet(args[1])) {
					HashMap<String, String> rep = new HashMap<String, String>();
					rep.put("{SETNAME}", args[1]);
					sender.sendMessage(CustomMessage.CN_NOT_SET.pre(rep));
					if (perm(sender, "listsets"))
						listAvailbleGroups(sender);
					return;
				}
			} else {
				sender.sendMessage(CustomMessage.CN_NEEDS_SET.pre());
				if (perm(sender, "listsets"))
					listAvailbleGroups(sender);
				return;
			}
			BroadcastTask bt = CarbonNewsModule.getMessageSet(args[1]);
			if (bt.isEmpty()) {
				sender.sendMessage(Clr.RED + "This list is empty! You must add messages to it before enabling it!");
				return;
			}
			if (args.length > 2 && MiscUtils.isBoolean(args[2]))
				bt.setEnabled(MiscUtils.toBoolean(args[2]));
			else if (args.length > 1)
				bt.setEnabled(!bt.isEnabled());
			sender.sendMessage(Clr.AQUA + "Message set " + bt.getSetName() + (bt.isEnabled()?Clr.LIME + " enabled":Clr.RED + " disabled"));
		} else if (MiscUtils.eq(args[0], "listsets", "lists", "ls")) { // '/cn listsets'
			if (!perm(sender, "listsets")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			listAvailbleGroups(sender);
		} else if (MiscUtils.eq(args[0], "listmessages", "listm", "lm")) { // '/cn listmessages <set>'
			if (!perm(sender, "listmessages")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			if (args.length < 2){
				sender.sendMessage(CustomMessage.CN_NEEDS_SET.pre());
				if (perm(sender, "listsets"))
					listAvailbleGroups(sender);
				return;
			}
			if (!CarbonNewsModule.isMessageSet(args[1])){
				HashMap<String, String> rep = new HashMap<String, String>();
				rep.put("{SETNAME}", args[1]);
				sender.sendMessage(CustomMessage.CN_NOT_SET.pre(rep));
				if (perm(sender, "listsets"))
					listAvailbleGroups(sender);
				return;
			}
			BroadcastTask bt = CarbonNewsModule.getMessageSet(args[1]);
			//List<String> messages = bt.getMessages();
			final int LENGTH = 50;
			if (!bt.isEmpty()) {
				for (int i = 0; i <bt.size(); i++) {
					String msg = bt.getMessage(i);
					boolean tooLong = msg.length() > LENGTH;
					sender.sendMessage(Clr.AQUA + "" + i + ": " + Clr.GRAY + (tooLong?msg.substring(0,LENGTH):msg) + (tooLong?"...":""));
				}
			} else {
				sender.sendMessage(Clr.GRAY + "This set has no messages! Add some with /cn addm " + bt.getSetName() + " <msg>");
			}
		} else if (MiscUtils.eq(args[0], "setinfo", "sinfo", "si")) { // '/cn setinfo <set>'
			if (!perm(sender, "setinfo")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			if (args.length <2){
				sender.sendMessage(CustomMessage.CN_NEEDS_SET.pre());
				if (perm(sender, "listsets"))
					listAvailbleGroups(sender);
				return;
			}
			if (!CarbonNewsModule.isMessageSet(args[1])){
				HashMap<String, String> rep = new HashMap<String, String>();
				rep.put("{SETNAME}", args[1]);
				sender.sendMessage(CustomMessage.CN_NOT_SET.pre(rep));
				if (perm(sender, "listsets"))
					listAvailbleGroups(sender);
				return;
			}
			BroadcastTask bt = CarbonNewsModule.getMessageSet(args[1]);
			sender.sendMessage(Clr.TITLE + "===[ Set Info for " + bt.getSetName() + " ]===");
			sender.sendMessage(Clr.AQUA + "Broadcasting: " + Clr.GRAY + (bt.isEnabled()?Clr.LIME + "Enabled":Clr.RED + "Disabled"));
			sender.sendMessage(Clr.AQUA + "Message Count: " + Clr.GRAY + bt.size());
			sender.sendMessage(Clr.AQUA + "Interval Delay (seconds) : " + Clr.GRAY + CarbonKit.getConfig(ConfType.NEWS).getLong("MessageSets." + bt.getSetName() + ".delaySeconds", 60));
			sender.sendMessage(Clr.AQUA + "Random: " + Clr.GRAY + bt.isRandom());
			sender.sendMessage(Clr.AQUA + "Requires Permission: " + Clr.GRAY + bt.requirePerms() + Clr.NOTE + " (carbonkit.news.receive." + bt.getSetName() + ")");
			sender.sendMessage(Clr.AQUA + "Sending to Console: " + Clr.GRAY + bt.isSentToConole());
			sender.sendMessage(Clr.AQUA + "Sending to Players: " + Clr.GRAY + bt.isSentToPlayers());
			sender.sendMessage(Clr.AQUA + "Color Console: " + Clr.GRAY + bt.isConsoleColorized());
			sender.sendMessage(Clr.AQUA + "Prefix: " + Clr.GRAY + bt.getPrefix());
			sender.sendMessage(Clr.AQUA + "Postfix: " + Clr.GRAY + bt.getPostfix());
		}/* else if (MiscUtils.eq(args[0], "reload", "rl", "r")) { // '/cn reload'
			if (!perm(sender, "reload")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			CarbonNewsModule.reloadPlugin();
			sender.sendMessage(Clr.GRAY + "CarbonNewsModule has finished reloading");
		} */else if (MiscUtils.eq(args[0], "set")) { // '/cn set <set> <setting> <value>'
			if (!perm(sender, allSetPerms)) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			if (args.length > 1) {
				if (CarbonNewsModule.isMessageSet(args[1])) {
					BroadcastTask bt = CarbonNewsModule.getMessageSet(args[1]);
					if (args.length > 2) {
						String opt = args[2];
						if (MiscUtils.eq(opt, "setenabled", "enabed")) {
							if (!perm(sender, "set.enabled")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							if (args.length > 3) {
								if (MiscUtils.isBoolean(args[3])) {
									if (MiscUtils.toBoolean(args[3]) && bt.isEmpty()) {
										sender.sendMessage(Clr.RED + "This list is empty! You must add messages to it before enabling it!");
										return;
									}
									bt.setEnabled(MiscUtils.toBoolean(args[3]));
								} else {
									sender.sendMessage(Clr.RED + "'" + opt + "' must be set to true, false, or nothing to toggle current value");
									return;
								}
							} else {
								if (!bt.isEnabled() && bt.isEmpty()) {
									sender.sendMessage(Clr.RED + "This list is empty! You must add messages to it before enabling it!");
									return;
								}
								bt.setEnabled(!bt.isEnabled());
							}
							sender.sendMessage(Clr.AQUA + "Message set " + bt.getSetName() + (bt.isEnabled()?Clr.LIME + " enabled":Clr.RED+ " disabled"));
						} else if (MiscUtils.eq(opt, "delayseconds", "delay", "time")) {
							if (!perm(sender, "set.delay")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							if (args.length > 3) {
								if (MiscUtils.isLong(args[3]) && Long.parseLong(args[3]) > 0) {
									bt.setDelay(Long.parseLong(args[3]));
									sender.sendMessage(Clr.AQUA + "Message set " + bt.getSetName() + "'s delay set to " + Long.parseLong(args[3]) + " seconds");
								} else {
									sender.sendMessage(Clr.RED + "'" + opt + "' must be set to a whole number above 0");
								}
							} else {
								sender.sendMessage(Clr.RED + "'" + opt + "' must be set to a whole number above 0");
							}
						} else if (MiscUtils.eq(opt, "requirepermission", "requireperm")) {
							if (!perm(sender, "set.requireperms")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							if (args.length > 3) {
								if (MiscUtils.isBoolean(args[3])) {
									bt.setRequirePerms(MiscUtils.toBoolean(args[3]));
								} else {
									sender.sendMessage(Clr.RED + "'" + opt + "' must be set to true, false, or nothing to toggle current value");
									return;
								}
							} else {
								bt.setRequirePerms(!bt.requirePerms());
							}
							sender.sendMessage(Clr.AQUA + "Message set " + bt.getSetName() + " will " + (bt.requirePerms()?"now":"no longer") + " require perms to listen.");
						} else if (MiscUtils.eq(opt, "prefix", "pre")) {
							if (!perm(sender, "set.prefix")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							if (args.length > 3) {
								String pf = args[3];
								if (args.length > 4)
									for (int i = 4; i < args.length; i++)
										pf += " " + args[i];
								bt.setPrefix(pf);
							} else {
								bt.setPrefix("");
							}
							sender.sendMessage(Clr.AQUA + "Message set " + bt.getSetName() + "'s postfix " + (bt.getPostfix().equals("")?"removed":"set"));
						} else if (MiscUtils.eq(opt, "postfix", "post")) {
							if (!perm(sender, "set.postfix")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							if (args.length > 3) {
								String pf = args[3];
								if (args.length > 4)
									for (int i = 4; i < args.length; i++)
										pf += " " + args[i];
								bt.setPostfix(pf);
							} else {
								bt.setPostfix("");
							}
							sender.sendMessage(Clr.AQUA + "Message set " + bt.getSetName() + "'s postfix " + (bt.getPostfix().equals("")?"removed":"set"));
						} else if (MiscUtils.eq(opt, "randomorder", "random")) {
							if (!perm(sender, "set.random")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							if (args.length > 3) {
								if (MiscUtils.isBoolean(args[3])) {
									bt.setRandom(MiscUtils.toBoolean(args[3]));
								} else {
									sender.sendMessage(Clr.RED + "'" + opt + "' must be set to true, false, or nothing to toggle current value");
									return;
								}
							} else {
								bt.setRandom(!bt.isRandom());
							}
							sender.sendMessage(Clr.AQUA + "Message set " + bt.getSetName() + " will " + (bt.isRandom()?"now":"no longer") + " be randomized.");
						} else if (MiscUtils.eq(opt, "sendtoconsole", "sendconsole")) {
							if (!perm(sender, "set.sendtoconsole")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							if (args.length > 3) {
								if (MiscUtils.isBoolean(args[3])) {
									bt.setSendToConsole(MiscUtils.toBoolean(args[3]));
								} else {
									sender.sendMessage(Clr.RED + "'" + opt + "' must be set to true, false, or nothing to toggle current value");
									return;
								}
							} else {
								bt.setSendToConsole(!bt.isSentToConole());
							}
							sender.sendMessage(Clr.AQUA + "Message set " + bt.getSetName() + " will " + (bt.isSentToConole()?"now":"no longer") + " send to the console.");
						} else if (MiscUtils.eq(opt, "colorconsolemessages", "colorconsole", "consolecolor", "colorconsole")) {
							if (!perm(sender, "set.colorconsole")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							if (args.length > 3) {
								if (MiscUtils.isBoolean(args[3])) {
									bt.setColorConsole(MiscUtils.toBoolean(args[3]));
								} else {
									sender.sendMessage(Clr.RED + "'" + opt + "' must be set to true, false, or nothing to toggle current value");
									return;
								}
							} else {
								bt.setColorConsole(!bt.isEnabled());
							}
							sender.sendMessage(Clr.AQUA + "Message set " + bt.getSetName() + " will " + (bt.isConsoleColorized()?"now":"no longer") + " color console messages.");
						} else if (MiscUtils.eq(opt, "sendtoplayers", "sendplayers")) {
							if (!perm(sender, "set.sendtoplayers")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							if (args.length > 3) {
								if (MiscUtils.isBoolean(args[3])) {
									bt.setSendToPlayer(MiscUtils.toBoolean(args[3]));
								} else {
									sender.sendMessage(Clr.RED + "'" + opt + "' must be set to true, false, or nothing to toggle current value");
									return;
								}
							} else {
								bt.setSendToPlayer(!bt.isEnabled());
							}
							sender.sendMessage(Clr.AQUA + "Message set " + bt.getSetName() + " will " + (bt.isSentToPlayers()?"now":"no longer") + " send to players.");
						} else {
							if (perm(sender, allSetPerms)) {
								String opts = "";
								if (perm(sender, "set.enabled"))
									opts += (opts.equals("")?"enabled":", enabled");
								if (perm(sender, "set.delay"))
									opts += (opts.equals("")?"delay":", delay");
								if (perm(sender, "set.requireperms"))
									opts += (opts.equals("")?"requireperms":", requireperms");
								if (perm(sender, "set.prefix"))
									opts += (opts.equals("")?"prefix":", prefix");
								if (perm(sender, "set.postfix"))
									opts += (opts.equals("")?"postfix":", postfix");
								if (perm(sender, "set.random"))
									opts += (opts.equals("")?"random":", random");
								if (perm(sender, "set.sendtoconsole"))
									opts += (opts.equals("")?"sendtoconsole":", sendtoconsole");
								if (perm(sender, "set.colorconsole"))
									opts += (opts.equals("")?"colorconsole":", colorconsole");
								if (perm(sender, "set.sendtoplayer"))
									opts += (opts.equals("")?"sendtoplayer":", sendtoplayer");
								sender.sendMessage(Clr.AQUA + "Available set options: " + opts);
							}
						}
					} else {
						if (perm(sender, allSetPerms)) {
							String opts = "";
							if (perm(sender, "set.enabled"))
								opts += (opts.equals("")?"enabled":", enabled");
							if (perm(sender, "set.delay"))
								opts += (opts.equals("")?"delay":", delay");
							if (perm(sender, "set.requireperms"))
								opts += (opts.equals("")?"requireperms":", requireperms");
							if (perm(sender, "set.prefix"))
								opts += (opts.equals("")?"prefix":", prefix");
							if (perm(sender, "set.postfix"))
								opts += (opts.equals("")?"postfix":", postfix");
							if (perm(sender, "set.random"))
								opts += (opts.equals("")?"random":", random");
							if (perm(sender, "set.sendtoconsole"))
								opts += (opts.equals("")?"sendtoconsole":", sendtoconsole");
							if (perm(sender, "set.colorconsole"))
								opts += (opts.equals("")?"colorconsole":", colorconsole");
							if (perm(sender, "set.sendtoplayer"))
								opts += (opts.equals("")?"sendtoplayer":", sendtoplayer");
							sender.sendMessage(Clr.AQUA + "Available set options: " + opts);
						}
					}
				} else {
					HashMap<String, String> rep = new HashMap<String, String>();
					rep.put("{SETNAME}", args[1]);
					sender.sendMessage(CustomMessage.CN_NOT_SET.pre(rep));
					if (perm(sender, "listsets"))
						listAvailbleGroups(sender);
				}
			} else {
				sender.sendMessage(CustomMessage.CN_NEEDS_SET.pre());
				if (perm(sender, "listsets"))
					listAvailbleGroups(sender);
			}
		} else if (MiscUtils.eq(args[0], "addmessage", "addm", "am")) { // '/cn addm <set> <msg>'
			if (!perm(sender, "addmessage")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			if (args.length < 2) {
				sender.sendMessage(CustomMessage.CN_NEEDS_SET.pre());
				if (perm(sender, "listsets"))
					listAvailbleGroups(sender);
				return;
			}
			if (args.length < 3) {
				sender.sendMessage(Clr.RED + "Not enough arguments!" + Clr.GRAY + " Usage: /cn addm <set> <msg>");
			} else {
				if (CarbonNewsModule.isMessageSet(args[1])) {
					BroadcastTask bt = CarbonNewsModule.getMessageSet(args[1]);
					String msg = args[2];
					for (int i = 3; i < args.length; i++)
						msg += " " + args[i];
					bt.addMessage(msg);
					boolean pl = bt.size() > 1;
					sender.sendMessage(Clr.GRAY + "Message added. '" + bt.getSetName() + "' set now has " + bt.size() + " message" + (pl?"s":"") + ".");
				} else {
					HashMap<String, String> rep = new HashMap<String, String>();
					rep.put("{SETNAME}", args[1]);
					sender.sendMessage(CustomMessage.CN_NOT_SET.pre(rep));
					if (perm(sender, "listsets"))
						listAvailbleGroups(sender);
				}
			}
		} else if (MiscUtils.eq(args[0], "removemessage", "removem", "deletemessage", "deletem", "delmessage", "delm", "rm", "dm")) { // '/cn removem <set> <msgID>'
			if (!perm(sender, "removemessage")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			if (args.length < 2) {
				sender.sendMessage(CustomMessage.CN_NEEDS_SET.pre());
				if (perm(sender, "listsets"))
					listAvailbleGroups(sender);
				return;
			}
			if (args.length < 3) {
				sender.sendMessage(Clr.RED + "Not enough arguments!" + Clr.GRAY + " Usage: /cn removem <set> <msgID>");
			} else {
				if (CarbonNewsModule.isMessageSet(args[1])) {
					BroadcastTask bt = CarbonNewsModule.getMessageSet(args[1]);
					if (MiscUtils.isInteger(args[2]) && Integer.parseInt(args[2]) >= 0) {
						if (bt.isEmpty()) {
							sender.sendMessage(Clr.RED + "This message set is already empty");
							return;
						}
						if (Integer.parseInt(args[2]) < bt.size()) {
							bt.removeMessage(Integer.parseInt(args[2]));
							if (bt.size() > 0) {
								boolean pl = bt.size() > 1;
								sender.sendMessage(Clr.GRAY + "Message removed. '" + bt.getSetName() + "' set now has " + bt.size() + " message" + (pl?"s":"") + ".");
							} else {
								bt.setEnabled(false);
								sender.sendMessage(Clr.GRAY + "Message removed. '" + bt.getSetName() + "' set now has 0 messages and is disabled.");
							}
						} else {
							sender.sendMessage("Message ID can't be found! Use /listmessages <set> to see messages");
						}
					} else {
						sender.sendMessage(Clr.RED + "Message ID must be positive number or 0");
					}
				} else {
					HashMap<String, String> rep = new HashMap<String, String>();
					rep.put("{SETNAME}", args[1]);
					sender.sendMessage(CustomMessage.CN_NOT_SET.pre(rep));
					if (perm(sender, "listsets"))
						listAvailbleGroups(sender);
				}
			}
		} else if (MiscUtils.eq(args[0], "setmessage", "setm", "sm", "udpatemessage", "updatem", "um")) { // '/cn setm <set> <msgID> <newMsg>'
			if (!perm(sender, "setmessage")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			if (args.length < 2) {
				sender.sendMessage(CustomMessage.CN_NEEDS_SET.pre());
				if (perm(sender, "listsets"))
					listAvailbleGroups(sender);
				return;
			}
			if (args.length < 4) {
				sender.sendMessage(Clr.RED + "Not enough arguments!" + Clr.GRAY + " Usage: /cn setm <set> <msgID> <newMsg>");
			} else {
				if (CarbonNewsModule.isMessageSet(args[1])) {
					BroadcastTask bt = CarbonNewsModule.getMessageSet(args[1]);
					if (MiscUtils.isInteger(args[2]) && Integer.parseInt(args[2]) >= 0) {
						if (bt.isEmpty()) {
							sender.sendMessage(Clr.RED + "This message set has no messages to modify");
							return;
						}
						if (Integer.parseInt(args[2]) < bt.size()) {
							String newMsg = args[3];
							if (args.length > 4)
								for (int i = 4; i < args.length; i++)
									newMsg += " " + args[i];
							bt.updateMessage(Integer.parseInt(args[2]), newMsg);
							sender.sendMessage(Clr.GRAY + "Message updated.");
						} else {
							sender.sendMessage("Message ID can't be found! Use /listmessages <set> to see messages");
						}
					} else {
						sender.sendMessage(Clr.RED + "Message ID must be positive number or 0");
					}
				} else {
					HashMap<String, String> rep = new HashMap<String, String>();
					rep.put("{SETNAME}", args[1]);
					sender.sendMessage(CustomMessage.CN_NOT_SET.pre(rep));
					if (perm(sender, "listsets"))
						listAvailbleGroups(sender);
				}
			}
		} else if (MiscUtils.eq(args[0], "addset", "addg", "ag")) { // '/cn addg <set>'
			if (!perm(sender, "addset")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			if (args.length < 2) {
				sender.sendMessage(CustomMessage.CN_NEEDS_SET.pre());
				if (perm(sender, "listsets"))
					listAvailbleGroups(sender);
				return;
			}
			if (CarbonNewsModule.isMessageSet(args[1])) {
				sender.sendMessage(Clr.RED + "That set already exists. Groups must have unique names!");
				return;
			}
			BroadcastTask bt = new BroadcastTask(args[1]);
			bt.runTaskTimer(CarbonKit.inst, 1200L, 1200L);
			CarbonNewsModule.tasks.add(bt);
			boolean pl = CarbonNewsModule.tasks.size() != 1;
			sender.sendMessage(Clr.GRAY + "Message set '" + args[1] + "' created. " + CarbonNewsModule.tasks.size() + " set" + (pl?"s":"") +  " now exist" + (pl?"":"s") + ".");
		} else if (MiscUtils.eq(args[0], "removeset", "removeg", "deleteset", "deleteg", "delset", "delg", "rg", "dg")) { // '/cn removeg <set>'
			if (!perm(sender, "removeset")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			if (args.length < 2) {
				sender.sendMessage(CustomMessage.CN_NEEDS_SET.pre());
				if (perm(sender, "listsets"))
					listAvailbleGroups(sender);
				return;
			}
			if (!CarbonNewsModule.isMessageSet(args[1])) {
				sender.sendMessage(Clr.RED + "Could not find set '" + args[1] + "'");
				return;
			}
			BroadcastTask bt = CarbonNewsModule.getMessageSet(args[1]);
			if (Bukkit.getScheduler().isCurrentlyRunning(bt.getTaskId()))
				bt.cancel();
			if (CarbonKit.getConfig(ConfType.NEWS).contains("MessageSets." + CarbonNewsModule.getMessageSet(args[1]).getSetName()))
				CarbonKit.getConfig(ConfType.NEWS).set("MessageSets." + CarbonNewsModule.getMessageSet(args[1]).getSetName(), null);
			if (CarbonNewsModule.tasks.contains(bt))
				CarbonNewsModule.tasks.remove(bt);
			CarbonKit.saveConfig(ConfType.NEWS);
			boolean pl = CarbonNewsModule.tasks.size() != 1;
			sender.sendMessage(Clr.GRAY + "Message set '" + args[1] + "' removed. " + CarbonNewsModule.tasks.size() + " set" + (pl?"s":"") +  " now exist" + (pl?"":"s") + ".");
		} else if (MiscUtils.eq(args[0], "broadcast", "bcast", "bc")) { // '/cn bcast <set> <msgID>'
			if (!perm(sender, "broadcast")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return;
			}
			if (args.length < 3) {
				sender.sendMessage(Clr.RED + "Not enough arguments!" + Clr.GRAY + " Usage: /cn bcast <set> <msgID>");
			} else {
				if (CarbonNewsModule.isMessageSet(args[1])) {
					BroadcastTask bt = CarbonNewsModule.getMessageSet(args[1]);
					//List<String> messages = CarbonNewsModule.config.getStringList("MessageGroups." + bt.getSetName() + ".messages");
					if (bt.isEmpty()) {
						sender.sendMessage(Clr.RED + "That message set has no messages to broadcast");
						return;
					}
					if (MiscUtils.isInteger(args[2]) && Integer.parseInt(args[2]) >= 0) {
						if (Integer.parseInt(args[2]) < bt.size()) {
							sender.sendMessage(Clr.GRAY + "Broadcasting message ID: " + args[2] + " from set: " + bt.getSetName());
							String msg = bt.getPrefix() + bt.getMessage(Integer.parseInt(args[2])) + bt.getPostfix();
							int i = 0;
							Player[] plArray = new Player[Bukkit.getOnlinePlayers().size()];
							for (Player pl : Bukkit.getOnlinePlayers()) { plArray[i] = pl; i++; }
							CarbonNewsModule.broadcastFormatted(msg, plArray, bt.requirePerms(), bt.getPerm());
						} else {
							sender.sendMessage(Clr.RED + "Message ID can't be found! Use /cn listm <set> to see messages");
						}
					} else {
						sender.sendMessage(Clr.RED + "Message ID must be positive number or 0");
					}
				} else {
					HashMap<String, String> rep = new HashMap<String, String>();
					rep.put("{SETNAME}", args[1]);
					sender.sendMessage(CustomMessage.CN_NOT_SET.pre(rep));
					if (perm(sender, "listsets"))
						listAvailbleGroups(sender);
				}
			}
		} else {
			sender.sendMessage(Clr.RED + "Could not find command: " + args[0]);
			printHelp(sender);
		}
	}

	private void printHelp(CommandSender sender) {
		if (!MiscUtils.perm(sender, allPerms) && !perm(sender, "info")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		sender.sendMessage(Clr.TITLE + "===[ CarbonNewsModule v." + CarbonKit.inst.getDescription().getVersion() + " - Info ]===");
		if (perm(sender, "info")) {
			int ts = CarbonNewsModule.tasks.size(), enabled = 0, toPlayers = 0, toConsole = 0;
			sender.sendMessage(Clr.AQUA + "" + ts + " sets currently loaded");
			for (BroadcastTask bt : CarbonNewsModule.tasks) {
				enabled += (bt.isEnabled())?1:0;
				toPlayers += (bt.isSentToPlayers())?1:0;
				toConsole += (bt.isSentToConole())?1:0;
			}
			sender.sendMessage(Clr.AQUA + "" + enabled + "/" + ts + " sets enabled");
			sender.sendMessage(Clr.AQUA + "" + toPlayers + "/" + ts + " sets shown to players");
			sender.sendMessage(Clr.AQUA + "" + toConsole + "/" + ts + " sets shown to console");
			sender.sendMessage(Clr.TITLE + "=============");
		}
		if (perm(sender, allPerms)) {
			if (perm(sender, "set.enabled"))
				sender.sendMessage(Clr.AQUA + "/cn toggle <set> [on|off]");
			if (perm(sender, "reload"))
				sender.sendMessage(Clr.AQUA + "/cn reload");
			if (perm(sender, "setinfo"))
				sender.sendMessage(Clr.AQUA + "/cn setinfo <set>");
			if (perm(sender, "listsets"))
				sender.sendMessage(Clr.AQUA + "/cn listsets");
			if (perm(sender, "listmessages"))
				sender.sendMessage(Clr.AQUA + "/cn listmessages <set>");
			if (perm(sender, "addmessage"))
				sender.sendMessage(Clr.AQUA + "/cn addmessage <set> <msg>");
			if (perm(sender, "removemessage"))
				sender.sendMessage(Clr.AQUA + "/cn removemessage <set> <msgID>");
			if (perm(sender, "setmessage"))
				sender.sendMessage(Clr.AQUA + "/cn setmessage <set> <msgID> <newMsg>");
			if (perm(sender, "addset"))
				sender.sendMessage(Clr.AQUA + "/cn addset <set>");
			if (perm(sender, "removeset"))
				sender.sendMessage(Clr.AQUA + "/cn removeset <set>");
			if (perm(sender, "set.enabled"))
				sender.sendMessage(Clr.AQUA + "/cn set <set> enabled <true|false>");
			if (perm(sender, "set.delay"))
				sender.sendMessage(Clr.AQUA + "/cn set <set> delay <value>");
			if (perm(sender, "set.requireperms"))
				sender.sendMessage(Clr.AQUA + "/cn set <set> requireperms <true|false>");
			if (perm(sender, "set.prefix"))
				sender.sendMessage(Clr.AQUA + "/cn set <set> prefix <value>");
			if (perm(sender, "set.postfix"))
				sender.sendMessage(Clr.AQUA + "/cn set <set> postfix <value>");
			if (perm(sender, "set.random"))
				sender.sendMessage(Clr.AQUA + "/cn set <set> random <true|false>");
			if (perm(sender, "set.sendtoconsole"))
				sender.sendMessage(Clr.AQUA + "/cn set <set> sendtoconsole <true|false>");
			if (perm(sender, "set.colorconsole"))
				sender.sendMessage(Clr.AQUA + "/cn set <set> colorconsole <true|false>");
			if (perm(sender, "set.sendtoplayer"))
				sender.sendMessage(Clr.AQUA + "/cn set <set> sendtoplayer <true|false>");
			sender.sendMessage(Clr.TITLE + "=============");
		}
	}

	private void listAvailbleGroups(CommandSender sender) {
		String gList = "";
		for (BroadcastTask bt : CarbonNewsModule.tasks)
			gList += Clr.GRAY + (gList.equals("")?"":", ") + (bt.isEnabled()?Clr.LIME:Clr.RED) + bt.getSetName();
		sender.sendMessage(Clr.AQUA + "Available sets: " + ((gList.length()>0)?gList:Clr.GRAY + "No sets"));
	}

	private boolean perm(CommandSender sender, String... nodes) {
		for (String n : nodes)
			if (MiscUtils.perm(sender, "carbonkit.news." + n))
				return true;
		return false;
	}

}