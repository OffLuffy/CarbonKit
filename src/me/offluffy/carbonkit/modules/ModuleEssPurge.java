package me.offluffy.carbonkit.modules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.HashMap;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.cmds.CommandEssPurge;
import me.offluffy.carbonkit.utils.DuplicateModuleException;
import me.offluffy.carbonkit.utils.Lib;
import me.offluffy.carbonkit.utils.Log;
import me.offluffy.carbonkit.utils.Messages.Clr;
import me.offluffy.carbonkit.utils.Module;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class ModuleEssPurge extends Module {
	private static Plugin ess;
	private static File ep;
	private static File ef;
	private static File bf;
	private static String rf;
	private static File[] efList;
	private static FileConfiguration uFile;
	private static long uTime;
	private static long time;
	private static PrintWriter out;
	private static boolean timeSet = false;
	private static boolean purgeEmpty = false;
	private static boolean purgeTime = false;
	public static HashMap<String, String[]> epConf = new HashMap<String, String[]>();
	public ModuleEssPurge() throws DuplicateModuleException {
		super("EssentialsPurge", "esspurge", "ep");
	}

	@Override
	public void initModule() {
		addCmd(new CommandEssPurge(this));
		Log.info(getName() + ".auto-purge = " + CarbonKit.config.getBoolean(getName() + ".auto-purge", false));
		if (CarbonKit.config.getBoolean(getName() + ".auto-purge", false)) {
			Bukkit.getScheduler().runTaskLaterAsynchronously(CarbonKit.inst, new Runnable() {
				@Override
				public void run() {
					Log.info("Attempting to auto-purge Essentials user files...");
					String[] args = new String[2];
					args[0] = (CarbonKit.config.getBoolean(getName() + ".purge-empty",false))?"-e":"";
					args[1] = CarbonKit.config.getString(getName() + ".threshold", "3m");
					try {
						purge(Bukkit.getConsoleSender(), args);
					} catch (Exception e) {
						Log.warn("Auto-purge failed! Details:");
						e.printStackTrace();
						Log.warn("========[ End of EssPurge Report ]========");
					}
				}
			}, 20L);
		} else {
			Log.info("EssPurge auto-purge disabled.");
		}
	}

	@Override
	public void disableModule() {}

	@Override
	protected boolean hasListeners() { return false; }

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
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
	
	public static void purge(CommandSender purger, String[] args) throws FileNotFoundException {
		int purgeCount = 0;
		String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" };
		Calendar date = Calendar.getInstance();
		time = System.currentTimeMillis();
		date.setTimeInMillis(time);
		String amString;
		if (date.get(Calendar.AM_PM) == 0)
			amString = "AM";
		else
			amString = "PM";
		String dateString = months[date.get(Calendar.MONTH)] + " " + date.get(Calendar.DAY_OF_MONTH) + ", " + 
				date.get(Calendar.YEAR) + " @ " + pad(date.get(Calendar.HOUR)) + ":" + pad(date.get(Calendar.MINUTE)) + 
				amString + " -- Purge initiated by: " + purger.getName();
		String fName = "Log_" + months[date.get(Calendar.MONTH)]+ "_" + date.get(Calendar.DAY_OF_MONTH) + "_" + date.get(Calendar.YEAR) + ".txt"; 
		File logPath = new File(CarbonKit.inst.getDataFolder().toString() + File.separator + "epurge");
		File logFile = new File(logPath + File.separator + fName);
		if (!logPath.exists())
			logPath.mkdir();
		out = new PrintWriter(logFile);
		out.println("+=======================================================================+");
		out.println(String.format("| Log for %-61s |", dateString));
		out.println(">-----------------------------------------------------------------------<");
		out.println("|   PURGED USERS   |   THRESHOLD   |    OFFLINE    | OVERWROTE |  EMPTY |");
		out.println(">-----------------------------------------------------------------------<");
		long threshold = 0L;
		String argString = "";
		for (String a : args)
			argString = argString + a;
		if (argString.indexOf("-e") != -1)
			purgeEmpty = true;
		threshold = parseMilliseconds(argString);
		if ((!timeSet) && (!purgeEmpty)) {
			if (purger != null)
				purger.sendMessage(ChatColor.RED + "A threshold time was not set, please try again.");
			return;
		}
		if ((threshold <= 0L) && (!purgeEmpty)) {
			if (purger != null)
				purger.sendMessage(ChatColor.RED + "Cannot purge users with a 0ms or less threshold!");
			return;
		}
		String timeString = getTimeString(threshold);
		if ((timeSet) && (threshold > 0L) && (!purgeEmpty)) {
			if (purger != null)
				purger.sendMessage(ChatColor.GREEN + "Attempting to purge user data inactive for at least " + timeString);
			purgeTime = true;
		}
		if ((!timeSet) && (threshold <= 0L) && (purgeEmpty))
			if (purger != null)
				purger.sendMessage(ChatColor.GREEN + "Attempting to purge empty user files.");
		if ((timeSet) && (threshold > 0L) && (purgeEmpty)) {
			if (purger != null)
				purger.sendMessage(ChatColor.GREEN + "Attempting to purge empty user files and data inactive for at least " + timeString);
			purgeTime = true;
		}
		timeSet = false;
		ess = CarbonKit.pm.getPlugin("Essentials");
		ep = ess.getDataFolder().getParentFile();
		rf = (ep.getPath() + File.separator + "Essentials");
		ef = new File(rf + File.separator + "userdata");
		bf = new File(logPath + File.separator + "oldUserData");
		bf.setWritable(true);
		efList = ef.listFiles();
		String[] ecl = { "homes", "jailed", "socialspy", "teleportenabled", "money", "ban", "mail" };
		for (File f : efList) {
			if (!bf.exists())
				bf.mkdirs();
			if (f.isFile()) {
				uFile = new YamlConfiguration();
				try {
					boolean isEmpty = true;
					uFile.load(f);
					int ss = f.getName().indexOf(".");
					String name = f.getName().substring(0, ss);
					if (Bukkit.getServer().getPlayer(name) == null) { // User is not online
						for (String s : ecl)
							if (uFile.contains(s))
								isEmpty = false;
						uTime = uFile.getLong("timestamps.logout");
						if ((purgeEmpty) && (isEmpty)) { // The file is empty and purge empty is true
							String deletedOld = "false";
							File newFile = new File(bf + File.separator + f.getName());
							if (newFile.exists()) {
								newFile.delete();
								deletedOld = "true";
							}
							f.renameTo(newFile);
							if (newFile.exists() && f.exists())
								f.delete();
							out.println(String.format("| %16s | %13d | %13d | %9s | %6s |", name, Long.valueOf(threshold), Long.valueOf(time - uTime), deletedOld, Boolean.valueOf(isEmpty)));
							purgeCount++;
						} else if ((purgeTime) && (time - uTime > threshold)) {
							String deletedOld = "false";
							File newFile = new File(bf + File.separator + f.getName());
							if (newFile.exists()) {
								newFile.delete();
								deletedOld = "true";
							}
							f.renameTo(newFile);
							if (newFile.exists() && f.exists())
								f.delete();
							out.println(String.format("| %16s | %13d | %13d | %9s | %6s |", name, Long.valueOf(threshold), Long.valueOf(time - uTime), deletedOld, Boolean.valueOf(isEmpty)));
							purgeCount++;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		out.println(">-----------------------------------------------------------------------<");
		String cm = "Purged " + purgeCount + " users";
		out.println(String.format("| %69s |", cm));
		out.print("+=======================================================================+");
		out.close();
		if (purgeCount > 0) {
			Log.info("[EssPurge] Purged " + purgeCount + " users. Log written to " + logFile);
			if (purger != null)
				purger.sendMessage(Clr.TITLE + "[EssPurge] " + Clr.HEAD + "Purged " + Clr.NORM + purgeCount + Clr.HEAD + " users. Log written to " + Clr.NORM + logFile);
		} else {
			logFile.delete();
			Log.info("[EssPurge] No users were purged");
			if (purger != null)
				purger.sendMessage(Clr.TITLE + "[EssPurge] " + Clr.HEAD + "No users were purged");
		}
		epConf.remove((purger != null)?purger.getName():"CONSOLE");
		return;
	}

	public static String getTimeString(long l) {
		String timeString = "";
		long years = 0L;
		long months = 0L;
		long days = 0L;
		long hours = 0L;
		if (l >= 32140800000L) {
			years = l / 32140800000L;
			l -= years * 32140800000L;
		}
		if (l >= 2678400000L) {
			months = l / 2678400000L;
			l -= months * 2678400000L;
		}
		if (l >= 86400000L) {
			days = l / 86400000L;
			l -= days * 86400000L;
		}
		if (l >= 3600000L) {
			hours = l / 3600000L;
			l -= hours * 3600000L;
		}
		if (years > 0L)
			if (years == 1L)
				timeString = timeString + years + " year, ";
			else
				timeString = timeString + years + " years, ";
		if (months > 0L)
			if (months == 1L)
				timeString = timeString + months + " month, ";
			else
				timeString = timeString + months + " months, ";
		if (days > 0L)
			if (days == 1L)
				timeString = timeString + days + " day, ";
			else
				timeString = timeString + days + " days, ";
		if (hours > 0L) {
			if (hours == 1L)
				timeString = timeString + hours + " hour";
			else
				timeString = timeString + hours + " hours";
		}
		return timeString;
	}
	
	public static long parseMilliseconds(String s) {
		long day = 0L; long hr = 0L; long mon = 0L; long year = 0L; long total = 0L;
		try {
			String st = s.replace(" ", "").toLowerCase();
			String tempDigit = "";
			boolean isLetter = false;
			for (char c : st.toCharArray()) {
				if (Character.isDigit(c)) {
					tempDigit = tempDigit + c;
					isLetter = false;
				} else if ((!isLetter) && (Lib.eq(c, "h,d,m,y", true))) {
					switch (c) {
					case 'h':
						hr = Long.parseLong(tempDigit) * 3600000L;
						isLetter = true;
						tempDigit = "";
						timeSet = true;
						break;
					case 'd':
						day = Long.parseLong(tempDigit) * 86400000L;
						isLetter = true;
						tempDigit = "";
						timeSet = true;
						break;
					case 'm':
						mon = Long.parseLong(tempDigit) * 2678400000L;
						isLetter = true;
						tempDigit = "";
						timeSet = true;
						break;
					case 'y':
						year = Long.parseLong(tempDigit) * 32140800000L;
						isLetter = true;
						tempDigit = "";
						timeSet = true;
						break;
					default:
						System.out.println("Char " + c + " not found.");
					}
				}
			}
			total = day + hr + mon + year;
		} catch (Exception e) {
			System.out.println("An invalid value was specified for purge time!");
			e.printStackTrace();
		}
		return total;
	}
	
	public static String pad(int x) {
		if (x < 10)
			return "0" + x;
		else
			return ""+x;
	}

}
