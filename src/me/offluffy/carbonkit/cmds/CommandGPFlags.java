package me.offluffy.carbonkit.cmds;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.modules.ModuleGPFlags;
import me.offluffy.carbonkit.utils.FlaggedClaim;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Clr;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;
import me.ryanhamshire.GriefPrevention.Claim;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGPFlags extends ModuleCmd {
	public ModuleGPFlags gMod = (ModuleGPFlags)Module.getModule("GriefPreventionFlags");
	public CommandGPFlags(Module module) {
		super(module, "gpflag");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length != 0) {
			Claim c = ModuleGPFlags.gp.getClaimAt(((Player)sender).getLocation(), false, null);
			if (c == null) {
				sender.sendMessage(Clr.ERR + "There's no claim to flag here!");
				return true;
			}
			FlaggedClaim fc = gMod.getFlaggedClaim(c);
			if (Lib.eq(args[0], "help")) {
				if (!Lib.perm(sender, "gpflag.help")) {
					Messages.send(sender, Message.NO_PERM);
					return true;
				}
				if (args.length > 1) {
					if (Lib.eq(args[1], "n","nickname","alias","nick","name")) {
						if (Lib.perm(sender, "gpflag.name")) {
							sender.sendMessage("\u00A7a/gpf name <name>");
							sender.sendMessage(Clr.HEAD + "<name> " + Clr.NORM + "- Name of your claim (Don't put <> symbols)");
							sender.sendMessage(Clr.NOTE + "No spaces! Underscores are replace with spaces");
						} else {
							Messages.send(sender, Message.NO_PERM);
							return true;
						}
					}
					if (Lib.eq(args[1], "g","greeting","greet","welcome","enter")) {
						if (Lib.perm(sender, "gpflag.greeting")) {
							sender.sendMessage("\u00A7a/gpf greeting <greeting>");
							sender.sendMessage(Clr.HEAD + "<greeting> " + Clr.NORM + "- What to say on entry (Don't put <> symbols)");
							sender.sendMessage(Clr.NOTE + "There are a few variables you can use:");
							sender.sendMessage(Clr.HEAD + "{PLAYER} " + Clr.NORM + "- The player who entered the claim");
							sender.sendMessage(Clr.HEAD + "{OWNER} " + Clr.NORM + "- The player who owns the claim");
							sender.sendMessage(Clr.HEAD + "{NAME} " + Clr.NORM + "- The name flag of the claim");
						} else {
							Messages.send(sender, Message.NO_PERM);
							return true;
						}
					}
					if (Lib.eq(args[1], "f","farewell","leave","exit")) {
						if (Lib.perm(sender, "gpflag.farewell")) {
							sender.sendMessage("\u00A7a/gpf farewell <farewell>");
							sender.sendMessage(Clr.HEAD + "<farewell> " + Clr.NORM + "- What to say on exit (Don't put <> symbols)");
							sender.sendMessage(Clr.NOTE + "There are a few variables you can use:");
							sender.sendMessage(Clr.HEAD + "{PLAYER} " + Clr.NORM + "- The player who left the claim");
							sender.sendMessage(Clr.HEAD + "{OWNER} " + Clr.NORM + "- The player who owns the claim");
							sender.sendMessage(Clr.HEAD + "{NAME} " + Clr.NORM + "- The name flag of the claim");
						} else {
							Messages.send(sender, Message.NO_PERM);
							return true;
						}
					}
					if (Lib.eq(args[1], "hs","ms","hostile","monster","hostiles","monsters","hostilespawns","monsterspawns","hostilespawning","monsterspawning")) {
						if (Lib.perm(sender, "gpflag.spawning.hostile")) {
							sender.sendMessage("\u00A7a/gpflag hostile <mode>");
							sender.sendMessage(Clr.HEAD + "<true|false> " + Clr.NORM + "- Whether or not to prevent hostile mob spawning");
						} else {
							Messages.send(sender, Message.NO_PERM);
							return true;
						}
					}
					if (Lib.eq(args[1], "ps","as","passive","animal","passives","animals","passivespawns","animalspawns","passivespawning","animalspawning")) {
						if (Lib.perm(sender, "gpflag.spawning.passive")) {
							sender.sendMessage("\u00A7a/gpflag passive <mode>");
							sender.sendMessage(Clr.HEAD + "<true|false> " + Clr.NORM + "- Whether or not to prevent passive mob spawning");
						} else {
							Messages.send(sender, Message.NO_PERM);
							return true;
						}
					}
					return true;
				} else {
					if (Lib.perm(sender, "gpflag.name"))
						sender.sendMessage(Clr.HEAD + "/gpf help name " + Clr.NORM + "- Name flag help");
					if (Lib.perm(sender, "gpflag.greeting"))
						sender.sendMessage(Clr.HEAD + "/gpf help greeting " + Clr.NORM + "- Greeting flag help");
					if (Lib.perm(sender, "gpflag.farewell"))
						sender.sendMessage(Clr.HEAD + "/gpf help farewell " + Clr.NORM + "- Farewell flag help");
					if (Lib.perm(sender, "gpflag.spawning.hostile"))
						sender.sendMessage(Clr.HEAD + "/gpf help hs " + Clr.NORM + "- Hostile spawning flag help");
					if (Lib.perm(sender, "gpflag.spawning.passive"))
						sender.sendMessage(Clr.HEAD + "/gpf help ps " + Clr.NORM + "- Passive spawning flag help");
					return true;
				}
			} else if (Lib.eq(args[0], "check","c")) {
				if (!c.getOwnerName().equalsIgnoreCase(sender.getName()) && !Lib.perm(sender, "gpflag.check")) {
					sender.sendMessage(Clr.ERR + "You can't check this claim (you don't own it)!");
					return true;
				}
				if (fc != null) {
					sender.sendMessage(Clr.TITLE + "Current Claim Flags");
					if (fc.isSet(FlaggedClaim.NAME))
						sender.sendMessage(Clr.HEAD + "Name: " + Clr.NORM + fc.getName());
					if (fc.isSet(FlaggedClaim.GREETING))
						sender.sendMessage(Clr.HEAD + "Greeting: " + Clr.NORM + fc.getGreeting());
					if (fc.isSet(FlaggedClaim.FAREWELL))
						sender.sendMessage(Clr.HEAD + "Farewell: " + Clr.NORM + fc.getFarewell());
					if (fc.isSet(FlaggedClaim.HOSSPAWN))
						sender.sendMessage(Clr.HEAD + "Hostile Spawning: " + Clr.NORM + ((fc.getHostileSpawning())?"Enabled":"Disabled"));
					if (fc.isSet(FlaggedClaim.ANISPAWN))
						sender.sendMessage(Clr.HEAD + "Passive Spawning: " + Clr.NORM + ((fc.getPassiveSpawning())?"Enabled":"Disabled"));
					return true;
				} else {
					sender.sendMessage(Clr.HEAD + "There are no flags on this claim");
					return true;
				}
			} else if (Lib.eq(args[0], "n","nickname","alias","nick","name")) {
				if (!c.getOwnerName().equalsIgnoreCase(sender.getName()) && !Lib.perm(sender, "gpflag.admin")) {
					sender.sendMessage(Clr.ERR + "You can't flag this claim (you don't own it)!");
					return true;
				}
				if(!Lib.perm(sender, "gpflag.name")) {
					Messages.send(sender, Message.NO_PERM);
					return true;
				}
				if (fc != null) {
					if (args.length == 1) {
						if (fc.isSet(FlaggedClaim.NAME)) {
							fc.removeName();
							sender.sendMessage(Clr.HEAD + "Name flag removed");
							return true;
						} else {
							sender.sendMessage(Clr.HEAD + "There's no name flag to remove");
							return true;
						}
					} else {
						String name = args[1];
						fc.setName(name.replace("_", " "));
						sender.sendMessage(Clr.HEAD + "Claim's name flag set to " + name.replace("_", " "));
						return true;
					}
				} else {
					if (args.length == 1) {
						sender.sendMessage(Clr.HEAD + "There's no name flag to remove");
						return true;
					} else {
						FlaggedClaim nfc = new FlaggedClaim(c);
						if (nfc.getId() == -1L) {
							sender.sendMessage(Clr.ERR + "You can't flag subclaims!");
							return true;
						}
						String name = args[1];
						nfc.setName(name.replace("_", " "));
						sender.sendMessage(Clr.HEAD + "Claim's name flag set to " + name.replace("_", " "));
						FlaggedClaim.flaggedClaims.add(nfc);
						return true;
					}
				}
			} else if (Lib.eq(args[0], "g","greeting","greet","welcome","enter")) {
				if (!c.getOwnerName().equalsIgnoreCase(sender.getName()) && !Lib.perm(sender, "gpflag.admin")) {
					sender.sendMessage(Clr.ERR + "You can't flag this claim (you don't own it)!");
					return true;
				}
				if (!Lib.perm(sender, "gpflag.greeting")) {
					Messages.send(sender, Message.NO_PERM);
					return true;
				}
				if (fc != null) {
					if (args.length == 1) {
						if (fc.isSet(FlaggedClaim.GREETING)) {
							fc.removeGreeting();
							sender.sendMessage(Clr.HEAD + "Greeting flag removed");
							return true;
						} else {
							sender.sendMessage(Clr.HEAD + "There's no greeting flag to remove");
							return true;
						}
					} else {
						String argString = args[1];
						for (int i = 2; i < args.length; i++)
							argString += " " + args[i];
						fc.setGreeting(argString);
						sender.sendMessage(Clr.HEAD + "Claim's greeting flag set to " + argString);
						return true;
					}
				} else {
					if (args.length == 1) {
						sender.sendMessage(Clr.HEAD + "There's no greeting flag to remove");
						return true;
					} else {
						String argString = args[1];
						for (int i = 2; i < args.length; i++)
							argString += " " + args[i];
						FlaggedClaim nfc = new FlaggedClaim(c);
						if (nfc.getId() == -1L) {
							//nfc = new FlaggedClaim(c.parent);
							sender.sendMessage(Clr.ERR + "You can't flag subclaims!");
							return true;
						}
						nfc.setGreeting(argString);
						sender.sendMessage(Clr.HEAD + "Claim's greeting flag set to " + argString);
						FlaggedClaim.flaggedClaims.add(nfc);
						return true;
					}
				}
			} else if (Lib.eq(args[0], "f","farewell","leave","exit")) {
				if (!c.getOwnerName().equalsIgnoreCase(sender.getName()) && !Lib.perm(sender, "gpflag.admin")) {
					sender.sendMessage(Clr.ERR + "You can't flag this claim (you don't own it)!");
					return true;
				}
				if (!Lib.perm(sender, "gpflag.farewell")) {
					Messages.send(sender, Message.NO_PERM);
					return true;
				}
				if (fc != null) {
					if (args.length == 1) {
						if (fc.isSet(FlaggedClaim.FAREWELL)) {
							fc.removeFarewell();
							sender.sendMessage(Clr.HEAD + "Farewell flag removed");
							return true;
						} else {
							sender.sendMessage(Clr.HEAD + "There's no farewell flag to remove");
							return true;
						}
					} else {
						String argString = args[1];
						for (int i = 2; i < args.length; i++)
							argString += " " + args[i];
						fc.setFarewell(argString);
						sender.sendMessage(Clr.HEAD + "Claim's farewell flag set to " + argString);
						return true;
					}
				} else {
					if (args.length == 1) {
						sender.sendMessage(Clr.HEAD + "There's no farewell flag to remove");
						return true;
					} else {
						String argString = args[1];
						for (int i = 2; i < args.length; i++)
							argString += " " + args[i];
						FlaggedClaim nfc = new FlaggedClaim(c);
						if (nfc.getId() == -1L) {
							sender.sendMessage(Clr.ERR + "You can't flag subclaims!");
							return true;
						}
						nfc.setFarewell(argString);
						sender.sendMessage(Clr.HEAD + "Claim's farewell flag set to " + argString);
						FlaggedClaim.flaggedClaims.add(nfc);
						return true;
					}
				}
			} else if (Lib.eq(args[0], "hs","ms","hostile","monster","hostiles","monsters","hostilespawns","monsterspawns","hostilespawning","monsterspawning")) {
				if (!c.getOwnerName().equalsIgnoreCase(sender.getName()) && !Lib.perm(sender, "gpflag.admin")) {
					sender.sendMessage(Clr.ERR + "You can't flag this claim (you don't own it)!");
					return true;
				}
				if (!Lib.perm(sender,  "gpflag.spawning.hostile")) {
					Messages.send(sender, Message.NO_PERM);
					return true;
				}
				if (fc != null) {
					if (args.length == 1) {
						if (fc.isSet(FlaggedClaim.HOSSPAWN)) {
							fc.removeHostileSpawning();
							sender.sendMessage(Clr.HEAD + "Hostile spawning flag removed");
							return true;
						} else {
							sender.sendMessage(Clr.HEAD + "There's no hostile spawning flag to remove");
							return true;
						}
					} else {
						boolean mode = true;
						if (Lib.eq(args[1], "1","on","true","enabled")) {
							mode = true;
						} else if (Lib.eq(args[1], "0","off","false","disabled")) {
							mode = false;
						} else {
							sender.sendMessage(Clr.ERR + "Spawning: " + args[1] + " not found");
							sender.sendMessage(Clr.NORM + "true - Enable mob spawning");
							sender.sendMessage(Clr.NORM + "false - Disable mob spawning");
							return true;
						}
						fc.setHostileSpawning(mode);
						sender.sendMessage(Clr.HEAD + "Claim's hostile spawning " + Clr.NORM + ((mode)?"enabled":"disabled"));
						return true;
					}
				} else {
					if (args.length == 1) {
						sender.sendMessage(Clr.HEAD + "There's no hostile spawning flag to remove");
						return true;
					} else {
						boolean mode = true;
						if (Lib.eq(args[1], "1","on","true","enabled")) {
							mode = true;
						} else if (Lib.eq(args[1], "0","off","false","disabled")) {
							mode = false;
						} else {
							sender.sendMessage(Clr.ERR + "Spawning: " + args[1] + " not found");
							sender.sendMessage(Clr.NORM + "true - Enable mob spawning");
							sender.sendMessage(Clr.NORM + "false - Disable mob spawning");
							return true;
						}
						FlaggedClaim nfc = new FlaggedClaim(c);
						if (nfc.getId() == -1L) {
							//nfc = new FlaggedClaim(c.parent);
							sender.sendMessage(Clr.ERR + "You can't flag subclaims!");
							return true;
						}
						nfc.setHostileSpawning(mode);
						sender.sendMessage(Clr.HEAD + "Claim's hostile spawning " + Clr.NORM + ((mode)?"enabled":"disabled"));
						FlaggedClaim.flaggedClaims.add(nfc);
						return true;
					}
				}
			} else if (Lib.eq(args[0], "ps","as","passive","animal","passives","animals","passivespawns","animalspawns","passivespawning","animalspawning")) {
				if (!c.getOwnerName().equalsIgnoreCase(sender.getName()) && !Lib.perm(sender, "gpflag.admin")) {
					sender.sendMessage(Clr.ERR + "You can't flag this claim (you don't own it)!");
					return true;
				}
				if (!Lib.perm(sender,  "gpflag.spawning.passive")) {
					Messages.send(sender, Message.NO_PERM);
					return true;
				}
				if (fc != null) {
					if (args.length == 1) {
						if (fc.isSet(FlaggedClaim.ANISPAWN)) {
							fc.removePassiveSpawning();
							sender.sendMessage(Clr.HEAD + "Passive spawning flag removed");
							return true;
						} else {
							sender.sendMessage(Clr.HEAD + "There's no passive spawning flag to remove");
							return true;
						}
					} else {
						boolean mode = true;
						if (Lib.eq(args[1], "1","on","true","enabled")) {
							mode = true;
						} else if (Lib.eq(args[1], "0","off","false","disabled")) {
							mode = false;
						} else {
							sender.sendMessage(Clr.ERR + "Spawning: " + args[1] + " not found");
							sender.sendMessage(Clr.NORM + "true - Enable mob spawning");
							sender.sendMessage(Clr.NORM + "false - Disable mob spawning");
							return true;
						}
						fc.setPassiveSpawning(mode);
						sender.sendMessage(Clr.HEAD + "Claim's passive spawning " + Clr.NORM + ((mode)?"enabled":"disabled"));
						return true;
					}
				} else {
					if (args.length == 1) {
						sender.sendMessage(Clr.HEAD + "There's no passive spawning flag to remove");
						return true;
					} else {
						boolean mode = true;
						if (Lib.eq(args[1], "1","on","true","enabled")) {
							mode = true;
						} else if (Lib.eq(args[1], "0","off","false","disabled")) {
							mode = false;
						} else {
							sender.sendMessage(Clr.ERR + "Spawning: " + args[1] + " not found");
							sender.sendMessage(Clr.NORM + "true - Enable mob spawning");
							sender.sendMessage(Clr.NORM + "false - Disable mob spawning");
							return true;
						}
						FlaggedClaim nfc = new FlaggedClaim(c);
						if (nfc.getId() == -1L) {
							sender.sendMessage(Clr.ERR + "You can't flag subclaims!");
							return true;
						}
						nfc.setPassiveSpawning(mode);
						sender.sendMessage(Clr.HEAD + "Claim's passive spawning " + Clr.NORM + ((mode)?"enabled":"disabled"));
						FlaggedClaim.flaggedClaims.add(nfc);
						return true;
					}
				}
			} else {
				sender.sendMessage(Clr.ERR + "Unknown flag");
				return true;
			}
		} else {
			if (Lib.perm(sender, "gpflag.name"))
				sender.sendMessage(Clr.HEAD + "/gpf help name " + Clr.NORM + "- Name flag help");
			if (Lib.perm(sender, "gpflag.greeting"))
				sender.sendMessage(Clr.HEAD + "/gpf help greeting " + Clr.NORM + "- Greeting flag help");
			if (Lib.perm(sender, "gpflag.farewell"))
				sender.sendMessage(Clr.HEAD + "/gpf help farewell " + Clr.NORM + "- Farewell flag help");
			if (Lib.perm(sender, "gpflag.spawning.hostile"))
				sender.sendMessage(Clr.HEAD + "/gpf help hs " + Clr.NORM + "- Hostile spawning flag help");
			if (Lib.perm(sender, "gpflag.spawning.passive"))
				sender.sendMessage(Clr.HEAD + "/gpf help ps " + Clr.NORM + "- Passive spawning flag help");
			return true;
		}
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("carbonkit.permission"));
		return perms;
	}

}
