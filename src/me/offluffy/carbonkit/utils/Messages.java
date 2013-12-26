package me.offluffy.carbonkit.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Messages {
	public static enum Clr {
		TITLE('6','l'), HEAD('a'), NORM('3'), NOTE('7','o'), ERR('c'), WARN('6'), PRE('7');
		private String c;
		Clr(char ... colorChar) {
			c = "";
			for (char ch : colorChar)
				c += "\u00A7" + ch;
		}
		@Override
		public String toString() { return c; }
		
		public String cc(char ... colors) {
			String s = "";
			for (char ch : colors)
				s += "\u00A7" + ch;
			return s;
		}
		
		public String cc(String colors) {
			return cc(colors.toCharArray());
		}
	}
	
	public static enum Message {
		NO_PERM(Clr.ERR + "You do not have permission to do that"),
		NOT_ONLINE(Clr.ERR + "You must be in-game to use that"),
		CMDBLOCK(Clr.NORM + "This can only be used from Command Blocks!");
		private String msg;
		Message(String msg) {
			this.msg = msg;
		}
		public String msg() {
			return msg;
		}
	}
	
	/**
	 * Sends a message to a player from the Message list
	 * @param sender The CommandSender to send the message
	 * @param msg The Message enum to send
	 * @see Message
	 */
	public static void send(CommandSender sender, Message msg) {
		sender.sendMessage(msg.msg());
	}
	
	/**
	 * Sends a message to a player from the Message list
	 * @param player The Player to send the message
	 * @param msg The Message enum to send
	 * @see Message
	 */
	public static void send(Player player, Message msg) {
		player.sendMessage(msg.msg());
	}
}
