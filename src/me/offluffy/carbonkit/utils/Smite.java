package me.offluffy.carbonkit.utils;

import java.util.ArrayList;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.modules.ModuleGoldenSmite;
import me.offluffy.carbonkit.modules.ModuleGoldenSmite.EntType;
import me.offluffy.carbonkit.utils.Messages.Message;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class Smite {
	public enum SmiteType {
		AXE, CMD, ARROW;
	}
	private static final int OVER_NINE_THOUSAND = 9001;
	private static ModuleGoldenSmite sMod = (ModuleGoldenSmite)Module.getModule("GoldenSmite");
	private static ConfigurationSection kill = CarbonKit.data.getConfigurationSection(sMod.getName() + ".enabled-kills");
	
	public static void createSmite(Player pl, Location l, SmiteType smiteMethod) {
		Location effectLocation;
		if (smiteMethod.equals(SmiteType.ARROW))
			effectLocation = new Location(l.getWorld(), l.getX(), l.getY(), l.getZ());
		else
			effectLocation = new Location(l.getWorld(), l.getX(), Math.floor(l.getBlockY())+1.5, l.getZ());
		ModuleGoldenSmite.killed = getTargets(pl, l, smiteMethod);
		createEffects(effectLocation);
		if (!ModuleGoldenSmite.killed.isEmpty())
			for (Entity x : ModuleGoldenSmite.killed)
				if (x instanceof LivingEntity) {
					((LivingEntity)x).damage(OVER_NINE_THOUSAND);
				} else { x.remove(); }
		ModuleGoldenSmite.clearKills();
	}
	
	public static void smitePlayer(CommandSender p, Player vic) {
		if (!(p instanceof Player)) {
			Messages.send(p, Message.NOT_ONLINE);
			return;
		}
		smitePlayer((Player)p, vic);
	}
	
	public static void smitePlayer(Player pl, Player vic) {
		if (CarbonKit.perms.has(vic, "gsmite.immune.cmd"))
			pl.sendMessage(ChatColor.RED + vic.getName() + " is immune!");
		else {
			Location effectLocation = vic.getLocation();
			createEffects(effectLocation);
			vic.damage(OVER_NINE_THOUSAND);
			pl.sendMessage(Messages.Clr.NORM + vic.getName() + " has been smited");
		}
	}
	
	private static ArrayList<Entity> getTargets(Player pl, Location l, SmiteType smiteMethod) {
		ArrayList<Entity> entList = new ArrayList<Entity>();
		if (kill.contains(pl.getName())) {
			List<String> dest = kill.getStringList(pl.getName());
			boolean host, neut, pass, tame, play, drop;
			host = dest.contains(EntType.HOSTILE.getName());
			neut = dest.contains(EntType.NEUTRAL.getName());
			pass = dest.contains(EntType.PASSIVE.getName());
			tame = dest.contains(EntType.TAMED.getName());
			play = dest.contains(EntType.PLAYER.getName());
			drop = dest.contains(EntType.DROP.getName());
			if (host || neut || pass || tame || play || drop) {
				int rad = CarbonKit.config.getInt(sMod.getName() + ".radius", 6);
				int rng = CarbonKit.config.getInt(sMod.getName() + ".range", 100) + rad;
				ArrayList<Entity> ents = new ArrayList<Entity>();
				for (Entity e : pl.getNearbyEntities(rng, rng, rng)) {
					if ((EntityHelper.isHostile(e.getType()) && host) || (EntityHelper.isNeutral(e.getType()) && neut) ||(EntityHelper.isPassive(e.getType()) && pass))
						ents.add(e);
					if (e instanceof LivingEntity && EntityHelper.isTamed((LivingEntity)e) && !tame && ents.contains(e))
						ents.remove(e);
					if (EntityHelper.isDrop(e.getType()) && drop)
						ents.add(e);
					if (EntityHelper.isPlayer(e.getType()) && play) {
						Player epl = (Player)e;
						if (!Lib.perm(epl, "gsmite.immune." + smiteMethod.toString().toLowerCase()))
							ents.add(e);
						else
							ents.add(e);
					}
				}
				Block b = l.getBlock();
				int bx = b.getX(),
					by = b.getY(),
					bz = b.getZ();
				Double	ex, ey, ez;
				for (Entity e : ents) {
					Location el = e.getLocation();
					ex = el.getX();
					ey = el.getY();
					ez = el.getZ();
					boolean inRange = ((bx-rad <= ex && bx+rad >= ex) && (by-rad <= ey && by+rad >= ey) && (bz-rad <= ez && bz+rad >= ez));
					if (inRange)
						entList.add(e);
				}
			}
		}
		return entList;
	}
	
	private static void createEffects(Location l) {
		if (CarbonKit.config.getBoolean(sMod.getName() + ".explosionEffect", true))
			l.getWorld().createExplosion(l, 0);
		if (CarbonKit.config.getBoolean(sMod.getName() + ".lightningEffect", true))
			l.getWorld().strikeLightningEffect(l);
	}
}
