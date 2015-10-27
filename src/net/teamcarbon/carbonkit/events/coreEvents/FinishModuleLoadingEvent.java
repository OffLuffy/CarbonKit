package net.teamcarbon.carbonkit.events.coreEvents;

import net.teamcarbon.carbonkit.utils.Module;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

/**
 * Called when the plugin initializes after all modules have finished
 * loading because the plugin delays module loading for 5 ticks. A
 * List of Modules enabled/disabled during loaded are stored in this event.
 */
@SuppressWarnings("unused")
public class FinishModuleLoadingEvent extends Event {
	List<Module> enabled, disabled;
	public FinishModuleLoadingEvent(List<Module> modulesEnabled, List<Module> modulesDisabled) {
		enabled = new ArrayList<Module>(modulesEnabled);
		disabled = new ArrayList<Module>(modulesDisabled);
	}
	public List<Module> getEnabledModules() { return new ArrayList<Module>(enabled); }
	public List<Module> getDisabledModules() { return new ArrayList<Module>(disabled); }
	private static final HandlerList handlers = new HandlerList();
	public HandlerList getHandlers() { return handlers; }
	public static HandlerList getHandlerList() { return handlers; }
}
