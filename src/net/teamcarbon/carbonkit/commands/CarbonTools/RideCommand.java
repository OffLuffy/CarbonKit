package net.teamcarbon.carbonkit.commands.CarbonTools;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;

@SuppressWarnings("UnusedDeclaration")
public class RideCommand extends ModuleCmd {

	public RideCommand(Module module) { super(module, "ride"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		Player pl = (Player) sender;
		if (!mod.perm(sender, "ride", "ride.players")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		if (args.length == 0) {
			Entity ent = MiscUtils.getTarget((Player) sender);
			if (ent != null) {
				if (ent instanceof Player) {
					if (!mod.perm(sender, "ride.players")) sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
				} else {
					boolean allow = true;
					if (MiscUtils.checkPlugin("GriefPrevention", true)) {
						me.ryanhamshire.GriefPrevention.DataStore ds = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore;
						me.ryanhamshire.GriefPrevention.Claim claim = ds.getClaimAt(ent.getLocation(), false, null);
						me.ryanhamshire.GriefPrevention.PlayerData pd = ds.getPlayerData(pl.getUniqueId());
						allow = claim == null || claim.ownerID.equals(pl.getUniqueId()) || (pd != null && pd.ignoreClaims);
						if (!allow) {
							String msg = claim.allowAccess(pl);
							if (msg == null) {
								allow = true;
							} else {
								pl.sendMessage(msg);
								return;
							}
						}
					}
					if (allow) ent.setPassenger((Entity) sender);
				}
			}
		} else {
			if (!mod.perm(sender, "ride.players")) {
				sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
				return;
			}
			OfflinePlayer target = MiscUtils.getPlayer(args[0], false);
			if (target != null && target.isOnline()) {
				((Player) target).setPassenger((Entity)sender);
			} else {
				sender.sendMessage(Clr.RED + "That player could not be found!");
			}
		}
	}
}
