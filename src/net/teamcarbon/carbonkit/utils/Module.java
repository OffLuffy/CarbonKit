package net.teamcarbon.carbonkit.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.CarbonKit.ConfType;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException.DupeType;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import net.teamcarbon.carbonkit.modules.CarbonCoreModule;

/**
 * Abstract Module class to keep track of initialized modules and organize universal module information
 * @author Luther Langford aka OffLuffy
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class Module implements Listener {
	public static List<Module> modules = new ArrayList<>();
	private UUID id;
	private String name;
	private List<String> aliases;
	private List<ModuleCmd> modCmds;
	private boolean enabled;
	protected List<String> requires;
	protected String reqVer = "ANY";

	/**
	 * Initializes a new Module with the give name and aliases
	 * @param name The name of the module (Used for display and fetching modules by name)
	 * @param aliases Other names or abbreviations of the plugin (Used for fetching modules by alias)
	 * @throws DuplicateModuleException Thrown if a module already exists that has the same name or any matching aliases
	 */
	public Module(String name, String ... aliases) throws DuplicateModuleException {
		if (getModule(name) != null)
			throw new DuplicateModuleException(DupeType.DUPE_NAME);
		for (String a : aliases)
			if (getModule(a) != null)
				throw new DuplicateModuleException(DupeType.DUPE_ALIAS);
		setName(name);
		this.aliases = new ArrayList<>();
		this.requires = new ArrayList<>();
		this.modCmds = new ArrayList<>();
		for (String a : aliases) this.aliases.add(a.toLowerCase());
		id = UUID.randomUUID();
		setEnabled(this instanceof CarbonCoreModule || (isConfigEnabled() && hasAllDependencies()));
		if (isConfigEnabled() && !hasAllDependencies()) {
			CarbonKit.log.warn(getName() + " module could not be enabled, server does not meet some requirements. This module requires:");
			CarbonKit.log.warn("Plugins: " + MiscUtils.stringFromArray(", ", getDependencies()));
			CarbonKit.log.warn("Required Bukkit server version: " + reqVer);
		}
		CarbonKit.getDefConfig().set("modules." + getName(), isEnabled());
		CarbonKit.inst.saveConfig();
		modules.add(this);
	}
	/*=======================[ PRIVATE ]=======================*/

	private void setName(String n) { name = n; }

	private String[] prefixPerms(String[] perms) {
		String[] prePerms = new String[perms.length];
		String mid = getName().equals("CarbonKit") ? "" : getName().toLowerCase() + ".";
		for (int i = 0; i < perms.length; i++) prePerms[i] = "carbonkit." + mid + perms[i];
		return prePerms;
	}

	/*=======================[ PROTECTED ]=======================*/

	/**
	 * Unregisters listeners associated with this Module
	 */
	protected void unregisterListeners() { if (needsListeners()) { HandlerList.unregisterAll(this); } }
	/**
	 * Registers any listeners included in this Module
	 */
	protected void registerListeners() { if (needsListeners()) { CarbonKit.pm.registerEvents(this, CarbonKit.inst); } }
	/**
	 * Adds a required plugin name to the required plugins list
	 */
	protected void addRequires(String req) { if (!requires.contains(req)) requires.add(req); }

	/*=======================[ PUBLIC ABSTRACT ]=======================*/

	/**
	 * Method called when the module is initialized
	 */
	public abstract void initModule();
	/**
	 * Method called when the module is disabling (called with plugin reloads or disables)
	 */
	public abstract void disableModule();
	/**
	 * Method called when the module is reloaded
	 */
	public abstract void reloadModule();
	/**
	 * @return Returns true if module needs to register listeners
	 */
	protected abstract boolean needsListeners();

	/*=======================[ PUBLIC ]=======================*/
	/**
	 * @return Returns true if all the plugins a module requires are enabled
	 */
	public boolean hasAllDependencies() {
		if (getDependencies().isEmpty()) return true;
		for (String p : getDependencies()) { if (!MiscUtils.checkPlugin(p, true)) return false; }
		return !reqVer.equals("ANY") && CarbonKit.NMS_VER.equalsIgnoreCase(reqVer);
	}
	/**
	 * @return Returns a List of Strings of dependency names this module requires
	 */
	public List<String> getDependencies() { return requires; }
	/**
	 * @return Returns the Module's unique identifier
	 */
	public UUID getId() { return id; }
	/**
	 * @return Returns the name of the module instance
	 */
	public String getName() { return name; }
	/**
	 * @return Returns the list of aliases for the module instance
	 */
	public List<String> getAliases() { return aliases; }
	/**
	 * @return Returns the module's enabled variable
	 */
	public boolean isEnabled() { return enabled; }
	/**
	 * @return Returns true if the module is set enabled in config, false otherwise
	 */
	public boolean isConfigEnabled() { return CarbonKit.getDefConfig().getBoolean("modules." + getName(), false); }
	/**
	 * Set the module instance enabled or disabled
	 * @param enabled Whether the module is enabled or not
	 */
	public void setEnabled(boolean enabled) {
		if (this.enabled != enabled) {
			if (enabled) {
				initModule();
			} else {
				disableModule();
				modCmds.clear();
			}
		}
		this.enabled = enabled;
		CarbonKit.getDefConfig().set("modules." + name, enabled);
		CarbonKit.saveDefConfig();
	}
	/**
	 * @return Returns the list of commands the module instance has registered
	 * @see ModuleCmd
	 */
	public List<ModuleCmd> getCommands() { return modCmds; }
	/**
	 * Registers a ModuleCmd to this module instance
	 * @param command The ModuleCmd to register
	 * @see ModuleCmd
	 */
	public void addCmd(ModuleCmd command) { if (!modCmds.contains(command)) modCmds.add(command); }
	/**
	 * @return Returns a ConcifugrationSection contains this module's config
	 */
	public ConfigurationSection getConfig() {
		ConfigurationSection confSect = CarbonKit.getDefConfig().getConfigurationSection(getName());
		if (confSect == null) confSect = new MemoryConfiguration();
		return confSect;
	}
	/**
	 * @return Returns a ConcifugrationSection contains this module's data
	 */
	public ConfigurationSection getData() {
		ConfigurationSection dataSect = CarbonKit.getConfig(ConfType.DATA).getConfigurationSection(getName());
		if (dataSect == null) dataSect = new MemoryConfiguration();
		return dataSect;
	}

	/**
	 * Fetches a message from this module's message set
	 * @param msgKey The key of the message to find
	 * @param prefix Whether or not to append the prefix to the message
	 * @return Returns a message from this module's message set or null if not found
	 */
	public String getMsg(String msgKey, boolean prefix) {
		ConfigurationSection msgSect = CarbonKit.getConfig(ConfType.MESSAGES).getConfigurationSection(getName().toLowerCase());
		if (msgSect == null) msgSect = new MemoryConfiguration();
		msgKey = msgKey.toLowerCase();
		if (msgSect.contains(msgKey) && msgSect.isString(msgKey)) {
			String msg = msgSect.getString(msgKey);
			if (prefix && msgSect.contains("prefix") && msgSect.isString("prefix")) {
				msg = msgSect.getString("prefix") + msg;
			}
			msg = Clr.trans(msg);
			return msg;
		}
		return null;
	}

	/**
	 * Fetches a message from the core message set
	 * @param msgKey The key of the message to find
	 * @param prefix Whether or not to append the prefix to the message
	 * @return Returns a message from this module's message set or null if not found
	 */
	public String getCoreMsg(String msgKey, boolean prefix) {
		ConfigurationSection msgSect = CarbonKit.getConfig(ConfType.MESSAGES).getConfigurationSection("carbonkit");
		if (msgSect == null) msgSect = new MemoryConfiguration();
		msgKey = msgKey.toLowerCase();
		if (msgSect.contains(msgKey) && msgSect.isString(msgKey)) {
			String msg = msgSect.getString(msgKey);
			if (prefix && msgSect.contains("prefix") && msgSect.isString("prefix")) {
				msg = msgSect.getString("prefix") + msg;
			}
			msg = Clr.trans(msg);
			return msg;
		}
		return null;
	}
	/**
	 * Prefixes all permissions with 'carbonkit.[modulename].' before checking.
	 * @see MiscUtils#perm(World, OfflinePlayer, String...)
	 */
	public boolean perm(World w, OfflinePlayer pl, String ... perms) { return MiscUtils.perm(w, pl, prefixPerms(perms)); }
	/**
	 * Prefixes all permissions with 'carbonkit.[modulename].' before checking.
	 * @see MiscUtils#perm(OfflinePlayer, String...)
	 */
	public boolean perm(OfflinePlayer pl, String ... perms) { return MiscUtils.perm(pl, prefixPerms(perms)); }
	/**
	 * Prefixes all permissions with 'carbonkit.[modulename].' before checking.
	 * @see MiscUtils#perm(Player, String...)
	 */
	public boolean perm(Player pl, String ... perms) { return MiscUtils.perm(pl, prefixPerms(perms)); }
	/**
	 * Prefixes all permissions with 'carbonkit.[modulename].' before checking.
	 * @see MiscUtils#perm(CommandSender, String...)
	 */
	public boolean perm(CommandSender pl, String ... perms) { return MiscUtils.perm(pl, prefixPerms(perms)); }

	/**
	 * Sends a message formatted with {@link String#format(Locale, String, Object...)} to the CommandSender if they have the specified permission
	 * @param sender The CommandSender to send the message to
	 * @param format The format that the args are applied to via {@link String#format(Locale, String, Object...)}
	 * @param perm The permission the sender must have before the message is sent. If this is null, the message is sent without checking permissions.
	 * @param args The args that are inserted into the give format via {@link String#format(Locale, String, Object...)}
	 */
	public void sendFormatted(CommandSender sender, String format, String perm, String[] args) {
		sendFormatted(sender, format, perm == null ? null : new String[]{perm}, args);
	}

	/**
	 * Sends a message formatted with {@link String#format(Locale, String, Object...)} to the CommandSender if they have any of the specified permissions
	 * @param sender The CommandSender to send the message to
	 * @param format The format that the args are applied to via {@link String#format(Locale, String, Object...)}
	 * @param perms The permissions the sender must have at least one of before the message is sent. If this is null, the message is sent without checking permissions.
	 * @param args The args that are inserted into the give format via {@link String#format(Locale, String, Object...)}
	 */
	public void sendFormatted(CommandSender sender, String format, String[] perms, String[] args) {
		if (perms == null || perm(sender, perms)) sender.sendMessage(String.format(Locale.ENGLISH, format, args));
	}
	
	/*=======================[ STATIC ]=======================*/

	/**
	 * Fetches a module based on its name or aliases
	 * @param query The name or alias to search for
	 * @return Returns the Module instance if found, or null if nothing is found
	 */
	public static Module getModule(String query) {
		for (Module m : modules)
			if (MiscUtils.eq(query, m.getName()))
				return m;
		for (Module m : modules)
			if  (MiscUtils.eq(query, m.getAliases()))
				return m;
		return null;
	}
	/**
	 * Fetches a module based on its unique identifier
	 * @param id The UUID to search for
	 * @return Returns the Module instance if found, or null if nothing is found
	 */
	public static Module getModule(UUID id) {
		for (Module m : modules)
			if (m.getId().equals(id))
				return m;
		return null;
	}

	/**
	 * @return Returns a copy of the list of Modules
	 */
	public static List<Module> getAllModules() { return new ArrayList<>(modules); }

	/**
	 * Empties static data in this class (the list of loaded modules)
	 */
	public static void flushData() {
		if (modules != null && !modules.isEmpty()) modules.clear(); else modules = new ArrayList<>();
	}

	/*=======================[ OVERRIDES ]=======================*/

	@Override
	public String toString() { return name; }
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Module)) return false;
		Module m = (Module)obj;
		return new EqualsBuilder()
				.append(m.getClass().getName(), name)
				.append(m.getId(), id)
				.isEquals();
	}
	@Override
	public int hashCode() {
		return new HashCodeBuilder()
				.append(name)
				.append(id)
				.toHashCode();
	}
}
