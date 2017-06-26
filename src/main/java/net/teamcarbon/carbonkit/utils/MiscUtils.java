package net.teamcarbon.carbonkit.utils;

import net.teamcarbon.carbonkit.utils.Messages.Clr;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.milkbowl.vault.item.ItemInfo;
import net.milkbowl.vault.item.Items;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.bukkit.Material.*;

/**
 * Convenience class with several miscellaneous purposes
 * @author OffLuffy
 */
@SuppressWarnings({"UnusedDeclaration"})
public final class MiscUtils {

	// Materials that allow a player to walk through them
	private static final Material[] permeableMats = new Material[]{
			AIR, SAPLING, POWERED_RAIL, DETECTOR_RAIL, LONG_GRASS, DEAD_BUSH, YELLOW_FLOWER, RED_ROSE, BROWN_MUSHROOM,
			RED_MUSHROOM, TORCH, REDSTONE_WIRE, SEEDS, SIGN_POST, WOODEN_DOOR, LADDER, RAILS, WALL_SIGN, LEVER, STONE_PLATE,
			IRON_DOOR_BLOCK, WOOD_PLATE, REDSTONE_TORCH_OFF, REDSTONE_TORCH_ON, STONE_BUTTON, SNOW, SUGAR_CANE_BLOCK,
			DIODE_BLOCK_OFF, DIODE_BLOCK_ON, PUMPKIN_STEM, MELON_STEM, VINE, FENCE_GATE, WATER_LILY, NETHER_WARTS,
			CARPET, ACTIVATOR_RAIL, SIGN, BANNER, STANDING_BANNER, WALL_BANNER, WATER, STATIONARY_WATER, LAVA, STATIONARY_LAVA,
			WEB, FIRE, REDSTONE, WOOD_BUTTON, REDSTONE_COMPARATOR_OFF, REDSTONE_COMPARATOR_ON, TRIPWIRE, TRIPWIRE_HOOK,
			CARROT, CROPS, POTATO, FLOWER_POT, DOUBLE_PLANT
	};

	// Of the Materials that are permeable, ones that allow the player to stand on them and covers whole square
	private static final Material[] supportingMats = new Material[]{
			POWERED_RAIL, RAILS, SNOW, CARPET, ACTIVATOR_RAIL, DETECTOR_RAIL
	};

	private static final HashMap<Enchantment, String[]> enchantAliases = new HashMap<>();

    private static Permission perms;

	public MiscUtils() {
		enchantAliases.put(Enchantment.DAMAGE_ALL, new String[] {"alldamage", "alldmg", "sharpness", "sharp", "dal"});
		enchantAliases.put(Enchantment.DAMAGE_ARTHROPODS , new String[] {"ardmg", "baneofarthropods", "baneofarthropod", "arthropod", "dar"});
		enchantAliases.put(Enchantment.DAMAGE_UNDEAD , new String[] {"undeaddamage", "smite", "du"});
		enchantAliases.put(Enchantment.DIG_SPEED , new String[] {"digspeed", "efficiency", "minespeed", "cutspeed", "ds", "eff"});
		enchantAliases.put(Enchantment.DURABILITY , new String[] {"durability", "dura", "unbreaking", "d"});
		enchantAliases.put(Enchantment.THORNS , new String[] {"thorns", "highcrit", "thorn", "highercrit", "t"});
		enchantAliases.put(Enchantment.FIRE_ASPECT , new String[] {"fireaspect", "fire", "meleefire", "meleeflame", "fa"});
		enchantAliases.put(Enchantment.KNOCKBACK , new String[] {"knockback", "kback", "kb", "k"});
		enchantAliases.put(Enchantment.LOOT_BONUS_BLOCKS , new String[] {"blockslootbonus", "fortune", "fort", "lbb"});
		enchantAliases.put(Enchantment.LOOT_BONUS_MOBS , new String[] {"mobslootbonus", "mobloot", "looting", "lbm"});
		enchantAliases.put(Enchantment.OXYGEN , new String[] {"oxygen", "respiration", "breathing", "breath", "o"});
		enchantAliases.put(Enchantment.PROTECTION_ENVIRONMENTAL , new String[] {"protection", "prot", "protect", "p"});
		enchantAliases.put(Enchantment.PROTECTION_EXPLOSIONS , new String[] {"explosionsprotection", "explosionprotection", "expprot", "blastprotection", "bprotection", "bprotect", "blastprotect", "pe"});
		enchantAliases.put(Enchantment.PROTECTION_FALL , new String[] {"fallprotection", "fallprot", "featherfall", "featherfalling", "pfa"});
		enchantAliases.put(Enchantment.PROTECTION_FIRE , new String[] {"fireprotection", "flameprotection", "fireprotect", "flameprotect", "fireprot", "flameprot", "pf"});
		enchantAliases.put(Enchantment.PROTECTION_PROJECTILE , new String[] {"projectileprotection", "projprot", "pp"});
		enchantAliases.put(Enchantment.SILK_TOUCH , new String[] {"silktouch", "softtouch", "st"});
		enchantAliases.put(Enchantment.WATER_WORKER , new String[] {"waterworker", "aquaaffinity", "watermine", "ww"});
		enchantAliases.put(Enchantment.FIRE_ASPECT , new String[] {"firearrow", "flame", "flamearrow", "af"});
		enchantAliases.put(Enchantment.ARROW_DAMAGE , new String[] {"arrowdamage", "power", "arrowpower", "ad"});
		enchantAliases.put(Enchantment.ARROW_KNOCKBACK , new String[] {"arrowknockback", "arrowkb", "punch", "arrowpunch", "ak"});
		enchantAliases.put(Enchantment.ARROW_INFINITE , new String[] {"infinitearrows", "infarrows", "infinity", "infinite", "unlimited", "unlimitedarrows", "ai"});
		enchantAliases.put(Enchantment.LUCK , new String[] {"luck", "luckofsea", "luckofseas", "rodluck"});
		enchantAliases.put(Enchantment.LURE , new String[] {"lure", "rodlure"});
		enchantAliases.put(Enchantment.DEPTH_STRIDER , new String[] {"depthstrider", "depth", "strider"});
	}

    /**
     * Sets the Permission object used for perm checking
     * @param p The Permission object provided by Vault
     */
    public static void setPerms(Permission p) { perms = p; }
	/**
	 * Fetch the Permission object used for perm checking
	 * @return Returns the cached Permission object
	 */
	public static Permission getPerms() { return perms; }
    /**
     * Checks if the Player has any of the listed perms
     * @param player The Player to check
     * @param permissions The list of perms to check
     * @return Returns true if the player has any of the perms
     */
    public static boolean perm(Player player, String ... permissions) {
        for (String p : permissions)
            if (player != null && p != null)
                if (perms != null) {
					return perms.playerHas(player, p);
				} else if (player.hasPermission(p)) return true;
        return false;
    }
	/**
	 * Checks if the Player has any of the listed perms
	 * @param w The World to check permissions in
	 * @param player The Player to check
	 * @param permissions The list of perms to check
	 * @return Returns true if the player has any of the perms, false otherwise or if the World can't be resolved
	 */
	public static boolean perm(World w, OfflinePlayer player, String ... permissions) {
		if (w == null) w = (player.isOnline() ? ((Player) player).getWorld() : Bukkit.getWorlds().get(0));
		if (w == null || perms == null || player == null) return false;
		for (String p : permissions)
			if (p != null && perms.playerHas(w.getName(), player, p))
				return true;
		return false;
	}
	/**
	 * Checks if the Player has any of the listed perms in their current world or default world if they're offline
	 * @param player The Player to check
	 * @param permissions The list of perms to check
	 * @return Returns true if the player has any of the perms, false if not or if the World is somehow null
	 */
	public static boolean perm(OfflinePlayer player, String ... permissions) {
		return player.isOnline() ? perm((Player) player, permissions) : perm(null, player, permissions);
	}
    /**
     * Checks if the CommandSender has any of the listed perms
     * @param sender The CommandSender to check
     * @param permissions The list of perms to check
     * @return Returns true if the CommandSender has any of the perms
     */
    public static boolean perm(CommandSender sender, String ... permissions) {
        for (String p : permissions) {
			if (perms != null && sender != null && p != null) {
				if (perms.has(sender, p))
					return true;
			}
		}
        return false;
    }
	/**
	 * Broadcasts a message to all online players with the given permission
	 * @param perm The permission node that players must have to view the message
	 * @param msg The message to broadcast
	 */
	public static void permBroadcast(String perm, String msg) {
		for (Player p : Bukkit.getOnlinePlayers()) permSend(p, perm, msg);
	}
	/**
	 * Broadcasts a message to all online players with the given permission
	 * @param perm The permission node that players must have to view the message
	 * @param avoid A List of &lt;? extends Player&gt; whom to not send the message to
	 * @param msg The message to broadcast
	 */
	public static void permBroadcast(String perm, List<? extends Player> avoid, String msg) {
		for (Player p : Bukkit.getOnlinePlayers()) if (perm(p, perm) && !avoid.contains(p)) p.sendMessage(msg);
	}
	/**
	 * Sends a message to the specified Player if they have the specified permission
	 * @param pl The Player to send the message to if they have permission
	 * @param msg The Message to send to the Player if they have permission
	 * @param perm The permission node to check before sending the message
	 */
	public static void permSend(Player pl, String perm, String msg) { if (perm(pl, perm)) pl.sendMessage(msg); }
	/**
	 * Sends a message to the specified CommandSender if they have the specified permission
	 * @param s The CommandSender to send the message to if they have permission
	 * @param msg The Message to send to the CommandSender if they have permission
	 * @param perm The permission node to check before sending the message
	 */
	public static void permSend(CommandSender s, String perm, String msg) { if (perm(s, perm)) s.sendMessage(msg); }
    /**
     * Checks a String query against a list of Strings
     * @param query The string to check
     * @param matches The list of Strings to check the query against
     * @return Returns true if the query matches any String from matches (case-insensitive)
     */
    public static boolean eq(String query, String ... matches) {
		query = query.replace("_", "").replace("-", "");
        for (String s : matches) {
			s = s.replace("_", "").replace("-", "");
			if (query.equalsIgnoreCase(s))
				return true;
		}
        return false;
    }
	/**
	 * Checks a String query against a list of Strings
	 * @param query The string to check
	 * @param matches The list of Strings to check the query against
	 * @return Returns true if the query matches any String from matches (case-insensitive)
	 */
	public static boolean eq(String query, List<String> matches) { return eq(query, matches.toArray(new String[matches.size()])); }
	/**
	 * Checks if the query starts with any of the provided Strings in matches (case-insensitive)
	 * @param query The String to check
	 * @param matches The Strings to check if 'query' starts with
	 * @return Returns true if query starts with (case-insensitive) any of the matches, false otherwise
	 */
	public static boolean starts(String query, String ... matches) { for (String s : matches) if (query.toLowerCase().startsWith(s.toLowerCase())) return true; return false; }
	/**
	 * Checks an Object's equality to a list of other Objects. Uses == for Enums.
	 * @param obj The Object to compare
	 * @param query An array of Objects to compare the obj against
	 * @return Returns true if 'obj' is equal to any Object in the 'query' array
	 */
	@SafeVarargs
	public static <T> boolean objEq(T obj, T ... query) {
		for (T q : query) { if ((obj.getClass().isEnum() && obj == q) || obj.equals(q)) { return true; } }
		return false;
	}
	/**
	 * Checks if an object is exactly equal (==) to any other objects in 'query'
	 * @param obj The initial object to compare to the list of objects
	 * @param query The list of objects to be compared to the initial object
	 * @return Returns true if 'obj' is exactly equal (== comparison) to any object in 'query'
	 */
	@SafeVarargs
	public static <T> boolean exactEq(T obj, T ... query) {
		for (T b : query) { if (obj == b) return true; }
		return false;
	}
	/**
	 * Short-hand method to capitalize the first letter of a String
	 * @param word The String to be capitalized
	 * @param lowerOthers Whether or not to convert all other letters to lower case
	 * @return Returns the String with the first letter capitalized
	 */
	public static String capFirst(String word, boolean lowerOthers) {
		if (word.isEmpty()) return "";
		if (word.length() == 1) return word.toUpperCase();
		return (word.charAt(0)+"").toUpperCase() + ((lowerOthers) ? word.substring(1).toLowerCase() : word.substring(1));
	}
	/**
	 * Short-hand method to capitalize the first letter of every word in a String
	 * @param words The String of words to be capitalized
	 * @param lowerOthers Whether or not to convert all other letters to lower case
	 * @return Returns the String after the first letter of each word is capitalized
	 */
	public static String capAllFirst(String words, boolean lowerOthers) {
		String[] wa = words.split(" "), wa2 = new String[wa.length];
		for (int i = 0; i < wa.length; i++) { wa2[i] = capFirst(wa[i], lowerOthers); }
		return implode(" ",0, wa2);
	}
	/**
	 * Replaces all instances of key values in 'rep' in 'subject' with the associated values in the HashMap
	 * @param subject The String to search and replace within
	 * @param rep Pairings of key Strings (what to search for) and value Strings (what to replace it with)
	 * @return Returns the String 'subject' with all replacements
	 */
	public static String massReplace(String subject, HashMap<String, String> rep) {
		if (subject != null && !subject.isEmpty() && rep != null && !rep.isEmpty())
			for (String s : rep.keySet())
				if (s != null && rep.get(s) != null)
					subject = subject.replace(s, rep.get(s));
		return subject;
	}
	/**
	 * Short-hand method of forming a HashMap&lt;String, String&gt; from an array of strings (every other string being a key, the string after being the value)
	 * @param st The array of Strings to form into a HashMap&lt;String, String&gt;
	 * @return Returns a HashMap&lt;String, String&gt; filled with the given Strings
	 */
	public static HashMap<String,String> quickHash(String ... st) {
		HashMap<String,String> ret = new HashMap<>();
		for (int i = 0; i < st.length; i+=2)
			if (st.length > i+1)
				ret.put(st[i], st[i+1]);
		return ret;
	}
	/**
	 * Short-hand method of forming a List&lt;?&gt; from an array of items
	 * @param stuff The items being translated into a List
	 * @return Returns a List of the specified objects
	 */
	@SafeVarargs
	public static <T> List<T> quickList(T ... stuff) {
		List<T> ret = new ArrayList<>();
		Collections.addAll(ret, stuff);
		return ret;
	}

	/**
	 * Attempts to parse a material name or number to a Material using Vault
	 * @param mat The name or number to search for
	 * @return Returns a Material object if found, null otherwise
	 */
	public static Material getMaterial(String mat) {
		ItemInfo ii = Items.itemByString(mat);
		return ii == null ? null : ii.getType();
	}
	/**
	 * Attempts to parse a enchantment name or number to an Enchantment. Can use Essentials's enchant aliases if it's initialized
	 * @param ench The name or number to search for
	 * @return Returns an Enchantment object if found, null otherwise
	 */
	@SuppressWarnings("deprecation")
	public static Enchantment getEnchant(String ench) {
		if (Enchantment.getByName(ench.toUpperCase()) != null) return Enchantment.getByName(ench);
		if (TypeUtils.isInteger(ench) && Enchantment.getById(Integer.parseInt(ench)) != null) return Enchantment.getById(Integer.parseInt(ench));
		for (Enchantment e : enchantAliases.keySet()) if (eq(ench, enchantAliases.get(e))) return e;
		return null;
	}
	/**
	 * Checks if a plugin is installed and enabled
	 * @param pluginName The name of the plugin to check
	 * @return Returns true if the plugin is found and enabled, false otherwse
	 */
	public static boolean checkPlugin(String pluginName, boolean requireEnabled) {
		if (Bukkit.getPluginManager().getPlugin(pluginName) != null)
			if (!requireEnabled || Bukkit.getPluginManager().isPluginEnabled(pluginName))
				return true;
		return false;
	}
	/**
	 * Attempts to fetch the plugin if it's installed and enabled
	 * @param pluginName The name of the plugin to check
	 * @param requireEnabled Whether to require the plugin to be enabled to return the Plugin
	 * @return Returns the Plugin if the plugin is installed and enabled, null otherwise
	 */
	public static Plugin getPlugin(String pluginName, boolean requireEnabled) {
		if (Bukkit.getPluginManager().getPlugin(pluginName) != null)
			if (!requireEnabled || Bukkit.getPluginManager().isPluginEnabled(pluginName))
				return Bukkit.getPluginManager().getPlugin(pluginName);
		return null;
	}
	/*/**
	 * Attempts to remove a recipe
	 * @param r The Recipe to remove
	 */
	/*public static void removeRecipe(Recipe r) {
		if (CraftingManager.getInstance().getRecipes().contains(r))
			CraftingManager.getInstance().getRecipes().remove(r);
	}*/
	/*/**
	 * Attempts to remove a list of recipes <i>This doesn't work yet</i>
	 * @param r The List of Recipes to remove
	 */
	/*public static void removeRecipes(List<Recipe> r) {
		Iterator<Recipe> it = Bukkit.recipeIterator();
		while (it.hasNext()) {
			Recipe itr = it.next();
			if (itr != null && r.contains(itr))
				it.remove();
		}
	}*/

	/**
	 * Attempts to fetch an OfflinePlayer based on a String name or UUID
	 * @param query The username or UUID to search for
	 * @param matchOffline Whether or not to attempt to search through offline players
	 * @return Returns an OfflinePlayer if found, false otherwise
	 */
	public static OfflinePlayer getPlayer(String query, boolean matchOffline) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			try { if (p.getName().equalsIgnoreCase(query) || p.getUniqueId().equals(UUID.fromString(query))) return p;
			} catch (Exception e) { /*(new CarbonException(e)).printStackTrace();*/ }
		}
		if (matchOffline) {
			for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
				try {
					if (p.getName().equalsIgnoreCase(query) || p.getUniqueId().equals(UUID.fromString(query)))
						return p;
				} catch (Exception e) { /*(new CarbonException(e)).printStackTrace();*/ }
			}
		}
		return null;
	}
	/**
	 * Attempts to fetch an OfflinePlayer based on a String name or UUID
	 * @param id The UUID to search for
	 * @param matchOffline Whether or not to attempt to search through offline players
	 * @return Returns an OfflinePlayer if found, false otherwise
	 */
	public static OfflinePlayer getPlayer(UUID id, boolean matchOffline) {
		if (matchOffline) { return Bukkit.getPlayer(id); } else {
			for (Player pl : Bukkit.getOnlinePlayers()) {
				if (pl.getUniqueId().equals(id)) return pl;
			}
		}
		return null;
	}
	/**
	 * Loads chunks in the given radius around the given Location
	 * @param location The Location around which to load chunks
	 * @param radius The radius around the Location in which to load chunks
	 */
	public static void loadChunks(Location location, final int radius) {
		for (int cx = (int)(location.getX() / 16.0) - radius; cx <= (int)(location.getX() / 16.0) + radius; cx++) {
			for (int cz = (int)(location.getZ() / 16.0) - radius; cz <= (int)(location.getZ() / 16.0) + radius; cz++) {
				location.getWorld().getChunkAt(cx, cz);
			}
		}
	}
	/**
	 * Attempts to add a String to a String list in a FileConfiguration (Doesn't save the file!)
	 * @param fc The FileConfiguration to modify
	 * @param path The path to the String list
	 * @param value The value to add to the String list (Uses Object's toString() method)
	 */
	public static boolean addToStringList(FileConfiguration fc, String path, Object value) {
		boolean added = false;
		List<String> list = fc.contains(path)?fc.getStringList(path): new ArrayList<>();
		if (!list.contains(value.toString())) { list.add(value.toString()); added = true; }
		fc.set(path, list);
		return added;
	}
	/**
	 * Attempts to remove a String to a String list in a FileConfiguration (Doesn't save the file!)
	 * @param fc The FileConfiguration to modify
	 * @param path The path to the String list
	 * @param value The value to remove from the String list (Uses Object's toString() method)
	 */
	public static boolean removeFromStringList(FileConfiguration fc, String path, Object value) {
		boolean removed = false;
		List<String> list = fc.contains(path)?fc.getStringList(path): new ArrayList<>();
		if (list.contains(value.toString())) { list.remove(value.toString()); removed = true; }
		fc.set(path, list);
		return removed;
	}

	/**
	 * Attempts to parse out each color/format code individually based on the permissions of the user
	 * @param msg The message to parse colors in
	 * @param p The Player whose permissions will be checked
	 * @param perm The permission node that will be appended with specific nodes like .color.red or .format.bold. A '.'
	 *             is not required at the end of this, it'll be added automatically if necessary.
	 * @return Returns the message that's parsed, doesn't parse codes the user doesn't have permission for
	 */
	public static String permColorParse(String msg, Player p, String perm) {
		// Add the trailing dot if there isn't one (since I'll be adding code-specific perm nodes on later)
		if (!perm.endsWith(".")) perm += ".";
		// If they have all perms, replace everything and return it
		if (perm(p, perm+"color.*") && perm(p, perm+"format.*")) return Clr.trans(msg);
		// Otherwise loop through each code and check for individual codes
		for (ChatColor cc : ChatColor.values()) {
			// If a color code, check if user has perm to change all colors, if not then check color.colorname
			if (cc.isColor() && (perm(p, perm+"color.*") || perm(p, perm+"color."+cc.name().toLowerCase())))
				msg = msg.replaceAll("&"+cc.getChar(), "\u00A7"+cc.getChar());
			// If a format code, check if user has perm to change all formats, if not then check format.formatname
			if (cc.isFormat() && (perm(p, perm+"format.*") || perm(p, perm+"format."+cc.name().toLowerCase())))
				msg = msg.replaceAll("&"+cc.getChar(), "\u00A7"+cc.getChar());
		}
		return msg;
	}

	/**
	 * Concatenates the array of Strings togther with the specified delimiter between each String.
	 * This method will use Strings from the array from index 'fromIndex' to index 'toIndex'
	 * @param array The array of Objects to concatenate together
	 * @param delimiter The String to place between each String that's combined
	 * @param fromIndex The index of the array to start from (inclusive)
	 * @param toIndex The index of the array to end (inclusive)
	 * @return Returns the concatenated Strings as a single String. Returns an empty string if array is null
	 */
	public static String implode(String delimiter, int fromIndex, int toIndex, Object... array) {
		if (array == null) return "";
		if (fromIndex < 0) { fromIndex = 0; }
		if (toIndex > array.length-1) { toIndex = array.length-1; }
		array = Arrays.copyOfRange(array, fromIndex, toIndex);
		StringBuilder sb = new StringBuilder();
		//String newString = "";
		boolean first = true;
		for (Object o : array) {
			String word = o == null ? "" : o.toString();
			if (o instanceof String) word = (String) o;
			else if (o instanceof Player) word = ((Player) o).getName();
			else if (o instanceof World) word = ((World) o).getName();
			//newString += (first ? "" : delimiter) + word;
			sb.append(first ? "" : delimiter);
			sb.append(word);
			first = false;
		}
		return sb.toString();
	}

	/**
	 * Concatenates the array of Strings togther with the specified delimiter between each String.
	 * This method will use String froms the array from index 'fromIndex' to the end of the array
	 * @param array The array of Strings to concatenate together
	 * @param delimiter The String to place between each String that's combined
	 * @param fromIndex The index of the array to start from (inclusive)
	 * @return Returns the concatenated Strings as a single String. Returns an empty string if array is null
	 */
	public static String implode(String delimiter, int fromIndex, Object... array) {
		return implode(delimiter, fromIndex, array.length - 1, array);
	}

	/*
	 * Concatenates the array of Strings togther with the specified delimiter between each String.
	 * This method will use all the Strings from the array
	 * @param array The array of Strings tos concatenate together
	 * @param delimiter The String to place between each String that's combined
	 * @return Returns the concatenated Strings as a single String. Returns an empty string if array is null
	 */
/*
	public static String implode(String delimiter, Object... array) {
		return implode(delimiter, 0, array.length - 1, array);
	}
*/
	/**
	 * Checks if the Material allows a user to pass through it
	 * @param mat The Material to check
	 * @return Returns true if the Material allows a user to walk through the block, false otherwise
	 */
	public static boolean isPermeable(Material mat) {
		return Arrays.asList(permeableMats).contains(mat);
	}

	/**
	 * Checks if the Material allws a user to stand on it
	 * @param mat The Material to check
	 * @return Returns true if the Material allows the user to stand on it, false otherwise
	 */
	public static boolean isSupporting(Material mat) {
		return !isPermeable(mat) || Arrays.asList(supportingMats).contains(mat);
	}

	/**
	 * Replaces supported variables with appropriate values
	 * @param msg The message containing variables to be replaced
	 * @param pl Player whose data to use for variable replacements
	 * @return Returns a String with all variables in the original message replaced
	 */
	public static String repVars(String msg, Player pl) {
		if (msg == null || msg.isEmpty()) return "";
		msg = Clr.trans(msg);
		if (pl == null) return msg;

		// User Identity
		if (msg.contains("{NAME}")) msg = msg.replace("{NAME}", pl.getName());
		if (msg.contains("{DISPNAME}")) msg = msg.replace("{DISPNAME}", pl.getDisplayName());
		if (msg.contains("{UUID}")) msg = msg.replace("{UUID}", pl.getUniqueId().toString());
		if (msg.contains("{IP}")){
			String ip = pl.getAddress() == null ? "" : pl.getAddress().toString();
			if (!ip.isEmpty()) {
				if (ip.contains("/")) ip = ip.replace("/", ""); // Remove leading slash
				if (ip.contains(":")) ip = ip.substring(0, ip.indexOf(":")); // Remove port and port delimiter
			}
			msg = msg.replace("{IP}", ip);
		}

		// User Stats
		if (msg.contains("{HEALTH}")) msg = msg.replace("{HEALTH}", sformat("%.0f", pl.getHealth()));
		if (msg.contains("{MAXHEALTH}")) msg = msg.replace("{MAXHEALTH}", sformat("%.0f", pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue()));
		if (msg.contains("{LEVEL}")) msg = msg.replace("{LEVEL}", sformat("%d", pl.getLevel()));
		if (msg.contains("{DAYSLIVED}")) msg = msg.replace("{DAYSLIVED}", sformat("%d", pl.getTicksLived() / 24000));

		// Server Info
		if (msg.contains("{TIME}")) msg = msg.replace("{TIME}", ticksToTime(pl.getWorld().getTime()));
		if (msg.contains("{TPS}")) msg = msg.replace("{TPS}", sformat("%.1f", LagMeter.getTPS()));

		// Location Info
		if (msg.contains("{WORLD}")) msg = msg.replace("{WORLD}", pl.getWorld().getName());
		if (msg.contains("{X}")) msg = msg.replace("{X}", sformat("%d", pl.getLocation().getBlockX()));
		if (msg.contains("{Y}")) msg = msg.replace("{Y}", sformat("%d", pl.getLocation().getBlockY()));
		if (msg.contains("{Z}")) msg = msg.replace("{Z}", sformat("%d", pl.getLocation().getBlockZ()));
		if (msg.contains("{PITCH}")) msg = msg.replace("{PITCH}", sformat("%.0f", pl.getLocation().getPitch()));
		if (msg.contains("{YAW}")) msg = msg.replace("{YAW}", sformat("%.0f", pl.getLocation().getYaw()));

		// Directional Info
		if (msg.contains("{FACING}")) msg = msg.replace("{FACING}", LocUtils.getCardinalDir(pl, false, false, false));
		if (msg.contains("{FACING_SHORT}")) msg = msg.replace("{FACING_SHORT}", LocUtils.getCardinalDir(pl, true, false, false));
		if (msg.contains("{FACING_SEC}")) msg = msg.replace("{FACING_SEC}", LocUtils.getCardinalDir(pl, false, false, true));
		if (msg.contains("{FACING_SHORT_SEC}")) msg = msg.replace("{FACING_SHORT_SEC}", LocUtils.getCardinalDir(pl, true, false, true));
		if (msg.contains("{COMPASS}")) msg = msg.replace("{COMPASS}", LocUtils.getCardinalDir(pl, false, true, false));
		if (msg.contains("{COMPASS_SHORT}")) msg = msg.replace("{COMPASS_SHORT}", LocUtils.getCardinalDir(pl, true, true, false));
		if (msg.contains("{COMPASS_SEC}")) msg = msg.replace("{COMPASS_SEC}", LocUtils.getCardinalDir(pl, false, true, true));
		if (msg.contains("{COMPASS_SHORT_SEC}")) msg = msg.replace("{COMPASS_SHORT_SEC}", LocUtils.getCardinalDir(pl, true, true, true));

		return msg;
	}

	/**
	 * Chooses the singular or plural form of a the specified words depending on the value of amnt (1 = singular, plural otherwise)
	 * @param amnt The amount
	 * @param singular The singular form of the word
	 * @param plural The plural form of the word
	 * @return Returns the singular form of the word if amnt is 1 and the plural form otherwise
	 */
	public static String plural(int amnt, String singular, String plural) { return amnt == 1 ? singular : plural; }

	/**
	 * Checks if the specified Player has access to the given Location<br>
	 *     Considers: GriefPrevention, WorldGuard
	 * @param loc The Location to check
	 * @param pl The Player to check
	 * @return Returns true if the Player has access to the given Location, false otherwise
	 */
	public static boolean accessCheck(Location loc, Player pl, TrustLevel gptl) {
		boolean allow = true;
		if (MiscUtils.checkPlugin("GriefPrevention", true)) {
			me.ryanhamshire.GriefPrevention.DataStore ds = me.ryanhamshire.GriefPrevention.GriefPrevention.instance.dataStore;
			me.ryanhamshire.GriefPrevention.Claim claim = ds.getClaimAt(loc, false, null);
			if (claim != null) {
				if (gptl == null) gptl = TrustLevel.BUILD;
				switch (gptl) {
					case BUILD: if (claim.allowBuild(pl, Material.AIR) != null) allow = false; break;
					case CONTAINER: if (claim.allowContainers(pl) != null) allow = false; break;
					case ACCESS: if (claim.allowAccess(pl) != null) allow = false; break;
				}
			}
		}
		if (MiscUtils.checkPlugin("WorldGuard", true)) {
			Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
			if (plugin != null) {
				com.sk89q.worldguard.bukkit.WorldGuardPlugin wgp = (com.sk89q.worldguard.bukkit.WorldGuardPlugin) plugin;
				if (!wgp.canBuild(pl, loc)) allow = false;
			}
		}
		return allow;
	}

	/**
	 * Prints a formatted header for the specified CommandSender
	 * @param sender The CommandSender to send the message to
	 * @param header The header to print (is wrapped in brackets)
	 */
	public static void printHeader(CommandSender sender, String header) {
		if (header != null && !header.isEmpty())
			sender.sendMessage(Clr.fromChars("6lm") + "=-=[" + Clr.fromChars("r6") + " " + header + " " + Clr.fromChars("6lm") + "]=-=-=-=");
		else printFooter(sender);
	}

	/**
	 * Prints a formatted divider for the specified CommandSender
	 * @param sender The CommandSender to send the message to
	 */
	public static void printDivider(CommandSender sender) { sender.sendMessage(Clr.fromChars("6lm") + "----------------"); }

	/**
	 * Prints a formatted footer for the specified CommandSender
	 * @param sender The CommandSender to send the message to
	 */
	public static void printFooter(CommandSender sender) { sender.sendMessage(Clr.fromChars("6lm") + "=-=-=-=-=-=-=-=-="); }

	/**
	 * Sends a formatted interactive  pagination footer to a Player (not supported for CommandSender)
	 * @param pl The Player to send the footer to
	 * @param numPages The number of pages in total
	 * @param curPage The currently viewed page
	 * @param cmdFormat The String used in String.format() with a single %d placeholder to build the command<br />
	 *                  <b>Example:</b> "/ticket listall %d" where %d will be replaced with the page number
	 */
	public static void printPaginatedFooter(Player pl, int numPages, int curPage, String cmdFormat) {
		final char PREV = '\u25C0', NEXT = '\u25B6';
		final String DPREV = PREV + "" + PREV, DNEXT = NEXT + "" + NEXT;
		final String PGIND = "[" + curPage + "/" + numPages + "]", PGINDT = "Page " + curPage + " of " + numPages;
		final Locale L = Locale.ENGLISH;
		curPage = NumUtils.normalizeInt(curPage, 1, numPages);
		boolean hasPrev = curPage != 1, hasNext = curPage != numPages;
		ChatColor nextClr = hasNext ? ChatColor.GREEN : ChatColor.GRAY;
		ChatColor prevClr = hasPrev ? ChatColor.GREEN : ChatColor.GRAY;
		ChatColor gldClr = ChatColor.GOLD, ylwClr = ChatColor.YELLOW;
		ChatColor[] bsClrs = new ChatColor[]{ChatColor.BOLD, ChatColor.STRIKETHROUGH};
		FormattedMessage fm = new FormattedMessage("");
		fm.then("=-=-=[").color(gldClr).style(bsClrs) .then(" " + DPREV + " ").color(prevClr);
		if (hasPrev) fm.command(String.format(L, cmdFormat, 1)).tooltip("First page");
		fm.then("-").color(gldClr).style(bsClrs) .then(" " + PREV + " ").color(prevClr);
		if (hasPrev) fm.command(String.format(L, cmdFormat, curPage-1)).tooltip("Prev page");
		fm.then(PGIND).color(ylwClr).tooltip(PGINDT).then(" " + NEXT + " ").color(nextClr);
		if (hasNext) fm.command(String.format(L, cmdFormat, curPage+1)).tooltip("Next page");
		fm.then("-").color(gldClr).style(bsClrs).then(" " + DNEXT + " ").color(nextClr);
		if (hasNext) fm.command(String.format(L, cmdFormat, numPages)).tooltip("Last page");
		fm.then("]=-=-=").color(gldClr).style(bsClrs);
		fm.send(pl);
	}

	public enum TrustLevel { BUILD, CONTAINER, ACCESS }

	/**
	 * Attempts to fetch an Entity that the specified Player is looking at
	 * @param player The Player whom is looking at an Entity
	 * @return Returns the Entity the Player is looking at or null if none is found
	 */
	public static Entity getTarget(final Player player) {
		assert player != null;
		Entity target = null;
		double targetDistanceSquared = 0;
		final double radiusSquared = 1;
		final org.bukkit.util.Vector l = player.getEyeLocation().toVector(), n = player.getLocation().getDirection().normalize();
		final double cos45 = Math.cos(Math.PI / 4);
		for (final LivingEntity other : player.getWorld().getEntitiesByClass(LivingEntity.class)) {
			if (other == player)
				continue;
			if (target == null || targetDistanceSquared > other.getLocation().distanceSquared(player.getLocation())) {
				final org.bukkit.util.Vector t = other.getLocation().add(0, 1, 0).toVector().subtract(l);
				if (n.clone().crossProduct(t).lengthSquared() < radiusSquared && t.normalize().dot(n) >= cos45) {
					target = other;
					targetDistanceSquared = target.getLocation().distanceSquared(player.getLocation());
				}
			}
		}
		return target;
	}

	public static ItemStack getSkull(String url) {
		ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
		if(url.isEmpty())return head;


		SkullMeta headMeta = (SkullMeta) head.getItemMeta();
		GameProfile profile = new GameProfile(UUID.randomUUID(), null);
		//byte[] encodedData = Base64.encodeBase64(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
		byte[] encoded = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
		profile.getProperties().put("textures", new Property("textures", new String(encoded)));
		Field profileField;
		try {
			profileField = headMeta.getClass().getDeclaredField("profile");
			profileField.setAccessible(true);
			profileField.set(headMeta, profile);
		} catch (NoSuchFieldException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
		head.setItemMeta(headMeta);
		return head;
	}

	public static String ticksToTime(long ticks) {
		ticks += 6000; // Offset 0 ticks to = 6AM
		int hours = (int)(ticks / 1000), minutes = (int)((ticks % 1000) / 16.66);
		return (hours > 12 ? hours > 24 ? hours - 24 : hours-12 : hours) + ":"
				+ (minutes < 10 ? "0" : "") + minutes + (hours >= 12 && hours < 24 ? " PM" : " AM");
	}

	private static String sformat(String pat, Object ... objects) { return String.format(Locale.ENGLISH, pat, objects); }
}
