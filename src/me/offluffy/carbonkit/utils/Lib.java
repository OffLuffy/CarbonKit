package me.offluffy.carbonkit.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import me.offluffy.carbonkit.CarbonKit;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * @author OffLuffy
 */
public class Lib {
	
	private static CarbonKit inst = CarbonKit.inst;
	
	/**
	 * Checks if the Player has any of the listed perms
	 * @param player The Player to check
	 * @param perms The list of perms to check
	 * @return Returns true if the player has any of the perms
	 */
	public static boolean perm(Player player, String ... perms) {
		for (String p : perms)
			if (CarbonKit.perms.has(player, p))
				return true;
		return false;
	}

	/**
	 * Checks if the CommandSender has any of the listed perms
	 * @param sender The CommandSender to check
	 * @param perms The list of perms to check
	 * @return Returns true if the CommandSender has any of the perms
	 */
	public static boolean perm(CommandSender sender, String ... perms) {
		for (String p : perms)
			if (CarbonKit.perms.has(sender, p))
				return true;
		return false;
	}
	
	/**
	 * Checks a String query against a list of Strings
	 * @param query The string to check
	 * @param matches The list of Strings to check the query against
	 * @return Returns true if the query matches any String from matches (case-insensitive)
	 */
	public static boolean eq(String query, String ... matches) {
		for (String s : matches)
			if (query.equalsIgnoreCase(s))
				return true;
		return false;
	}
	
	/**
	 * @param c The character to compare to 's'
	 * @param s Comma delimited list of characters to check against 'c'
	 * @param ignoreCase Whether to ignore case when comparing 's' and 'c'
	 * @return true if 'c' matches any characters in 's'
	 */
	public static boolean eq(char c, String s, boolean ignoreCase) {
		s = s.replace(" ", "").replace(",", "");
		if (ignoreCase) {
			s = s.toLowerCase();
			String ct = "" + c;
			ct = ct.toLowerCase();
			c = ct.charAt(0);
		}
		char[] ca = s.toCharArray();
		for (char cc : ca)
			if (c == cc)
				return true;
		return false;
	} // End eq
	
	/**
	 * Checks through Vault's groups to see if a group matching the query exists
	 * @param query The name of the group to check for
	 * @return Returns true if a group's name matching the query exists (case-insensitive)
	 */
	public static boolean groupExists(String query) {
		for (String group : CarbonKit.perms.getGroups())
			if (group.equalsIgnoreCase(query))
				return true;
		return false;
	}
	
	/**
	 * Loads the files from the jar's src folder and copies them to the plugin's directory
	 * @param files String array of files
	 * @return True when files are loaded, false if failed
	 **/
	public static boolean initFiles(String ... files) {
		String fn = "";
		try {
			for (String file : files) {
				fn = file;
				File curFile = new File(inst.getDataFolder(), file);
				if (!curFile.exists()) {
					curFile.getParentFile().mkdirs();
					copy(inst.getResource(file), curFile);
				}
			}
			return true;
		} catch (Exception e) {
			Log.warn("There was an error creating a file: " + fn);
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Loads a file into memory as a MemoryConfiguration
	 * @param file The file to load, then delete after loading
	 * @return The MemoryConfiguration resulting from loading the file
	 */
	public static final MemoryConfiguration loadResource(String file) {
		initFiles(file);
		FileConfiguration fc = loadFile(file);
		MemoryConfiguration mc = new MemoryConfiguration(fc);
		fc = null;
		deleteFile(file);
		return mc;
	}
	
	/**
	 * Loads a file from the plugin's directory
	 * @param file File to load
	 * @return The FileConfiguration that was loaded
	 */
	public static FileConfiguration loadFile(String file) {
		File f = new File(inst.getDataFolder(), file);
		FileConfiguration fc = new YamlConfiguration();
		try {
			fc.load(f);
			fc.save(f);
		} catch (Exception e) {
			Log.warn("There was an error loading file: " + f);
			e.printStackTrace();
		}
		return fc;
	}
	
	/**
	 * Loads an array of files, and stores them into a parallel array of FileConfigurations
	 * @param fileNames The list of file names to load
	 * @param files The parallel array of FileConfigurations to load the files into
	 */
	public static void loadFiles(String[] fileNames, FileConfiguration ... files) {
		for (int i = 0; i < fileNames.length; i++)
			files[i] = loadFile(fileNames[i]);
	}
	
	/**
	 * Saves the specified FileConfiguration in the plugin's directory using the file name given
	 * @param fc The data to save
	 * @param file The file to save the data to
	 */
	public static void saveFile(FileConfiguration fc, String file) {
		File f = new File(inst.getDataFolder(), file);
		try {
			fc.save(f);
		} catch (Exception e) {
			Log.warn("There was an error saving file: " + f);
			e.printStackTrace();
		}
	}
	
	/**
	 * Reloads the specified file
	 * @param fileName The file to load from disc
	 * @return Returns the FileConfiguration of the newly loaded file
	 */
	public static FileConfiguration reloadFile(String fileName) {
		File file = new File(CarbonKit.inst.getDataFolder(), fileName);
		FileConfiguration fc = YamlConfiguration.loadConfiguration(file);
		InputStream defaults = CarbonKit.inst.getResource(fileName);
		if (defaults != null) {
			YamlConfiguration def = YamlConfiguration.loadConfiguration(defaults);
			fc.setDefaults(def);
		}
		return fc;
	}
	
	private static void deleteFile(String file) {
		File f = new File(inst.getDataFolder(), file);
		if (f.exists())
			f.delete();
	}
	
	private static void copy(InputStream in, File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		byte[] buf = new byte[1024];
		int len;
		while((len=in.read(buf))>0)
			out.write(buf,0,len);
		out.flush();
		out.close();
		in.close();
	}
}
