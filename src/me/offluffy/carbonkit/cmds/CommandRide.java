package me.offluffy.carbonkit.cmds;

import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Messages.Message;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.ModuleCmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class CommandRide extends ModuleCmd {
	public CommandRide(Module module) {
		super(module, "ride","dismount");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			Messages.send(sender, Message.NOT_ONLINE);
			return true;
		}
		if (label.equalsIgnoreCase("ride")) {
			if (!canExec(sender, false, "")) {
				Messages.send(sender, Message.NO_PERM);
				return true;
			}
			if (args.length == 0) {
				Entity ent = getTarget((Player)sender);
				if (ent instanceof Player)
					if (!Lib.perm(sender, "carbonkit.ride.players")) {
						Messages.send(sender, Message.NO_PERM);
						return true;
					}
				if (ent != null) {
					ent.setPassenger((Entity)sender);
					return true;
				}
			} else {
				if (!Lib.perm(sender, "carbonkit.ride.players")) {
					Messages.send(sender, Message.NO_PERM);
					return true;
				}
				if (CarbonKit.inst.getServer().getPlayer(args[0]) != null) {
					Entity ent = (Entity)CarbonKit.inst.getServer().getPlayer(args[0]);
					ent.setPassenger((Entity)sender);
					return true;
				} else {
					sender.sendMessage(Messages.Clr.ERR + "That player could not be found!");
					return true;
				}
			}
		} else if (label.equalsIgnoreCase("dismount")) {
			Entity v = (Entity)sender;
			if (!canExec(sender, false, "")) {
				Messages.send(sender, Message.NO_PERM);
				return true;
			}
			if (v.getPassenger() != null)
				v.eject();
			if (v.isInsideVehicle())
				v.getVehicle().eject();
		}
		return true;
	}

	@Override
	protected HashMap<String, List<String>> populatePerms() {
		HashMap<String, List<String>> perms = new HashMap<String, List<String>>();
		perms.put("", toList("carbonkit.ride"));
		return perms;
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
