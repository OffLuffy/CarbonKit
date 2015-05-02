package net.teamcarbon.carbonkit.commands.Misc;

import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Messages.Clr;
import net.teamcarbon.carbonlib.MiscUtils;

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
		if (!MiscUtils.perm(sender, "carbonkit.misc.ride", "carbonkit.misc.ride.players")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		if (args.length == 0) {
			Entity ent = getTarget((Player) sender);
			if (ent != null) {
				if (ent instanceof Player) {
					if (!MiscUtils.perm(sender, "carbonkit.misc.ride.players")) sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
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
			if (!MiscUtils.perm(sender, "carbonkit.misc.ride.players")) {
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

	public static Entity getTarget(final Player player) {
		assert player != null;
		Entity target = null;
		double targetDistanceSquared = 0;
		final double radiusSquared = 1;
		final Vector l = player.getEyeLocation().toVector(), n = player.getLocation().getDirection().normalize();
		final double cos45 = Math.cos(Math.PI / 4);
		for (final LivingEntity other : player.getWorld().getEntitiesByClass(LivingEntity.class)) {
			if (other == player)
				continue;
			if (target == null || targetDistanceSquared > other.getLocation().distanceSquared(player.getLocation())) {
				final Vector t = other.getLocation().add(0, 1, 0).toVector().subtract(l);
				if (n.clone().crossProduct(t).lengthSquared() < radiusSquared && t.normalize().dot(n) >= cos45) {
					target = other;
					targetDistanceSquared = target.getLocation().distanceSquared(player.getLocation());
				}
			}
		}
		return target;
	}
}
