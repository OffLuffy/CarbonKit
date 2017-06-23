package net.teamcarbon.carbonkit.utils;

import net.teamcarbon.carbonkit.CarbonKit;
import org.bukkit.OfflinePlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("unused")
public class UserStore extends YamlConfig {

	private UUID uuid;

	private static final File USER_DIR = new File(CarbonKit.inst.getDataFolder(), "userData");

	// CONSTRUCTORS

	public UserStore(OfflinePlayer pl) { this(pl.getUniqueId()); }
	public UserStore(UUID id) {
		super(CarbonKit.inst, new File(USER_DIR, id + ".yml"), "yml/user.yml");
		CarbonKit.log.debug("Loading user data for UUID: " + id);
		uuid = id;
		createUserDir();

		addDefault("global.last-name", "");
		addDefault("global.last-address", "Unknown");
		addDefault("global.name-history", new ArrayList<String>());
	}

	// PUBLIC

	public UUID getUuid() { return uuid; }
	public String getLastUsername() { return getString("global.last-name", ""); }
	public String getLastAddress() { return getString("global.last-address", "Unknown"); }
	public List<String> getPreviousNames() { return getStringList("global.name-history"); }

	public void setLastUsername(String username) {
		if (username == null || username.isEmpty()) return;
		set("global.last-name", username);
		save();
	}

	public void addPreviousNames(String ... usernames) {
		List<String> names = getPreviousNames();
		for (String u : usernames)
			if (!names.contains(u)) names.add(u);
		set("global.name-history", names);
		save();
	}

	public void removePreviousName(String username) {
		List<String> names = getPreviousNames();
		if (names.contains(username)) names.remove(username);
		set("global.name-history", names);
		save();
	}

	public void setLastAddress(String address) {
		if (address == null || address.isEmpty()) return;
		set("global.last-address", address);
		save();
	}

	// STATIC

	public static boolean userFileExists(UUID id) { return (new File(USER_DIR, id + ".yml")).exists(); }

	private static void createUserDir() {
		if (!USER_DIR.exists() && !USER_DIR.mkdirs()) {
			CarbonKit.log.debug("Failed to create userData directory");
		}
	}

}
