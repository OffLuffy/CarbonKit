package me.offluffy.carbonkit.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.utils.DuplicateModuleException.DupeType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.event.Listener;

/**
 * Abstract Module class to keep track of initialized modules and organize universal module information
 * @author Luther Langford aka OffLuffy
 */
public abstract class Module implements Listener {
	public static List<Module> modules = new ArrayList<Module>();
	private UUID id;
	private String name;
	private List<String> aliases;
	private List<ModuleCmd> modCmds;
	private boolean enabled;
	protected List<String> requires;
	
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
		this.name = name;
		this.aliases = new ArrayList<String>();
		this.requires = new ArrayList<String>();
		this.modCmds = new ArrayList<ModuleCmd>();
		for (String a : aliases)
			this.aliases.add(a.toLowerCase());
		id = UUID.randomUUID();
		if (hasListeners())
			CarbonKit.pm.registerEvents(this, CarbonKit.inst);
		setEnabled(CarbonKit.config.getBoolean("modules." + getName(), false));
		modules.add(this);
	}
	
	/*=======================[ PUBLIC ]=======================*/
	
	/**
	 * Method called when the module is initialized
	 */
	public abstract void initModule();
	
	/**
	 * Method called when the module is disabling
	 */
	public abstract void disableModule();
	
	/**
	 * @return Returns true if module needs to register listeners
	 */
	protected abstract boolean hasListeners();
	
	/**
	 * @return Returns true if all the plugins a module requires are enabled
	 */
	public abstract boolean hasDependencies();
	
	/**
	 * Disables the module and unregisters commands
	 */
	public void disable() {
		// TODO Unregister commands here
		disableModule();
	}
	
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
	 * @return Returns whether the module is enabled or not
	 */
	public boolean isEnabled() { return enabled; }
	
	/**
	 * Set the module instance enabled or disabled
	 * @param enabled Whether the module is enabled or not
	 */
	public void setEnabled(boolean enabled) { this.enabled = enabled; }
	
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
	public void addCmd(ModuleCmd command) {
		if (!modCmds.contains(command))
			modCmds.add(command);
	}
	
	/*=======================[ STATIC ]=======================*/
	
	/**
	 * Fetches a module based on its name or aliases
	 * @param query The name or alias to search for
	 * @return Returns the Module instance if found, or null if nothing is found
	 */
	public static Module getModule(String query) {
		for (Module m : modules)
			if (m.getName().equalsIgnoreCase(query))
				return m;
		for (Module m : modules)
			for (String a : m.getAliases())
				if (a.equalsIgnoreCase(query))
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
	
	/*=======================[ OVERRIDES ]=======================*/
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (obj == this) return true;
		if (!(obj instanceof Module)) return false;
		Module m = (Module)obj;
		return new EqualsBuilder()
			.append(m.getName(), name)
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
