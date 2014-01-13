package me.offluffy.carbonkit.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.modules.ModuleGPFlags;
import me.ryanhamshire.GriefPrevention.Claim;

import org.bukkit.Bukkit;

public class FlaggedClaim {
	private static ModuleGPFlags gMod = (ModuleGPFlags)Module.getModule("GriefPreventionFlags");
	private static List<FlaggedClaim> flaggedClaims = new ArrayList<FlaggedClaim>();
	public static final int NAME = 0, GREETING = 1, FAREWELL = 2, MONSPAWN = 3, ANISPAWN = 4;
	private Claim c;
	private HashMap<Integer, String> stringFlags = new HashMap<Integer, String>();
	private HashMap<Integer, Boolean> boolFlags = new HashMap<Integer, Boolean>();
	private HashMap<Integer, Boolean> setFlags = new HashMap<Integer, Boolean>();
	private List<String> users = new ArrayList<String>();
	
	// CONSTRUCT
	public FlaggedClaim(Claim claim) {
		if (claim != null)
			if (claim.parent != null)
				claim = claim.parent;
		c = claim;
		boolFlags.put(MONSPAWN, true);
		boolFlags.put(ANISPAWN, true);
		setFlags.put(NAME, false);
		setFlags.put(GREETING, false);
		setFlags.put(FAREWELL, false);
		setFlags.put(MONSPAWN, false);
		setFlags.put(ANISPAWN, false);
		flaggedClaims.add(this);
	}
	
	// GETS
	public Claim getClaim() {
		return c;
	}
	
	public static List<FlaggedClaim> getFlaggedClaims() {
		return flaggedClaims;
	}
	
	// FLAG GETS
	public String getName() {
		if (isSet(NAME))
			return stringFlags.get(NAME);
		else
			return null;
	}
	public long getId() {
		if (getClaim() == null || getClaim().getID() == null)
			return -1L;
		return getClaim().getID();
	}
	public String getGreeting() {
		if (isSet(GREETING))
			return stringFlags.get(GREETING);
		else
			return null;
	}
	public String getFarewell() {
		if (isSet(FAREWELL))
			return stringFlags.get(FAREWELL);
		else
			return null;
	}
	public boolean getHostileSpawning() {
		if (isSet(MONSPAWN))
			return boolFlags.get(MONSPAWN);
		else
			return true;
	}
	public boolean getPassiveSpawning() {
		if (isSet(ANISPAWN))
			return boolFlags.get(ANISPAWN);
		else
			return true;
	}
	
	// FLAG SETS
	public void setName(String name) {
		stringFlags.put(NAME, ((name.length() > 16)?name.substring(0,16):name));
		setFlags.put(NAME, true);
		CarbonKit.data.set(gMod.getName() + "." + getId() + ".name", ((name.length() > 16)?name.substring(0,16):name));
		Lib.saveFile(CarbonKit.data, "data.yml");
	}
	public void setGreeting(String greeting) {
		stringFlags.put(GREETING, (((greeting.length() > 64)?greeting.substring(0,64):greeting)));
		setFlags.put(GREETING, true);
		CarbonKit.data.set(gMod.getName() + "." + getId() + ".greeting", ((greeting.length() > 64)?greeting.substring(0,64):greeting));
		Lib.saveFile(CarbonKit.data, "data.yml");
	}
	public void setFarewell(String farewell) {
		stringFlags.put(FAREWELL, (((farewell.length() > 64)?farewell.substring(0,64):farewell)));
		setFlags.put(FAREWELL, true);
		CarbonKit.data.set(gMod.getName() + "." + getId() + ".farewell", ((farewell.length() > 64)?farewell.substring(0,64):farewell));
		Lib.saveFile(CarbonKit.data, "data.yml");
	}
	public void setHostileSpawning(boolean spawning) {
		boolFlags.put(MONSPAWN, spawning);
		setFlags.put(MONSPAWN, true);
		CarbonKit.data.set(gMod.getName() + "." + getId() + ".hostilespawning", spawning);
		Lib.saveFile(CarbonKit.data, "data.yml");
	}
	public void setPassiveSpawning(boolean spawning) {
		boolFlags.put(ANISPAWN, spawning);
		setFlags.put(ANISPAWN, true);
		CarbonKit.data.set(gMod.getName() + "." + getId() + ".passivespawning", spawning);
		Lib.saveFile(CarbonKit.data, "data.yml");
	}
	
	// FLAG REMOVES
	public void removeName() {
		if (stringFlags.containsKey(NAME))
			stringFlags.remove(NAME);
		setFlags.put(NAME, false);
		CarbonKit.data.set(gMod.getName() + "." + getId() + ".name", null);
		Lib.saveFile(CarbonKit.data, "data.yml");
		checkFlags();
	}
	public void removeGreeting() {
		if (stringFlags.containsKey(GREETING))
			stringFlags.remove(GREETING);
		setFlags.put(GREETING, false);
		CarbonKit.data.set(gMod.getName() + "." + getId() + ".greeting", null);
		Lib.saveFile(CarbonKit.data, "data.yml");
		checkFlags();
	}
	public void removeFarewell() {
		if (stringFlags.containsKey(FAREWELL))
			stringFlags.remove(FAREWELL);
		setFlags.put(FAREWELL, false);
		CarbonKit.data.set(gMod.getName() + "." + getId() + ".farewell", null);
		Lib.saveFile(CarbonKit.data, "data.yml");
		checkFlags();
	}
	public void removeHostileSpawning() {
		boolFlags.put(MONSPAWN, true);
		setFlags.put(MONSPAWN, false);
		CarbonKit.data.set(gMod.getName() + "." + getId() + ".hostilespawning", null);
		Lib.saveFile(CarbonKit.data, "data.yml");
		checkFlags();
	}
	public void removePassiveSpawning() {
		boolFlags.put(ANISPAWN, true);
		setFlags.put(ANISPAWN, false);
		CarbonKit.data.set(gMod.getName() + "." + getId() + ".passivespawning", null);
		Lib.saveFile(CarbonKit.data, "data.yml");
		checkFlags();
	}
	
	// MISC
	public boolean isSet(int flag) {
		return setFlags.get(flag);
	}
	public void addUser(String player) {
		if (Bukkit.getServer().getPlayer(player) != null)
			users.add(player);
	}
	public void removeUser(String player) {
		if (users.contains(player))
			users.remove(player);
	}
	public List<String> getUsers() {
		return users;
	}
	public static void saveClaimFlags() {
		for (FlaggedClaim fc : (flaggedClaims)) {
			String key = gMod.getName() + "." + fc.getId() + ".";
			if (fc.isSet(NAME))
				CarbonKit.data.set(key + "name", fc.getName());
			if (fc.isSet(GREETING))
				CarbonKit.data.set(key + "greeting", fc.getGreeting());
			if (fc.isSet(FAREWELL))
				CarbonKit.data.set(key + "farewell", fc.getFarewell());
			if (fc.isSet(MONSPAWN))
				CarbonKit.data.set(key + "hostilespawning", fc.getHostileSpawning());
			if (fc.isSet(ANISPAWN))
				CarbonKit.data.set(key + "passivespawning", fc.getHostileSpawning());
		}
		Lib.saveFile(CarbonKit.data, "data.yml");
	}
	private void checkFlags() {
		// Remove from flagged claims if no flags are set
		if (!(isSet(FlaggedClaim.NAME) || isSet(FlaggedClaim.GREETING) || isSet(FlaggedClaim.FAREWELL) || isSet(FlaggedClaim.MONSPAWN) || isSet(FlaggedClaim.ANISPAWN))) {
			FlaggedClaim.flaggedClaims.remove(this);
			CarbonKit.data.set(gMod.getName() + "." + getId(), null);
			Lib.saveFile(CarbonKit.data, "data.yml");
		}
	}
}
