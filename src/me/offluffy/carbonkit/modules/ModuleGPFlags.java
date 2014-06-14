package me.offluffy.carbonkit.modules;

import java.util.HashMap;
import java.util.Set;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.cmds.CommandGPFlags;
import me.offluffy.carbonkit.utils.DuplicateModuleException;
import me.offluffy.carbonkit.utils.EntityHelper;
import me.offluffy.carbonkit.utils.FlaggedClaim;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Log;
import me.offluffy.carbonkit.utils.Messages.Clr;
import me.offluffy.carbonkit.utils.Module;
import me.offluffy.carbonkit.utils.PlayerData;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class ModuleGPFlags extends Module {
	public static DataStore gp;
	private HashMap<String, Claim> lastClaim = new HashMap<String, Claim>();
	private HashMap<String, Long> gTimes = new HashMap<String, Long>();
	private HashMap<String, Long> fTimes = new HashMap<String, Long>();
	private Long timeout = 0L;
	
	public ModuleGPFlags() throws DuplicateModuleException {
		super("GriefPreventionFlags", "gpflags", "gpf");
		requires.add("GriefPrevention");
	}

	@Override
	public void initModule() {
		gp = GriefPrevention.instance.dataStore;
		timeout = CarbonKit.config.getLong(getName() + ".flag-timeout", 30L)*1000L;
		getFlags();
		for (Player pl : Bukkit.getOnlinePlayers()) {
			if (pl != null && pl.getLocation() != null) {
				Claim c = gp.getClaimAt(pl.getLocation(), false, null);
				if (c != null) {
					FlaggedClaim fc = getFlaggedClaim(c);
					if (fc != null)
						fc.addUser(pl.getName());
				}
			}
		}
		addCmd(new CommandGPFlags(this));
	}

	@Override
	public void disableModule() {}

	@Override
	protected boolean hasListeners() { return true; }

	@Override
	public boolean hasDependencies() {
		for (String r : requires)
			if (!CarbonKit.pm.isPluginEnabled(r))
				return false;
		return true;
	}
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/
	
	@EventHandler
	public void moveEvent(PlayerMoveEvent e) {
		if (PlayerData.getCoords(e.getPlayer().getName()) != null) {
			Player player = e.getPlayer();
			Location location = player.getLocation();
			int x = location.getBlockX();
			int z = location.getBlockZ();
			int fX = PlayerData.getCoords(player.getName())[0];
			int fZ = PlayerData.getCoords(player.getName())[1];
			int tX = x;
			int tZ = z;
			if ((fX != tX) || (fZ != tZ)) {
				Claim c = gp.getClaimAt(location, true, null);
				if (c != null) { 
					if (!lastClaim.containsKey(player.getName())) {
						String owner = c.getOwnerName();
						claimEnter(c, player, owner);
						lastClaim.put(player.getName(), c);
					} else if (!c.equals(lastClaim.get(player.getName()))) {
						Claim exClaim = lastClaim.get(player.getName());
						Claim enClaim = c;
						if (!(new FlaggedClaim(exClaim).getId() == -1L) && !(new FlaggedClaim(enClaim).getId() == -1L)) {
							claimChange(enClaim, exClaim, player);
							lastClaim.put(player.getName(), c);
						}
					}
				} else if (lastClaim.containsKey(player.getName())) {
					Claim claim = lastClaim.get(player.getName());
					String owner = lastClaim.get(player.getName()).getOwnerName();
					claimLeave(claim, player, owner);
					lastClaim.remove(player.getName());
				}
			}
		}
	}

	@EventHandler
	public void mobSpawnEvent(CreatureSpawnEvent e) {
		Location loc = e.getLocation();
		if (getFlaggedClaim(gp.getClaimAt(loc, false, null)) != null) {
			if (EntityHelper.isHostile(e.getEntity()))
				e.setCancelled(!getFlaggedClaim(gp.getClaimAt(loc, false, null)).getHostileSpawning());
			if (EntityHelper.isPassive(e.getEntity()))
				e.setCancelled(!getFlaggedClaim(gp.getClaimAt(loc, false, null)).getPassiveSpawning());
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
	
	private void claimEnter(Claim c, Player p, String o) {
		Log.debug(p.getName() + " entered a claim owned by " + o);
		FlaggedClaim fc = getFlaggedClaim(c);
		if (fc != null) {
			// Add user to players list
			if (!fc.getUsers().contains(p.getName()))
				fc.addUser(p.getName());
			// Greeting
			if (fc.isSet(FlaggedClaim.GREETING)) {
				String g = fc.getGreeting();
				if (fc.isSet(FlaggedClaim.NAME))
					g = g.replace("{NAME}",fc.getName());
				g = g.replace("{OWNER}",fc.getClaim().getOwnerName());
				g = g.replace("{PLAYER}", p.getName());
				boolean cp = (fc.getClaim().isAdminClaim() || CarbonKit.perms.has(fc.getClaim().getLesserBoundaryCorner().getWorld(), fc.getClaim().getOwnerName(), "gpflag.color"));
				if (p != null) {
					Long last = ((gTimes.containsKey(p.getName()))?gTimes.get(p.getName()):0L);
					Long crnt = System.currentTimeMillis();
					Long elpd = crnt - last;
					if (!gTimes.containsKey(p.getName())) {
						p.sendMessage(Clr.TITLE + "[GPF] " + ChatColor.RESET + ((cp)?ChatColor.translateAlternateColorCodes('&', g):g));
						last = System.currentTimeMillis();
						gTimes.put(p.getName(), last);
					} else if (elpd > timeout) {
						p.sendMessage(Clr.TITLE + "[GPF] " + ChatColor.RESET + ((cp)?ChatColor.translateAlternateColorCodes('&', g):g));
						last = System.currentTimeMillis();
						gTimes.put(p.getName(), last);
					}
				}
			}
		}
	}
	
	private void claimChange(Claim enc, Claim exc, Player p) {
		claimLeave(exc, p, exc.getOwnerName());
		claimEnter(enc, p, enc.getOwnerName());
	}
	
	private void claimLeave(Claim c, Player p, String o) {
		Log.debug(p.getName() + " left a claim owned by " + o);
		FlaggedClaim fc = getFlaggedClaim(c);
		if (fc != null) {
			// Remove player from users list
			if (fc.getUsers().contains(p.getName()))
				fc.removeUser(p.getName());
			// Farewell
			if (fc.isSet(FlaggedClaim.FAREWELL)) {
				String f = fc.getFarewell();
				if (fc.isSet(FlaggedClaim.NAME))
					f = fc.getFarewell().replace("{NAME}",fc.getName());
				f = f.replace("{OWNER}",fc.getClaim().getOwnerName());
				f = f.replace("{PLAYER}", p.getName());
				boolean cp = (fc.getClaim().isAdminClaim() || CarbonKit.perms.has(fc.getClaim().getLesserBoundaryCorner().getWorld(), fc.getClaim().getOwnerName(), "gpflag.color"));
				if (p != null) {
					Long last = ((fTimes.containsKey(p.getName()))?fTimes.get(p.getName()):0L);
					Long crnt = System.currentTimeMillis();
					Long elpd = crnt - last;
					if (!fTimes.containsKey(p.getName())) {
						p.sendMessage(Clr.TITLE + "[GPF] " + ChatColor.RESET + ((cp)?ChatColor.translateAlternateColorCodes('&', f):f));
						last = System.currentTimeMillis();
						fTimes.put(p.getName(), last);
					} else if (elpd > timeout) {
						p.sendMessage(Clr.TITLE + "[GPF] " + ChatColor.RESET + ((cp)?ChatColor.translateAlternateColorCodes('&', f):f));
						last = System.currentTimeMillis();
						fTimes.put(p.getName(), last);
					}
				}
			}
		}
	}
	
	public void getFlags() {
		if (!CarbonKit.data.contains("gpflag")) {
			CarbonKit.data.set(getName() + ".0", null);
			Lib.saveFile(CarbonKit.data, "data.yml");
		}
		Set<String> keys = CarbonKit.data.getConfigurationSection(getName()).getKeys(false);
		for (String key : keys) {
			try {
				Claim c = gp.getClaim(Long.parseLong(key));
				if (c != null) {
					FlaggedClaim fc = new FlaggedClaim(c);
					// Set name
					if (CarbonKit.data.contains(getName() + "." + key + ".name"))
						if (!CarbonKit.data.getString(getName() + "." + key + ".name").equals(""))
							fc.setName(CarbonKit.data.getString(getName() + "." + key + ".name"));
					// Set greeting
					if (CarbonKit.data.contains(getName() + "." + key + ".greeting"))
						if (!CarbonKit.data.getString(getName() + "." + key + ".greeting").equals(""))
							fc.setGreeting(CarbonKit.data.getString(getName() + "." + key + ".greeting"));
					// Set farewell
					if (CarbonKit.data.contains(getName() + "." + key + ".farewell"))
						if (!CarbonKit.data.getString(getName() + "." + key + ".farewell").equals(""))
							fc.setFarewell(CarbonKit.data.getString(getName() + "." + key + ".farewell"));
					// Set hostile spawning
					if (CarbonKit.data.contains(getName() + "." + key + ".hostilespawning"))
						fc.setHostileSpawning(CarbonKit.data.getBoolean(getName() + "." + key + ".hostilespawning"));
					// Set passive spawning
					if (CarbonKit.data.contains(getName() + "." + key + ".passivespawning"))
						fc.setPassiveSpawning(CarbonKit.data.getBoolean(getName() + "." + key + ".passivespawning"));
					FlaggedClaim.flaggedClaims.add(fc);
				} else {
					CarbonKit.data.set(getName() + "." + key, null);
					Lib.saveFile(CarbonKit.data, "data.yml");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public FlaggedClaim getFlaggedClaim(Claim c) {
		if (c == null)
			return null;
		for (FlaggedClaim fc : FlaggedClaim.flaggedClaims)
			if (fc.getClaim().getID() == c.getID())
				return fc;
		return null;
	}
}
