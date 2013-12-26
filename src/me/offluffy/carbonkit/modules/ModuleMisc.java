package me.offluffy.carbonkit.modules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.cmds.CommandEntCount;
import me.offluffy.carbonkit.cmds.CommandFakeJoin;
import me.offluffy.carbonkit.cmds.CommandFakeQuit;
import me.offluffy.carbonkit.cmds.CommandFreeze;
import me.offluffy.carbonkit.cmds.CommandRide;
import me.offluffy.carbonkit.cmds.CommandSlap;
import me.offluffy.carbonkit.utils.DuplicateModuleException;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Module;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Art;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleMoveEvent;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

public class ModuleMisc extends Module {
	public List<String> freezeList = new ArrayList<String>();
	public HashMap<String, String> addressMap = new HashMap<String, String>();
	//private boolean antiLoop = false;
	public ModuleMisc() throws DuplicateModuleException {
		super("Misc", "miscmodule");
	}
	
	private enum RadarType {
		PLAYER('2', "player"), ANIMAL('3', "animal"), MOB('4', "mob"),
		SLIME('5', "slime"), SQUID('6', "squid"), LIVING('7', "living");
		char code;
		String perm;
		RadarType(char c, String p) {
			code = c;
			perm = p;
		}
		public String getCode() {
			return "\u00A7" + code;
		}
		public String getPerm() {
			return "carbonkit.radar." + perm;
		}
	}

	@Override
	public void initModule() {
		addCmd(new CommandFreeze(this));
		addCmd(new CommandSlap(this));
		addCmd(new CommandRide(this));
		addCmd(new CommandFakeJoin(this));
		addCmd(new CommandFakeQuit(this));
		addCmd(new CommandEntCount(this));
	}

	@Override
	public void disableModule() {}

	@Override
	protected boolean hasListeners() { return true; }

	@Override
	public boolean hasDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler
	public void loginEvent(PlayerLoginEvent e) {
		addressMap.put(e.getPlayer().getName(), e.getAddress().toString().replace("/",""));
	}
	
	@EventHandler
	public void joinEvent(PlayerJoinEvent e) {
		if (isEnabled()) {
			Player p = e.getPlayer();
			Location l = p.getLocation();
			String statuses = "";
			
			// Minimap Radar
			String seqStart = "\u00A70\u00A70";
			String seqEnd = "\u00A7e\u00A7f";
			String radSeq = "";
			for (RadarType rt : RadarType.values())
				if (Lib.perm(p, rt.getPerm()))
					radSeq += rt.getCode();
			if (radSeq.length() > 0)
				p.sendMessage(seqStart + radSeq + seqEnd);
			
			// Custom join message
			boolean silent = Lib.perm(p, "carbonkit.silentjoin");
			if (silent)
				p.sendMessage(Messages.Clr.PRE + "[Silent] " + Messages.Clr.NORM + "You have joined silently.");
			String jm = translateVars(CarbonKit.config.getString(getName() + ".join-message"), p.getName(), l);
			String jme = translateVars(CarbonKit.config.getString(getName() + ".join-message-extended"), p.getName(), l);
			jm = translateVars(jm, p.getName(), l);
			jme = translateVars(jme, p.getName(), l);
			for (Player opl : CarbonKit.inst.getServer().getOnlinePlayers()) {
				boolean ext = Lib.perm(opl, "carbonkit.joinmsg.extended");
				if (!opl.equals(p))
					if (ext && CarbonKit.pm.isPluginEnabled("Essentials")) {
						Essentials ess = (Essentials)CarbonKit.pm.getPlugin("Essentials");
						User u = ess.getUserMap().getUser(p.getName());
						if (!statuses.contains("[J]") && u.isJailed())
							statuses += "[J]";
						if (!statuses.contains("[M]") && u.isMuted())
							statuses += "[M]";
					}
					if (!statuses.contains("[S]") && silent && ext)
						statuses += "[S]";
					if (statuses.length() > 0)
						statuses += " ";
					if (!silent || Lib.perm(opl, "carbonkit.silentjoin.notify")) {
						if (ext)
							opl.sendMessage(Messages.Clr.NOTE + statuses + ChatColor.RESET + jme);
						else
							opl.sendMessage(jm);
					}
			}
			e.setJoinMessage(null);
		}
	}

	@EventHandler
	public void quitEvent(PlayerQuitEvent e) {
		if (isEnabled()) {
			Player p = e.getPlayer();
			Location l = p.getLocation();
			
			boolean silent = Lib.perm(p, "carbonkit.silentquit");
			if (silent)
				p.sendMessage(Messages.Clr.PRE + "[Silent] " + Messages.Clr.NORM + "You have quit silently.");
			
			String qm = translateVars(CarbonKit.config.getString(getName() + ".quit-message"), p.getName(), l);
			String qme = translateVars(CarbonKit.config.getString(getName() + ".quit-message-extended"), p.getName(), l);
			String statuses = "";
			
			for (Player opl : CarbonKit.inst.getServer().getOnlinePlayers()) {
				boolean ext = Lib.perm(opl, "carbonkit.quitmsg.extended");
				if (!opl.equals(p))
					if (ext && CarbonKit.pm.isPluginEnabled("Essentials")) {
						Essentials ess = (Essentials)CarbonKit.pm.getPlugin("Essentials");
						User u = ess.getUserMap().getUser(p.getName());
						if (!statuses.contains("[J]") && u.isJailed())
							statuses += "[J]";
						if (!statuses.contains("[M]") && u.isMuted())
							statuses += "[M]";
					}
					if (!statuses.contains("[S]") && silent && ext)
						statuses += "[S]";
					if (!statuses.contains(" ") && statuses.length() > 0)
						statuses += " ";
					if (!silent || Lib.perm(opl, "carbonkit.silentquit.notify")) {
						if (ext)
							opl.sendMessage(Messages.Clr.NOTE + statuses + ChatColor.RESET + qme);
						else
							opl.sendMessage(qm);
					}
			}
				
			if (addressMap.containsKey(p.getName()))
				addressMap.remove(p.getName());
			e.setQuitMessage(null);
		}
	}

	@SuppressWarnings("deprecation")
	@EventHandler(ignoreCancelled=true,priority=EventPriority.LOWEST)
	public void entityEvent(PlayerInteractEntityEvent e) {
		if (isEnabled()) {
			if (e.getRightClicked() instanceof Painting && Lib.perm(e.getPlayer(),"carbonkit.artcycle")) {
				Painting p = (Painting)e.getRightClicked();
				boolean allow = true;
				if (CarbonKit.pm.isPluginEnabled("GriefPrevention")) {
					DataStore ds = GriefPrevention.instance.dataStore;
					Claim claim = ds.getClaimAt(p.getLocation(), false, null);
					if (claim != null) {
						List<String> builders = new ArrayList<String>();
						List<String> managers = new ArrayList<String>();
						claim.getPermissions(builders, null, null, managers);
						String pName = e.getPlayer().getName().toLowerCase();
						if (!e.getPlayer().isOp())
							if (!claim.getOwnerName().equals(pName) && !builders.contains(pName) && !managers.contains(pName))
								allow = false;
					}
				}
				if (allow) {
					boolean art = false;
					int av = Art.values().length-1,
							it = 0,
							cur = p.getArt().getId(),
							nid = cur+1;
					while (!art && !(it >= av)) {
						if (nid > av)
							nid = 0;
						art = p.setArt(Art.getById(nid));
						if (!art)
							nid++;
						it++;
					}
				}
			}
		}
	}

	@EventHandler
	public void moveEvent(PlayerMoveEvent e) {
		if (isEnabled() && freezeList.contains(e.getPlayer().getName()))
			e.getPlayer().teleport(e.getFrom());
	}
	@EventHandler
	public void vehicleMoveEvent(VehicleMoveEvent e) {
		if (isEnabled() && e.getVehicle() instanceof Player)
			if (freezeList.contains(((Player)e.getVehicle()).getName()))
				e.getVehicle().teleport(e.getFrom());
	}
	@EventHandler
	public void breakEvent(BlockBreakEvent e) {
		if (isEnabled() && freezeList.contains(e.getPlayer().getName()))
			e.setCancelled(true);
	}
	@EventHandler
	public void placeEvent(BlockPlaceEvent e) {
		if (isEnabled() && freezeList.contains(e.getPlayer().getName()))
			e.setCancelled(true);
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
	
	public String translateVars(String msg, String p, Location l) {
		msg = msg.replace("{PLAYER}", (p==null)?"NULL":p);
		msg = msg.replace("{NAME}", (p==null)?"NULL":p);
		msg = msg.replace("{X}", ""+l.getBlockX());
		msg = msg.replace("{Y}", ""+l.getBlockY());
		msg = msg.replace("{Z}", ""+l.getBlockZ());
		if (addressMap.containsKey(p)) {
			msg = msg.replace("{ADDRESS}", addressMap.containsKey(p)?addressMap.get(p):"");
			msg = msg.replace("{ADDR}", addressMap.containsKey(p)?addressMap.get(p):"");
			msg = msg.replace("{IP}", addressMap.containsKey(p)?addressMap.get(p):"");
		}
		msg = ChatColor.translateAlternateColorCodes('&', msg);
		return msg;
	}
}
