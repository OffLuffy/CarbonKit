package me.offluffy.carbonkit.modules;

import java.util.ArrayList;
import java.util.List;

import me.offluffy.carbonkit.CarbonKit;
import me.offluffy.carbonkit.cmds.CommandCKWatcher;
import me.offluffy.carbonkit.utils.DuplicateModuleException;
import me.offluffy.carbonkit.utils.Messages;
import me.offluffy.carbonkit.utils.Module;

import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ModuleCKWatcher extends Module {
	public List<String> watchers = new ArrayList<String>();
	public ModuleCKWatcher() throws DuplicateModuleException {
		super("CkWatcher", "commandwatcher", "cwatcher", "ckwatch", "cwatch", "cw");
	}

	@Override
	public void initModule() {
		addCmd(new CommandCKWatcher(this));
		watchers = CarbonKit.config.getStringList(getName() + ".watchers");
	}

	@Override
	public void disableModule() {}

	@Override
	protected boolean hasListeners() { return true; }

	@Override
	public boolean hasDependencies() { return true; }
	
	/*=============================================================*/
	/*===[                     LISTENERS                       ]===*/
	/*=============================================================*/
	
	@EventHandler
	public void preCommandEvent(PlayerCommandPreprocessEvent e) {
		if (isEnabled()) {
			boolean show = true;
			Player sender = e.getPlayer();
			String label = e.getMessage().split(" ")[0].replace("/", "");
			if (!(sender instanceof Player))
				show = false;
			if (show && sender instanceof Player && CarbonKit.config.getStringList(getName() + ".exempt").contains(sender.getName())) {
				show = false;
			} else if (show && sender instanceof BlockCommandSender && !CarbonKit.config.getBoolean(getName() + ".watch-cmdblocks", true)) {
				show = false;
			} else if (show) {
				for (String bl : CarbonKit.config.getStringList(getName() + ".blacklist")) {
					if (label.equalsIgnoreCase(bl))
						show = false;
					else if (CarbonKit.config.getBoolean(getName() + ".match-aliases", true) && CarbonKit.inst.getServer().getPluginCommand(bl) != null) {
						List<String> aliases = CarbonKit.inst.getServer().getPluginCommand(bl).getAliases();
						if (aliases.contains(label))
							show = false;
					}
				}
			}
			
			for (String sp : watchers) {
				Player cwp = CarbonKit.inst.getServer().getPlayer(sp);
				if (cwp != null && show && sender instanceof Player && !(sender.getName().equalsIgnoreCase(cwp.getName())))
					cwp.sendMessage(Messages.Clr.PRE + "[CW] " + Messages.Clr.HEAD + ChatColor.stripColor(sender.getName()) + ": " + Messages.Clr.NORM + e.getMessage());
			}
		}
	}
	
	/*=============================================================*/
	/*===[                      METHODS                        ]===*/
	/*=============================================================*/
}
