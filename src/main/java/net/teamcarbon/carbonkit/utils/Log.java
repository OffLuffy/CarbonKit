package net.teamcarbon.carbonkit.utils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("UnusedDeclaration")
public final class Log {

	private JavaPlugin pl;
	private String debugPath;

	/**
	 * Convenience class to enable easy logging and colored ouput
	 * @param pl A JavaPlugin object necessary to fetch names and configs
	 * @param debugPath The config path of a boolean value indicating if debug logging is enabled
	 */
	public Log(JavaPlugin pl, String debugPath) {
		this.pl = pl;
		this.debugPath = debugPath;
	}

	/**
	 * Represents a preset message severity level
	 */
	public enum Level {
		INFO('a'), WARN('6'), SEVERE('4'), DEBUG('d');
		private char c = 'f';
		Level(char color) {
			if ((c+"").matches("^[0-9a-fA-Fk-oK-O]$"))
				c = color;
		}
		public String c() { return ChatColor.getByChar(c)+""; }
	}
	/**
	 * Log a message with INFO level (Green text)
	 * @param msg The message to log
	 * @see Log.Level
	 */
	public void info(String msg) { log(msg, Level.INFO); }
	/**
	 * Log a message with WARN level (Orange text)
	 * @param msg The message to log
	 * @see Log.Level
	 */
	public void warn(String msg) { log(msg, Level.WARN); }
	/**
	 * Log a message with SEVERE level (Red text)
	 * @param msg The message to log
	 * @see Log.Level
	 */
	public void severe(String msg) { log(msg, Level.SEVERE); }
	/**
	 * Log a message with DEBUG level (Magenta text), only prints if dubug is enabled in config
	 * @param msg The message to log
	 * @see Log.Level
	 */
	public void debug(String msg) { if (isDebugEnabled()) log(msg, Level.DEBUG); }
	/**
	 * Log a message with no log level or color
	 * @param msg The message to log
	 */
	public void log(String msg) {
		String name = (pl != null? pl.getDescription().getName() + " : " : "CarbonLog : ");
		Bukkit.getConsoleSender().sendMessage("[" + name + "LOG] " + msg);
	}
	/**
	 * Log a message with the specified log level
	 * @param msg The Message to log
	 * @param level The Level which the message will be logged
	 * @see Log.Level
	 */
	private void log(String msg, Level level) {
		String name = (pl != null? pl.getDescription().getName() + " : " : "CarbonLog : ");
		Bukkit.getConsoleSender().sendMessage(level.c() + "[" + name + level.name() + "] " + msg);
	}
	/**
	 * @return Returns true if the plugin is not null and the specified debug config path returns true, false otherwise
	 */
	public boolean isDebugEnabled() { return pl != null && debugPath != null && pl.getConfig().getBoolean(debugPath, false); }

}
