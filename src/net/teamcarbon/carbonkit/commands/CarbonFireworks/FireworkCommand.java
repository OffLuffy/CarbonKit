package net.teamcarbon.carbonkit.commands.CarbonFireworks;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.modules.CarbonFireworksModule;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.Firework.FireworkPreset;
import net.teamcarbon.carbonkit.utils.Firework.FireworkUtils;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import net.teamcarbon.carbonlib.Misc.NumUtils;
import net.teamcarbon.carbonlib.Misc.TypeUtils;
import org.bukkit.Bukkit;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal", "deprecation"})
public class FireworkCommand extends ModuleCmd {

	public FireworkCommand(Module module) { super(module, "firework"); }

	private final String CKF_PERM = "carbonkit.fireworks.";

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length < 1 || MiscUtils.eq(args[0], "help")) {
			if (MiscUtils.perm(sender, CKF_PERM + "help.presets", CKF_PERM + "help.launch", CKF_PERM + "help.effect",
					CKF_PERM + "help.spawn", CKF_PERM + "help.arrow", CKF_PERM + "help.snowball")) {
				if (args.length < 1 || (args.length == 1 && MiscUtils.eq(args[0], "help"))) {
					CustomMessage.printHeader(sender, "Fireworks Help Categories");
					if (MiscUtils.perm(sender, CKF_PERM + "help.presets"))
						sender.sendMessage(Clr.AQUA + "/fw help presets" + Clr.DARKAQUA + " - Firework presets help");
					if (MiscUtils.perm(sender, CKF_PERM + "help.launch"))
						sender.sendMessage(Clr.AQUA + "/fw help launch" + Clr.DARKAQUA + " - Firework launch help");
					if (MiscUtils.perm(sender, CKF_PERM + "help.effect"))
						sender.sendMessage(Clr.AQUA + "/fw help effect" + Clr.DARKAQUA + " - Firework effect help");
					if (MiscUtils.perm(sender, CKF_PERM + "help.spawn"))
						sender.sendMessage(Clr.AQUA + "/fw help spawn" + Clr.DARKAQUA + " - Firework spawn help");
					if (MiscUtils.perm(sender, CKF_PERM + "help.arrow"))
						sender.sendMessage(Clr.AQUA + "/fw help arrow" + Clr.DARKAQUA + " - Firework arrow help");
					if (MiscUtils.perm(sender, CKF_PERM + "help.snowball"))
						sender.sendMessage(Clr.AQUA + "/fw help snowball" + Clr.DARKAQUA + " - Firework snowball help");
				} else if (MiscUtils.eq(args[0], "help")) {
					if (MiscUtils.eq(args[1], "presets") && MiscUtils.perm(sender, CKF_PERM + "help.presets")) {
						CustomMessage.printHeader(sender, "Fireworks Presets Help");
						if (MiscUtils.perm(sender, CKF_PERM + "presets.create", CKF_PERM + "presets.modify", CKF_PERM
								+ "presets.modify.others"))
							sender.sendMessage(Clr.AQUA + "/fw pre [preset] [params|hand]" + Clr.DARKAQUA
									+ " Creates or modifies a preset");
						if (MiscUtils.perm(sender, CKF_PERM + "presets.list"))
							sender.sendMessage(Clr.AQUA + "/fw pre list [page]" + Clr.DARKAQUA + " List your presets");
						if (MiscUtils.perm(sender, CKF_PERM + "presets.list.others"))
							sender.sendMessage(Clr.AQUA + "/fw pre list <player> [page]" + Clr.DARKAQUA
									+ " List another player's presets");
						if (MiscUtils.perm(sender, CKF_PERM + "presets.listall"))
							sender.sendMessage(Clr.AQUA + "/fw pre listall [page]" + Clr.DARKAQUA + " List all presets");
						if (MiscUtils.perm(sender, CKF_PERM + "presets.delete", CKF_PERM + "presets.delete.others"))
							sender.sendMessage(Clr.AQUA + "/fw pre delete [preset]" + Clr.DARKAQUA + " Deletes the preset");
						if (MiscUtils.perm(sender, CKF_PERM + "presets.combine", CKF_PERM + "presets.combine.others"))
							sender.sendMessage(Clr.AQUA + "/fw pre combine [preset] [params|preset|hand]" + Clr.DARKAQUA
									+ " Merges effects into a preset");
					} else if (MiscUtils.eq(args[1], "launch") && MiscUtils.perm(sender, CKF_PERM + "help.launch")) {
						CustomMessage.printHeader(sender, "Fireworks Launch Help");
						if (MiscUtils.perm(sender, CKF_PERM + "launch.preset")) {
							sender.sendMessage(Clr.AQUA + "/fw launch [preset]" + Clr.DARKAQUA + " Launch a preset");
							sender.sendMessage(Clr.AQUA + "/fw launch [power] [preset]" + Clr.DARKAQUA
									+ " Launch a preset with power");
						}
						if (MiscUtils.perm(sender, CKF_PERM + "launch.params")) {
							sender.sendMessage(Clr.AQUA + "/fw launch [params]" + Clr.GRAY
									+ " Launch a paramaterized firework");
							sender.sendMessage(Clr.AQUA + "/fw launch [power] [params]" + Clr.GRAY
									+ " Launch a paramaterized firework with power");
						}
						if (MiscUtils.perm(sender, CKF_PERM + "launch.hand")) {
							sender.sendMessage(Clr.AQUA + "/fw launch hand" + Clr.GRAY
									+ " Launch a copy of the in-hand firework");
							sender.sendMessage(Clr.AQUA + "/fw launch hand [power]" + Clr.GRAY
									+ " Launch a copy of the in-hand firework with power");
						}
					} else if (MiscUtils.eq(args[1], "effect") && MiscUtils.perm(sender, CKF_PERM
							+ "help.effect")) {
						CustomMessage.printHeader(sender, "Fireworks Effects Help");
						if (MiscUtils.perm(sender, CKF_PERM + "effect.preset"))
							sender.sendMessage(Clr.AQUA + "/fw effect [preset]" + Clr.GRAY
									+ " Produce a preset effect");
						if (MiscUtils.perm(sender, CKF_PERM + "effect.params"))
							sender.sendMessage(Clr.AQUA + "/fw effect [params]" + Clr.GRAY
									+ " Launch a paramaterized firework");
						if (MiscUtils.perm(sender, CKF_PERM + "effect.hand"))
							sender.sendMessage(Clr.AQUA + "/fw effect hand" + Clr.GRAY
									+ " Launch a copy of the in-hand firework");
					} else if (MiscUtils.eq(args[1], "spawn") && MiscUtils.perm(sender, CKF_PERM + "help.spawn")) {
						CustomMessage.printHeader(sender, "Fireworks Spawn Help");
						if (MiscUtils.perm(sender, CKF_PERM + "spawn.preset"))
							sender.sendMessage(Clr.RED + "Usage: /fw spawn <preset> [user]");
						if (MiscUtils.perm(sender, CKF_PERM + "spawn.params"))
							sender.sendMessage(Clr.RED + "Usage: /fw spawn <params> [user]");
					} else if (MiscUtils.eq(args[1], "arrow") && MiscUtils.perm(sender, CKF_PERM + "help.arrow")) {
						CustomMessage.printHeader(sender, "Fireworks Arrow Help");
						if (MiscUtils.perm(sender, CKF_PERM + "toggle")) {
							sender.sendMessage(Clr.AQUA + "/fw a <on|off>" + Clr.DARKAQUA
									+ " - Turn firework arrows on or off");
							sender.sendMessage(Clr.AQUA + "/fw a toggle" + Clr.DARKAQUA
									+ " - Toggle firework arrows");
						}
						if (MiscUtils.perm(sender, CKF_PERM + "toggle.others")) {
							sender.sendMessage(Clr.AQUA + "/fw a <on|off> [user]" + Clr.DARKAQUA
									+ " - Turn firework arrows on or off for others");
							sender.sendMessage(Clr.AQUA + "/fw a toggle [user]" + Clr.DARKAQUA
									+ " - Toggle firework arrows for others");
						}
						if (MiscUtils.perm(sender, CKF_PERM + "preset"))
							sender.sendMessage(Clr.AQUA + "/fw a preset <preset>" + Clr.DARKAQUA
									+ " - Set the firework arrow effect");
						if (MiscUtils.perm(sender, CKF_PERM + "preset.others"))
							sender.sendMessage(Clr.AQUA + "/fw a preset <preset> [user]" + Clr.DARKAQUA
									+ " - Set the firework arrow effect for others");
					} else if (MiscUtils.eq(args[1], "snowball") && MiscUtils.perm(sender, CKF_PERM
							+ "help.snowball")) {
						CustomMessage.printHeader(sender, "Fireworks Snowball Help");
						if (MiscUtils.perm(sender, CKF_PERM + "snowball.toggle")) {
							sender.sendMessage(Clr.AQUA + "/fw s <on|off>" + Clr.DARKAQUA
									+ " - Turn firework snowballs on or off");
							sender.sendMessage(Clr.AQUA + "/fw s toggle" + Clr.DARKAQUA
									+ " - Toggle firework snowballs");
						}
						if (MiscUtils.perm(sender, CKF_PERM + "snowball.toggle.others")) {
							sender.sendMessage(Clr.AQUA + "/fw s <on|off> [user]" + Clr.DARKAQUA
									+ " - Turn firework snowballs on or off for others");
							sender.sendMessage(Clr.AQUA + "/fw s toggle [user]" + Clr.DARKAQUA
									+ " - Toggle firework snowballs for others");
						}
						if (MiscUtils.perm(sender, CKF_PERM + "snowball.preset"))
							sender.sendMessage(Clr.AQUA + "/fw s preset <preset>" + Clr.DARKAQUA
									+ " - Set the firework snowball effect");
						if (MiscUtils.perm(sender, CKF_PERM + "snowball.preset.others"))
							sender.sendMessage(Clr.AQUA + "/fw s preset <preset> [user]" + Clr.DARKAQUA
									+ " - Set the firework snowball effect for others");
					} else {
						sender.sendMessage(Clr.RED + "Argument not recognized: " + args[1]);
					}
				}
			} else {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			}
		} else {
			if (MiscUtils.eq(args[0], "preset", "pre")) {
				/* *****************************************************************************************************
				*****                                                                                       PRESET *****
				***************************************************************************************************** */
				if (!MiscUtils.perm(sender, CKF_PERM + "presets.list", CKF_PERM + "preset.list.others")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				if (args.length > 1) {
					final int PER_PAGE = 10;
					if (MiscUtils.eq(args[1], "list", "l", "view", "v")) {
						List<FireworkPreset> presets;
						boolean isPlayer = sender instanceof Player;
						boolean bypassLimit = !isPlayer || MiscUtils.perm(sender, CKF_PERM + "presets.bypasslimit");
						boolean targeted = false;
						int limit = CarbonKit.getDefConfig().getInt("Fireworks.limit", 10);

						// Build the list of presets to list
						presets = new ArrayList<FireworkPreset>();
						if (sender instanceof Player) {
							if (args.length > 2 && Bukkit.getPlayer(args[2]) != null && MiscUtils.perm(sender, CKF_PERM
									+ "presets.list.others")) {
								targeted = true;
								for (FireworkPreset pre : FireworkPreset.getPresets())
									if (pre.getOwnerID().equals(Bukkit.getPlayer(args[2]).getUniqueId()))
										presets.add(pre);
							} else {
								for (FireworkPreset pre : FireworkPreset.getPresets())
									if (pre.getOwnerID().equals(((Player) sender).getUniqueId()))
										presets.add(pre);
							}
						} else presets = FireworkPreset.getPresets();

						if (presets.isEmpty()) {
							sender.sendMessage(Clr.GRAY + (targeted?Bukkit.getPlayer(args[2]).getName()
									+ " has":(isPlayer ? "You have" : "There are")) + " no presets.");
						} else {
							int page = 1, from = 0, to = presets.size() - 1, pages = 1;
							boolean paginate = presets.size() > PER_PAGE;
							if (paginate) {
								pages = (presets.size() / PER_PAGE) + (presets.size() % PER_PAGE != 0 ? 1 : 0);
								if (!targeted && args.length > 2 && TypeUtils.isInteger(args[2]))
									page = NumUtils.normalizeInt(Integer.parseInt(args[2]), 1, pages);
								else if (targeted && args.length > 3 && TypeUtils.isInteger(args[3]))
									page = NumUtils.normalizeInt(Integer.parseInt(args[3]), 1, pages);
								from = (page - 1) * PER_PAGE;
								to = (PER_PAGE * page) - 1;
							}
							CustomMessage.printHeader(sender, (targeted?Bukkit.getPlayer(args[2]).getName()
									+ "'s":(isPlayer ? "Your " : "All ")) + " Presets "
									+ "(" + presets.size() + (bypassLimit ? "/" + limit : "") + ")"
									+ " pg " + page + " of " + pages + "");
							for (int i = from; i < to+1; i++) {
								if (presets.get(i).getOwnerID() == null) {
									sender.sendMessage(Clr.DARKAQUA + "#" + (i + 1) + " " + Clr.AQUA
											+ presets.get(i).getName() + Clr.GRAY + " (no author)");
								} else {
									sender.sendMessage(Clr.DARKAQUA + "#" + (i + 1) + " " + Clr.AQUA
											+ presets.get(i).getName() + Clr.GRAY + " by "
											+ Bukkit.getOfflinePlayer(presets.get(i).getOwnerID()).getName());
								}
							}
							for (FireworkPreset pre : paginate ? presets.subList(from, to) : presets) {
								if (pre.getOwnerID() == null)
									sender.sendMessage(Clr.AQUA + pre.getName() + Clr.GRAY + " (no author)");
								else
									sender.sendMessage(Clr.AQUA + pre.getName() + Clr.GRAY + " by "
											+ Bukkit.getOfflinePlayer(pre.getOwnerID()).getName());
							}
							if (!bypassLimit && presets.size() < limit)
								sender.sendMessage(Clr.GRAY + (targeted?Bukkit.getPlayer(args[2]).getName():"You")
										+ " can create " + (limit - presets.size()) + " more preset"
										+ ((limit - presets.size() == 1)?"":"s"));
						}
					} else if (MiscUtils.eq(args[1], "listall", "viewall", "la", "va")) {
						if (!MiscUtils.perm(sender, CKF_PERM + "listall")) {
							sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
							return;
						}
						List<FireworkPreset> presets = FireworkPreset.getPresets();
						int page = 1, from = 0, to = presets.size() - 1, pages = 1;
						boolean paginate = presets.size() > PER_PAGE;
						if (paginate) {
							pages = (presets.size() / PER_PAGE) + (presets.size() % PER_PAGE != 0 ? 1 : 0);
							if (args.length > 2 && TypeUtils.isInteger(args[2]))
								page = NumUtils.normalizeInt(Integer.parseInt(args[2]), 1, pages);
							from = (page - 1) * PER_PAGE;
							to = (PER_PAGE * page) - 1;
						}
						CustomMessage.printHeader(sender, "All Presets " + "(" + presets.size() + ")"
								+ "[pg " + page + " of " + pages + "]");
						for (int i = from; i < to+1; i++) {
							if (presets.get(i).getOwnerID() == null) {
								sender.sendMessage(Clr.DARKAQUA + "#" + (i + 1) + " " + Clr.AQUA
										+ presets.get(i).getName() + Clr.GRAY + " (no author)");
							} else {
								sender.sendMessage(Clr.DARKAQUA + "#" + (i + 1) + " " + Clr.AQUA
										+ presets.get(i).getName() + Clr.GRAY + " by "
										+ Bukkit.getOfflinePlayer(presets.get(i).getOwnerID()).getName());
							}
						}
						for (int i = from; i < to+1; i++) {
							if (presets.get(i).getOwnerID() == null) {
								sender.sendMessage(Clr.DARKAQUA + "#" + (i + 1) + " " + Clr.AQUA
										+ presets.get(i).getName() + Clr.GRAY + " (no author)");
							} else {
								sender.sendMessage(Clr.DARKAQUA + "#" + (i + 1) + " " + Clr.AQUA
										+ presets.get(i).getName() + Clr.GRAY + " by "
										+ Bukkit.getOfflinePlayer(presets.get(i).getOwnerID()).getName());
							}
						}
					} else if (MiscUtils.eq(args[1], "delete", "remove", "del", "rem")) {
						if (!MiscUtils.perm(sender, CKF_PERM + "presets.delete", CKF_PERM + "preset.delete.others")) {
							sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
							return;
						}
						if (args.length > 2) {
							FireworkPreset pre = FireworkPreset.getPreset(args[2]);
							if (pre == null) {
								sender.sendMessage(CustomMessage.FW_PRESET_NOT_FOUND.pre());
							} else {
								if (sender instanceof Player) {
									if (!((Player)sender).getUniqueId().equals(pre.getOwnerID())
											&& !MiscUtils.perm(sender, CKF_PERM + "presets.delete.others")) {
										sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
										return;
									}
								}
								if (FireworkPreset.deletePreset(pre.getName())) {
									sender.sendMessage(CustomMessage.FW_DELETED_PRESET.pre());
								} else {
									sender.sendMessage(CustomMessage.FW_ERROR_DELETING_PRESET.pre());
								}
							}
						} else {
							sender.sendMessage(Clr.RED + "Usage: /" + label + " preset " + args[1] + Clr.ITALIC
									+ " [presetName]");
						}
					} else if (MiscUtils.eq(args[1], "add", "combine", "merge", "c", "concat")) {
						if (!MiscUtils.perm(sender, CKF_PERM + "presets.combine", CKF_PERM
								+ "presets.combine.others")) {
							sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
							return;
						}
						if (args.length > 2) {
							FireworkPreset pre = FireworkPreset.getPreset(args[2]);
							if (pre == null) {
								sender.sendMessage(CustomMessage.FW_PRESET_NOT_FOUND.pre());
							} else {
								if (sender instanceof Player) {
									if (!((Player)sender).getUniqueId().equals(pre.getOwnerID())
											&& !MiscUtils.perm(sender, CKF_PERM + "presets.combine.others")) {
										sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
										return;
									}
								}
								if (args.length > 3) {
									if (FireworkUtils.fromArgs(stringifyArgs(args, 3)).size() > 0) {
										List<FireworkEffect> presetEffects = pre.getEffects();
										presetEffects.addAll(FireworkUtils.fromArgs(stringifyArgs(args, 3)));
										FireworkPreset.setPreset(pre.getName(), presetEffects, pre.getOwnerID());
										sender.sendMessage(CustomMessage.FW_COMBINED_EFFECTS.pre());
									} else {
										sender.sendMessage(CustomMessage.FW_INVALID_EFFECTS.pre());
										CustomMessage.remindFireworkParams(sender);
									}
								} else {

								}
							}
						}
					} else {
						if (!MiscUtils.perm(sender, CKF_PERM + "presets.create", CKF_PERM +"presets.modify",
								CKF_PERM + "presets.modify.others")) {
							sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
							return;
						}
						if (args.length > 2) {
							FireworkPreset pre = FireworkPreset.getPreset(args[2]);
							if (pre != null) {
								if (sender instanceof Player) {
									if (!((Player)sender).getUniqueId().equals(pre.getOwnerID())
											&& !MiscUtils.perm(sender, CKF_PERM + "presets.modify.others")) {
										sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
										return;
									}
								}
								if (args.length > 3 && FireworkUtils.fromArgs(stringifyArgs(args, 3)).size() > 0) {
									pre.setEffects(FireworkUtils.fromArgs(stringifyArgs(args, 3)));
									sender.sendMessage(CustomMessage.FW_MODIFIED_PRESET.pre());
								} else {
									sender.sendMessage(CustomMessage.FW_INVALID_EFFECTS.pre());
									CustomMessage.remindFireworkParams(sender);
								}
							} else {
								if (sender instanceof Player && !MiscUtils.perm(sender, CKF_PERM + "presets.create")) {
									sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
									return;
								}
								if (args.length > 3 && FireworkUtils.fromArgs(stringifyArgs(args, 3)).size() > 0) {
									// For shits and giggles, use Herobrine as owner if used from Console
									// Mostly because I set it up to use UUIDs, Console has none.
									UUID owner = UUID.fromString("f84c6a79-0a4e-45e0-879b-cd49ebd4c4e2");
									if (sender instanceof Player)
										owner = ((Player)sender).getUniqueId();
									new FireworkPreset(args[2], FireworkUtils.fromArgs(stringifyArgs(args, 3)), owner);
									sender.sendMessage(CustomMessage.FW_CREATED_PRESET.pre());
								} else {
									sender.sendMessage(CustomMessage.FW_INVALID_EFFECTS.pre());
									CustomMessage.remindFireworkParams(sender);
								}
							}
						}
					}
				} else {
					if (MiscUtils.perm(sender, CKF_PERM + "help.presets"))
						Bukkit.dispatchCommand(sender, "firework help presets");
					else
						sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				}
			} else if (MiscUtils.eq(args[0], "launch")) {
				/* *****************************************************************************************************
				*****                                                                                       LAUNCH *****
				***************************************************************************************************** */
				if (!(sender instanceof Player)) {
					sender.sendMessage(CustomMessage.GEN_NOT_ONLINE + "");
					return;
				}
				if (!MiscUtils.perm(sender, CKF_PERM + "launch.preset", CKF_PERM + "launch.params",
						CKF_PERM + "launch.hand")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				if (args.length > 1) {
					if (MiscUtils.eq(args[1], "hand")) {
						if (!MiscUtils.perm(sender, CKF_PERM + "launch.hand")) {
							sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
							return;
						}
						if (((Player)sender).getItemInHand().getType().equals(Material.FIREWORK)) {
							int power = 1;
							if (args.length > 2 && TypeUtils.isInteger(args[2]))
								power = Integer.parseInt(args[2]);
							power = NumUtils.normalizeInt(power, 1, 6);
							FireworkUtils.launchFirework(((Player)sender).getLocation(), power,
									((FireworkMeta)((Player)sender).getItemInHand().getItemMeta()).getEffects());
						} else {
							sender.sendMessage(CustomMessage.FW_NOT_FIREWORK.pre());
						}
					} else {
						int power = 1;
						boolean powerSet = false;
						if (TypeUtils.isInteger(args[2])) {
							power = NumUtils.normalizeInt(Integer.parseInt(args[2]), 1, 4);
							powerSet = true;
						}
						if (FireworkPreset.getPreset(powerSet ? args[3] : args[2]) != null) {
							if (!MiscUtils.perm(sender, CKF_PERM + "launch.preset")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							FireworkUtils.launchFirework(((Player)sender).getLocation(), power,
									FireworkPreset.getPreset(powerSet?args[3]:args[2]).getEffects());
						} else if (FireworkUtils.fromArgs(stringifyArgs(args, powerSet?3:2)).size() > 0) {
							if (!MiscUtils.perm(sender, CKF_PERM + "launch.params")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							FireworkUtils.launchFirework(((Player)sender).getLocation(), power,
									FireworkUtils.fromArgs(stringifyArgs(args, powerSet?3:2)));
						} else {
							sender.sendMessage(CustomMessage.FW_INVALID_EFFECTS.pre());
						}
					}
				} else {
					if (MiscUtils.perm(sender, CKF_PERM + "help.launch"))
						Bukkit.dispatchCommand(sender, "firework help launch");
					else
						sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				}
			} else if (MiscUtils.eq(args[0], "effect")) {
				/* *****************************************************************************************************
				*****                                                                                       EFFECT *****
				***************************************************************************************************** */
				if (!(sender instanceof Player)) {
					sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.pre());
					return;
				}
				if (!MiscUtils.perm(sender, CKF_PERM + "effect.preset", CKF_PERM + "effect.params",
						CKF_PERM + "effect.hand")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				if (args.length > 1) {
					if (MiscUtils.eq(args[1], "hand")) {
						if (!MiscUtils.perm(sender, CKF_PERM + "effect.hand")) {
							sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
							return;
						}
						if (((Player)sender).getItemInHand().getType().equals(Material.FIREWORK)) {
							FireworkUtils.playFirework(((Player)sender).getLocation(),
									((FireworkMeta)((Player)sender).getItemInHand().getItemMeta()).getEffects());
						} else {
							sender.sendMessage(CustomMessage.FW_NOT_FIREWORK.pre());
						}
					} else {
						if (FireworkPreset.getPreset(args[2]) != null) {
							if (!MiscUtils.perm(sender, CKF_PERM + "effect.preset")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							FireworkUtils.playFirework(((Player)sender).getLocation(),
									FireworkPreset.getPreset(args[2]).getEffects());
						} else if (FireworkUtils.fromArgs(stringifyArgs(args, 2)).size() > 0) {
							if (!MiscUtils.perm(sender, CKF_PERM + "effect.params")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							FireworkUtils.playFirework(((Player)sender).getLocation(),
									FireworkUtils.fromArgs(stringifyArgs(args, 2)));
						} else {
							sender.sendMessage(CustomMessage.FW_INVALID_EFFECTS.pre());
						}
					}
				} else {
					if (MiscUtils.perm(sender, CKF_PERM + "help.effect"))
						Bukkit.dispatchCommand(sender, "firework help effect");
					else
						sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				}
			} else if (MiscUtils.eq(args[0], "arrow")) {
				/* *****************************************************************************************************
				*****                                                                                        ARROW *****
				***************************************************************************************************** */
				if (!MiscUtils.perm(sender, CKF_PERM + "arrow.toggle", CKF_PERM + "arrow.preset",
						CKF_PERM + "arrow.hand",
						CKF_PERM + "arrow.others", CKF_PERM + "arrow.preset.others")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				if (args.length > 1) {
					if (TypeUtils.isBoolean(args[1])) {
						boolean b = TypeUtils.toBoolean(args[1]);
						if (args.length > 2) {
							if (MiscUtils.perm(sender, CKF_PERM + "arrow.toggle.others")) {
								if (Bukkit.getPlayer(args[2]) != null) {
									CarbonFireworksModule.setArrowsEnabled(Bukkit.getPlayer(args[2]), b);
									if (b) {
										Bukkit.getPlayer(args[2]).sendMessage(CustomMessage.FW_ARROW_ON_SELF.pre());
										sender.sendMessage(CustomMessage.FW_ARROW_ON_OTHER.pre());
									} else {
										Bukkit.getPlayer(args[2]).sendMessage(CustomMessage.FW_ARROW_OFF_SELF.pre());
										sender.sendMessage(CustomMessage.FW_ARROW_OFF_OTHER.pre());
									}
								} else {
									sender.sendMessage(CustomMessage.GEN_PLAYER_NOT_FOUND.pre());
								}
							}
						} else {
							if (MiscUtils.perm(sender, CKF_PERM + "arrow.toggle")) {
								CarbonFireworksModule.setArrowsEnabled((OfflinePlayer) sender, b);
								if (b)
									sender.sendMessage(CustomMessage.FW_ARROW_ON_SELF.pre());
								else
									sender.sendMessage(CustomMessage.FW_ARROW_OFF_SELF.pre());
							}
						}
					} else if (MiscUtils.eq(args[1], "toggle", "t")) {
						if (args.length > 2) {
							if (!MiscUtils.perm(sender, CKF_PERM + "arrow.toggle.others")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							if (Bukkit.getPlayer(args[2]) != null) {
								CarbonFireworksModule.toggleArrowsEnabled(Bukkit.getPlayer(args[2]));
								if (CarbonFireworksModule.hasArrowsEnabled(Bukkit.getPlayer(args[2]))) {
									Bukkit.getPlayer(args[2]).sendMessage(CustomMessage.FW_ARROW_ON_SELF.pre());
									sender.sendMessage(CustomMessage.FW_ARROW_ON_OTHER.pre());
								} else {
									Bukkit.getPlayer(args[2]).sendMessage(CustomMessage.FW_ARROW_OFF_SELF.pre());
									sender.sendMessage(CustomMessage.FW_ARROW_OFF_OTHER.pre());
								}
							} else {
								sender.sendMessage(CustomMessage.GEN_PLAYER_NOT_FOUND.pre());
							}
						} else {
							if (MiscUtils.perm(sender, CKF_PERM + "arrow.toggle")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							CarbonFireworksModule.toggleArrowsEnabled((OfflinePlayer) sender);
							if (CarbonFireworksModule.hasArrowsEnabled((OfflinePlayer) sender))
								sender.sendMessage(CustomMessage.FW_ARROW_ON_SELF.pre());
							else
								sender.sendMessage(CustomMessage.FW_ARROW_OFF_SELF.pre());
						}
					} else if (MiscUtils.eq(args[1], "preset", "pre", "p", "effect", "fx", "e")) {
						if (args.length > 2) {
							List<FireworkEffect> fx = FireworkUtils.fromArgs(stringifyArgs(args, 2));
							if (fx == null || fx.size() < 1) {
								sender.sendMessage(CustomMessage.FW_INVALID_EFFECTS.pre());
								return;
							}
							Player pl = null;
							if (args[args.length-1].matches("[a-zA-Z0-9_]{3,16}"))
								pl = Bukkit.getPlayer(args[args.length-1]);
							if (pl != null) {
								HashMap<String, String> rep = new HashMap<String, String>();
								rep.put("{PLAYER}", pl.getName());
								if (!MiscUtils.perm(sender, CKF_PERM + "arrow.preset.others")) {
									sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
									return;
								}
								CarbonFireworksModule.setArrowEffects(pl, fx);
								pl.sendMessage(CustomMessage.FW_EFFECT_SET_OTHER.pre());
								sender.sendMessage(MiscUtils.massReplace(CustomMessage.FW_EFFECT_SET_SELF.pre(), rep));
							} else {
								if (!MiscUtils.perm(sender, CKF_PERM + "arrow.preset")) {
									sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
									return;
								}
								CarbonFireworksModule.setArrowEffects((OfflinePlayer) sender, fx);
								sender.sendMessage(CustomMessage.FW_EFFECT_SET_SELF.pre());
							}
						} else {
							sender.sendMessage(Clr.RED + "Usage: /fw a pre <preset> [user]");
							CustomMessage.remindFireworkParams(sender);
						}
					} else {
						sender.sendMessage(Clr.RED + "That sub-command was not recognized");
					}
				} else {
					if (MiscUtils.perm(sender, CKF_PERM + "help.arrow"))
						Bukkit.dispatchCommand(sender, "firework help arrow");
					else
						sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				}
			} else if (MiscUtils.eq(args[0], "snowball")) {
				/* *****************************************************************************************************
				*****                                                                                      SNOWBALL *****
				***************************************************************************************************** */
				if (!MiscUtils.perm(sender, CKF_PERM + "snowball.toggle", CKF_PERM + "snowball.preset",
						CKF_PERM + "snowball.hand", CKF_PERM + "snowball.others")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				if (args.length > 1) {
					if (TypeUtils.isBoolean(args[1])) {
						boolean b = TypeUtils.toBoolean(args[1]);
						if (args.length > 2) {
							if (MiscUtils.perm(sender, CKF_PERM + "snowball.toggle.others")) {
								if (Bukkit.getPlayer(args[2]) != null) {
									CarbonFireworksModule.setSnowballEnabled(Bukkit.getPlayer(args[2]), b);
									if (b) {
										Bukkit.getPlayer(args[2]).sendMessage(CustomMessage.FW_SNOWBALL_ON_SELF.pre());
										sender.sendMessage(CustomMessage.FW_SNOWBALL_ON_OTHER.pre());
									} else {
										Bukkit.getPlayer(args[2]).sendMessage(CustomMessage.FW_SNOWBALL_OFF_SELF.pre());
										sender.sendMessage(CustomMessage.FW_SNOWBALL_OFF_OTHER.pre());
									}
								} else {
									sender.sendMessage(CustomMessage.GEN_PLAYER_NOT_FOUND.pre());
								}
							}
						} else {
							if (MiscUtils.perm(sender, CKF_PERM + "snowball.toggle")) {
								CarbonFireworksModule.setSnowballEnabled((OfflinePlayer) sender, b);
								if (b)
									sender.sendMessage(CustomMessage.FW_SNOWBALL_ON_SELF.pre());
								else
									sender.sendMessage(CustomMessage.FW_SNOWBALL_OFF_SELF.pre());
							}
						}
					} else if (MiscUtils.eq(args[1], "toggle", "t")) {
						if (args.length > 2) {
							if (!MiscUtils.perm(sender, CKF_PERM + "snowball.toggle.others")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							if (Bukkit.getPlayer(args[2]) != null) {
								CarbonFireworksModule.toggleArrowsEnabled(Bukkit.getPlayer(args[2]));
								if (CarbonFireworksModule.hasArrowsEnabled(Bukkit.getPlayer(args[2]))) {
									Bukkit.getPlayer(args[2]).sendMessage(CustomMessage.FW_SNOWBALL_ON_SELF.pre());
									sender.sendMessage(CustomMessage.FW_SNOWBALL_ON_OTHER.pre());
								} else {
									Bukkit.getPlayer(args[2]).sendMessage(CustomMessage.FW_SNOWBALL_OFF_SELF.pre());
									sender.sendMessage(CustomMessage.FW_SNOWBALL_OFF_OTHER.pre());
								}
							} else {
								sender.sendMessage(CustomMessage.GEN_PLAYER_NOT_FOUND.pre());
							}
						} else {
							if (MiscUtils.perm(sender, CKF_PERM + "snowball.toggle")) {
								sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
								return;
							}
							CarbonFireworksModule.toggleArrowsEnabled((OfflinePlayer) sender);
							if (CarbonFireworksModule.hasArrowsEnabled((OfflinePlayer) sender))
								sender.sendMessage(CustomMessage.FW_SNOWBALL_ON_SELF.pre());
							else
								sender.sendMessage(CustomMessage.FW_SNOWBALL_OFF_SELF.pre());
						}
					} else if (MiscUtils.eq(args[1], "preset", "pre", "p", "effect", "fx", "e")) {
						if (args.length > 2) {
							List<FireworkEffect> fx = FireworkUtils.fromArgs(stringifyArgs(args, 2));
							if (fx == null || fx.size() < 1) {
								sender.sendMessage(CustomMessage.FW_INVALID_EFFECTS.pre());
								return;
							}
							Player pl = null;
							if (args[args.length-1].matches("[a-zA-Z0-9_]{3,16}"))
								pl = Bukkit.getPlayer(args[args.length-1]);
							if (pl != null) {
								HashMap<String, String> rep = new HashMap<String, String>();
								rep.put("{PLAYER}", pl.getName());
								if (!MiscUtils.perm(sender, CKF_PERM + "snowball.preset.others")) {
									sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
									return;
								}
								CarbonFireworksModule.setSnowballEffects(pl, fx);
								pl.sendMessage(CustomMessage.FW_EFFECT_SET_OTHER.pre());
								sender.sendMessage(MiscUtils.massReplace(CustomMessage.FW_EFFECT_SET_SELF.pre(), rep));
							} else {
								if (!MiscUtils.perm(sender, CKF_PERM + "snowball.preset")) {
									sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
									return;
								}
								CarbonFireworksModule.setSnowballEffects((OfflinePlayer) sender, fx);
								sender.sendMessage(CustomMessage.FW_EFFECT_SET_SELF.pre());
							}
						} else {
							sender.sendMessage(Clr.RED + "Usage: /fw s pre <preset> [user]");
							CustomMessage.remindFireworkParams(sender);
						}
					} else {
						sender.sendMessage(Clr.RED + "That sub-command was not recognized");
					}
				} else {
					if (MiscUtils.perm(sender, CKF_PERM + "help.snowball"))
						Bukkit.dispatchCommand(sender, "firework help snowball");
					else
						sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				}
			} else if (MiscUtils.eq(args[0], "spawn")) {
				/* *****************************************************************************************************
				*****                                                                                        SPAWN *****
				***************************************************************************************************** */
				if (!MiscUtils.perm(sender, CKF_PERM + "spawn.preset", CKF_PERM + "spawn.params")) {
					sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
					return;
				}
				if (args.length > 1) {
					List<FireworkEffect> fx = FireworkUtils.fromArgs(stringifyArgs(args, 1));
					Player pl = null;
					if (args[args.length-1].matches("[a-zA-Z0-9_]{3,16}"))
						pl = Bukkit.getPlayer(args[args.length-1]);
					if (fx == null || fx.size() < 1) {
						sender.sendMessage(CustomMessage.FW_INVALID_EFFECTS.pre());
					} else {
						ItemStack is = new ItemStack(Material.FIREWORK, 64);
						FireworkMeta fm = (FireworkMeta)is.getItemMeta();
						fm.addEffects(fx);
						if (pl == null) {
							pl = (Player) sender;
							if (pl.getInventory().firstEmpty() > -1) {
								pl.getInventory().addItem(is);
							} else {
								pl.sendMessage(CustomMessage.FW_INVENTORY_FULL_SELF.pre());
							}
						} else {
							if (pl.getInventory().firstEmpty() > -1) {
								pl.getInventory().addItem(is);
							} else {
								HashMap<String, String> rep = new HashMap<String, String>();
								rep.put("{PLAYER}", pl.getName());
								sender.sendMessage(MiscUtils.massReplace(CustomMessage.FW_INVENTORY_FULL_OTHER.pre(), rep));
								pl.sendMessage(CustomMessage.FW_INVENTORY_FULL_SELF.pre());
							}
						}
					}
				} else {
					if (MiscUtils.perm(sender, CKF_PERM + "help.spawn"))
						Bukkit.dispatchCommand(sender, "firework help spawn");
					else
						sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				}
			}
		}
	}
}
