package net.teamcarbon.carbonkit.modules;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.commands.Core.CarbonReloadCommand;
import net.teamcarbon.carbonkit.commands.Core.CarbonToggleCommand;
import net.teamcarbon.carbonkit.commands.Core.ChunkLockCommand;
import net.teamcarbon.carbonkit.utils.DuplicateModuleException;
import net.teamcarbon.carbonkit.utils.Module;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class CoreModule extends Module {

	private static List<Chunk> lockedChunks;

	public CoreModule() throws DuplicateModuleException { super("CarbonKit", "ckit", "core", "ck"); }
	public static CoreModule inst;
	public void initModule() {
		inst = this;
		lockedChunks = new ArrayList<Chunk>();
		addCmd(new CarbonReloadCommand(this));
		addCmd(new CarbonToggleCommand(this));
		addCmd(new ChunkLockCommand(this));
	}
	public void disableModule() {
		for (Module m : Module.getAllModules())
			if (!(m instanceof CoreModule)) m.disableModule();
	}
	public void reloadModule() {
		CarbonKit.reloadAllConfigs();
		for (Module m : Module.getAllModules())
			if (m.isEnabled() && !(m instanceof CoreModule)) m.reloadModule();
	}
	protected boolean needsListeners() { return true; }
	public boolean hasAllDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/

	@EventHandler
	public void chunkUnload(ChunkUnloadEvent e) {
		if (getLockedChunks() != null && isLockedChunk(e.getChunk())) {
			e.setCancelled(true);
			while (!e.getChunk().load(true));
		}
	}

	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/

	public static void toggleLockedChunk(Chunk c) { setLockedChunk(c, !isLockedChunk(c)); }
	public static void setLockedChunk(Chunk c, boolean locked) {
		if (locked) addLockedChunk(c);
		else removeLockedChunk(c);
	}
	public static void addLockedChunk(Chunk c) {
		if (!lockedChunks.contains(c)) lockedChunks.add(c);
		while (!c.load(true));
	}
	public static void removeLockedChunk(Chunk c) {
		if (lockedChunks.contains(c)) lockedChunks.remove(c);
		boolean unload = true;
		for (Entity ent : c.getEntities()) if (ent.getType() == EntityType.PLAYER) unload = false;
		if (unload) c.unload();
	}
	public static boolean isLockedChunk(Chunk c) { return lockedChunks.contains(c); }
	public static List<Chunk> getLockedChunks() {
		if (lockedChunks == null) return null;
		return new ArrayList<Chunk>(lockedChunks);
	}
}
