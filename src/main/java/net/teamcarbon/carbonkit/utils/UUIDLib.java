package net.teamcarbon.carbonkit.utils;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Conveneince method to handle UUIDS and store UUIDS with associated usernames
 */
@SuppressWarnings({"UnusedDeclaration", "deprecation"})
public final class UUIDLib {
	private static HashMap<String, UUID> storedIds = new HashMap<>();
	/**
	 * Stores the given id and player name
	 * @param pl The player name associated with the id
	 * @param id The id associated with the player name
	 */
	public static void store(String pl, UUID id) {
		if (storedIds.containsValue(id))
			for (String p : (new HashMap<>(storedIds)).keySet())
				if (storedIds.get(p).equals(id))
					storedIds.remove(p);
		storedIds.put(pl, id);
	}
	/**
	 * Stores the given id and player name
	 * @param pl The player name associated with the id
	 * @param id The id associated with the player name
	 */
	public static void store(Player pl, UUID id) { store(pl.getName(), id); }
	/**
	 * Stores the given id and player name
	 * @param pl The player name associated with the id
	 * @param id The id associated with the player name
	 */
	public static void store(String pl, String id) { store(pl, toUUID(id)); }
	/**
	 * Stores the given id and player name
	 * @param pl The player name associated with the id
	 * @param id The id associated with the player name
	 */
	public static void store(Player pl, String id) { store(pl.getName(), toUUID(id)); }
	/**
	 * Attempts to convert the given String into a proper UUID object
	 * @param id The string form to try and convert to a UUID
	 * @return UUID object if successful, null otherwise
	 */
	public static UUID toUUID(String id) {
		if (id.contains("-"))
			id = id.replace("-", "");
		if (!id.toLowerCase().matches("[a-f0-9]{32}"))
			return null;
		return UUID.fromString(id.substring(0,8) + "-" + id.substring(8,12) + "-"
				+ id.substring(12,16) + "-" + id.substring(16,20) + "-" + id.substring(20,32));
	}
	/**
	 * Convertes the provided UUID into String form without dashes
	 * @param id The UUID object to convert
	 * @return String of the String form of the UUID
	 */
	public static String idToString(UUID id) { return id.toString().toLowerCase().replace("-", ""); }
	/**
	 * Fetches a UUID associated with the given Player object, alias for org.bukkit.entity.player#getUniqueID()
	 * @param player the Player to get the UUID from
	 * @return The player's associated UUID
	 * @see org.bukkit.entity.Player#getUniqueId()
	 */
	public static UUID nameToID(Player player) { return player.getUniqueId(); }
	/**
	 * Fetches a UUID associated with the given Player object
	 * @param name the player name to search for
	 * @return The player's associated UUID if found, null otherwise
	 */
	public static UUID nameToID(String name) {
		if (storedIds.containsKey(name))
			return storedIds.get(name);
		Player pl = Bukkit.getPlayer(name);
		if (pl != null && pl.isOnline())
			return pl.getUniqueId();
		try {
			UUID uuid = UUIDFetcher.getUUIDOf(name);
			storedIds.put(name, uuid);
			return uuid;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static OfflinePlayer nameToOfflinePlayer(String name) {
		if (Bukkit.getPlayer(name) != null)
			return Bukkit.getPlayer(name);
		UUID id = nameToID(name);
		OfflinePlayer p = null;
		if (id != null)
			p = Bukkit.getOfflinePlayer(id);
		return p;
	}
}