package net.teamcarbon.carbonkit.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;

/**
 * Convenience object to assist send common messages and colors <br />
 * Usually only used for colors since I use a CustomMessages class in most of my plugins,
 * but custom messages can be stored here as well
 */
@SuppressWarnings("UnusedDeclaration")
public final class Messages {
	private static HashMap<String, String> customMessages = new HashMap<>();

	/**
	 * Convenience enum, shorter than ChatColor and allows me to store custom color combinations
	 */
	public enum Clr {
		TITLE("6l"), TITLE2("9l"), NOTE("7o"),
		BLACK('0'), DARKBLUE('1'), DARKGREEN('2'), DARKAQUA('3'), DARKRED('4'), PUPRLE('5'), GOLD('6'), GRAY('7'), DARKGRAY('8'), BLUE('9'),
		LIME('a'), AQUA('b'), RED('c'), MAGENTA('d'), YELLOW('e'), WHITE('f'), MAGIC('k'), BOLD('l'), STRIKE('m'), UNDERLINE('n'), ITALIC('o'), RESET('r'), Clr();
		private String clr = "";
		Clr(String chars) { clr = fromChars(chars); }
		Clr(char ... chars) { clr = fromChars(chars); }
		/**
		 * Simply return the clr variable, which stores the color code sequence already as a String
		 * @return Returns a String of color codes
		 */
		public String toString() { return clr; }
		/**
		 * Short-hand method of the toString()
		 * @return toString()
		 */
		public String s() { return clr; }
		/**
		 * Converts a series of characters into a color code sequence (doesn't check for valid characters)
		 * @param chars The characters to convert to color codes
		 * @return Returns a sequence of each character prepended with the section symbol
		 */
		public static String fromChars(char ... chars) {
			String clrs = "";
			for (char c : chars) clrs += "\u00A7"+c;
			return clrs;
		}
		/**
		 * Converts a series of characters into a color code sequence (doesn't check for valid characters)
		 * @param chars The characters (as a string) to convert to color codes
		 * @return Returns a sequence of each character prepended with the section symbol
		 */
		public static String fromChars(String chars) { return fromChars(chars.toCharArray()); }

		/**
		 * Alias for ChatColor.translateAlternateColorCodes('&', msg)
		 * @param msg The message to have color codes translated in
		 * @return Returns the translated message
		 */
		public static String trans(String msg) { return msg == null ? "" : ChatColor.translateAlternateColorCodes('&', msg); }
	}
	/**
	 * Common messages
	 */
    public enum Message {
		NO_PERM("&4You do not have permission to do that"),
		NOT_ONLINE("&4You must be in-game to use that"),
		NEEDS_GROUP("&4You must specify a group!"),
		GENERIC_ERROR("&4An error occurred. Please report this to an admin!");
		private String msg;
		Message(String message) { this.msg = Clr.trans(message); }
		public String toString() { return msg; }
	}

	/**
	 * Store a custom message for use later
	 * @param key The name of the message, used to retrieve it later when sending
	 * @param msg The message that will be displayed
	 */
	public static void addCustomMessage(String key, String msg) { customMessages.put(key, msg); }
	/**
	 * Sends a message to a player from the Message list
	 * @param sender The CommandSender to send the message
	 * @param msg The Message enum to send
	 * @see Messages.Message
	 */
	public static void send(CommandSender sender, Message msg) { sender.sendMessage("" + msg); }
	/**
	 * Sends a message to a player from the Message list
	 * @param player The Player to send the message
	 * @param msg The Message enum to send
	 * @see Messages.Message
	 */
	public static void send(Player player, Message msg) { player.sendMessage("" + msg); }
	/**
	 * Sends a message to a player from the custom messages list
	 * @param sender The CommandSender to send the message
	 * @param msgKey The name of the stored custom message (NOT THE MESSAGE TO BE SENT)
	 */
	public static void send(CommandSender sender, String msgKey) { if (customMessages.containsKey(msgKey)) sender.sendMessage(customMessages.get(msgKey)); }
	/**
	 * Sends a message to a player from the custom messages list
	 * @param player The Player to send the message
	 * @param msgKey The name of the stored custom message (NOT THE MESSAGE TO BE SENT)
	 */
	public static void send(Player player, String msgKey) { if (customMessages.containsKey(msgKey)) player.sendMessage(customMessages.get(msgKey)); }
	/**
	 * Sends a formatted message to a player from the custom messages list
	 * @param sender The CommandSender to send the message
	 * @param msgKey The name of the stored custom message (NOT THE MESSAGE TO BE SENT)
	 * @param args Message formattings variables
	 */
	public static void send(CommandSender sender, String msgKey, Object ... args) {
		try {
			if (customMessages.containsKey(msgKey))
				sender.sendMessage(String.format(Locale.ENGLISH, customMessages.get(msgKey), args));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Sends a formatted message to a player from the custom messages list
	 * @param player The Player to send the message
	 * @param msgKey The name of the stored custom message (NOT THE MESSAGE TO BE SENT)
	 * @param args Message formattings variables
	 */
	public static void send(Player player, String msgKey, Object ... args) {
		try {
			if (customMessages.containsKey(msgKey))
				player.sendMessage(String.format(Locale.ENGLISH, customMessages.get(msgKey), args));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Broadcasts a messages to all online Op players
	 * @param msg The messages to broadcast
	 */
	public static void opBroadcast(Message msg) {
		for (OfflinePlayer p : Bukkit.getOperators())
			if (p.isOnline())
				send((Player)p, msg);
	}
	/**
	 * Broadcasts a messages to all online Op players
	 * @param msgKey The name of the stored custom message (NOT THE MESSAGE TO BE SENT)
	 */
	public static void opBroadcast(String msgKey) {
		for (OfflinePlayer p : Bukkit.getOperators())
			if (p.isOnline())
				send((Player)p, msgKey);
	}
	/**
	 * Broadcasts a messages to all online Op players
	 * @param msgKey The name of the stored custom message (NOT THE MESSAGE TO BE SENT)
	 * @param args Message formattings variables
	 */
	public static void opBroadcast(String msgKey, Object ... args) {
		for (OfflinePlayer p : Bukkit.getOperators())
			if (p.isOnline())
				send((Player)p, msgKey, args);
	}
	/**
	 * Broadcasts a message to all online players with the given permission
	 * @param perm The permission to check before broadcasting to that player
	 * @param msg The message to broadcast
	 */
	public static void permBroadcast(String perm, Message msg) {
		for (OfflinePlayer p : Bukkit.getOfflinePlayers())
			if (p.isOnline() && MiscUtils.perm(((Player)p).getWorld(), p, perm))
				send((Player)p, msg);
	}
	/**
	 * Broadcasts a message to all online players with the given permission
	 * @param perm The permission to check before broadcasting to that player
	 * @param msgKey The name of the stored custom message (NOT THE MESSAGE TO BE SENT)
	 */
	public static void permBroadcast(String perm, String msgKey) {
		for (OfflinePlayer p : Bukkit.getOfflinePlayers())
			if (p.isOnline() && MiscUtils.perm(((Player)p).getWorld(), p, perm))
				send((Player)p, msgKey);
	}
	/**
	 * Broadcasts a message to all online players with the given permission
	 * @param perm The permission to check before broadcasting to that player
	 * @param msgKey The name of the stored custom message (NOT THE MESSAGE TO BE SENT)
	 * @param args Message formattings variables
	 */
	public static void permBroadcast(String perm, String msgKey, Object ... args) {
		for (OfflinePlayer p : Bukkit.getOfflinePlayers())
			if (p.isOnline() && MiscUtils.perm(((Player)p).getWorld(), p, perm))
				send((Player)p, msgKey, args);
	}
}
