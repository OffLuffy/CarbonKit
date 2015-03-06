package net.teamcarbon.carbonkit.utils;

import java.util.ArrayList;

import net.teamcarbon.carbonkit.modules.GoldenSmiteModule;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.EntHelper.EntityGroup;
import net.teamcarbon.carbonlib.LocUtils;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.*;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonlib.Messages.Clr;

@SuppressWarnings("UnusedDeclaration")
public class Smite {
	public enum SmiteType {
		AXE, CMD, PROJECTILE;
		public String lname() { return name().toLowerCase(); }
	}
	private static final int OVER_NINE_THOUSAND = 9001;
	public static void createSmite(Player pl, Location l, SmiteType smiteMethod) {
		Location effectLocation = l.clone();
		if (!smiteMethod.equals(SmiteType.PROJECTILE)) {
			effectLocation.setX(effectLocation.getBlockX() + 0.5);
			effectLocation.setY(effectLocation.getBlockY()+1.5);
			effectLocation.setZ(effectLocation.getBlockZ()+0.5);
		}
		GoldenSmiteModule.killed = getTargets(pl, l, smiteMethod);
		createEffects(effectLocation);
		if (!GoldenSmiteModule.killed.isEmpty())
			for (Entity x : GoldenSmiteModule.killed)
				if (x instanceof LivingEntity) {
					((LivingEntity)x).damage(OVER_NINE_THOUSAND, pl);
					final LivingEntity fx = (LivingEntity)x;
					Bukkit.getScheduler().runTaskLater(CarbonKit.inst, new Runnable() {
						public void run() {
							if (!(fx instanceof Player) && !fx.isDead())
								fx.remove();
						}
					}, 1L);
				} else {
					x.remove();
				}
		GoldenSmiteModule.killed.clear();
	}
	
	public static void smitePlayer(CommandSender p, Player vic) {
		if (!(p instanceof Player)) {
			p.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		smitePlayer((Player)p, vic);
	}
	
	public static void smitePlayer(Player pl, Player vic) {
		if (MiscUtils.perm(vic, "gsmite.immune.cmd"))
			pl.sendMessage(Clr.RED + vic.getName() + " is immune!");
		else {
			Location effectLocation = vic.getLocation();
			createEffects(effectLocation);
			vic.damage(OVER_NINE_THOUSAND);
			pl.sendMessage(Clr.AQUA + vic.getName() + " has been smited");
		}
	}
	
	private static ArrayList<Entity> getTargets(Player pl, Location l, SmiteType smiteMethod) {
		GoldenSmiteModule mod = (GoldenSmiteModule)Module.getModule("GoldenSmite");
		ArrayList<Entity> entList = new ArrayList<Entity>();
		boolean host = GoldenSmiteModule.isGroupEnabled(pl, EntityGroup.HOSTILE), neut = GoldenSmiteModule.isGroupEnabled(pl, EntityGroup.NEUTRAL),
				pass = GoldenSmiteModule.isGroupEnabled(pl, EntityGroup.PASSIVE), tame = GoldenSmiteModule.isGroupEnabled(pl, EntityGroup.TAMED),
				play = GoldenSmiteModule.isGroupEnabled(pl, EntityGroup.PLAYER), drop = GoldenSmiteModule.isGroupEnabled(pl, EntityGroup.DROP);
		if (host || neut || pass || tame || play || drop) {
			if (mod == null) CarbonKit.log.warn("Module is null");
			if (mod.getConfig() == null) CarbonKit.log.warn("Module config is null");
			int rad = mod.getConfig().getInt("radius", 6), rng = mod.getConfig().getInt("range", 100) + rad;
			ArrayList<Entity> ents = new ArrayList<Entity>();
			for (Entity e : pl.getNearbyEntities(rng, rng, rng)) {
				if ((EntHelper.isHostile(e.getType()) && host) || (EntHelper.isNeutral(e.getType()) && neut) ||(EntHelper.isPassive(e.getType()) && pass))
					ents.add(e);
				if (e instanceof LivingEntity && EntHelper.isTamed((LivingEntity)e) && !tame && ents.contains(e))
					ents.remove(e);
				if (EntHelper.isDrop(e.getType()) && drop)
					ents.add(e);
				if (EntHelper.isPlayer(e.getType()) && play) {
					Player epl = (Player)e;
					if (!MiscUtils.perm(epl, "carbonkit.goldensmite.immune." + smiteMethod.lname()))
						ents.add(e);
					else
						ents.add(e);
				}
			}
			for (Entity e : ents)
				if (LocUtils.distance(l.getBlock().getLocation(), e.getLocation()) <= rad)
					entList.add(e);
		}
		return entList;
	}
	
	private static void createEffects(Location l) {
		GoldenSmiteModule mod = (GoldenSmiteModule)Module.getModule("GoldenSmite");
		if (mod.getConfig().getBoolean("explosionEffect", false)) l.getWorld().createExplosion(l, 0);
		if (mod.getConfig().getBoolean("lightningEffect", false)) l.getWorld().strikeLightningEffect(l);
		if (mod.getConfig().getBoolean("fireworkEffect", true)) {
			Builder feb = FireworkEffect.builder();
			FireworkUtils.playFirework(l, feb.with(Type.BALL)
					.withColor(FireworkUtils.colorsFromHex("FFD700", "FF8C00", "FFDF00", "FFA500"))
					.withFade(FireworkUtils.colorsFromHex("8B4513", "A0522D", "996515", "DAA520"))
					.build());
		}
	}
}
