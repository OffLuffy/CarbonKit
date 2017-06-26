package net.teamcarbon.carbonkit.commands.CarbonTools;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.NumUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.modules.CarbonToolsModule;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class FakeQuitCommand extends ModuleCmd {
	private CarbonToolsModule modInst = CarbonToolsModule.inst;
	public FakeQuitCommand(Module module) { super(module, "fakequit"); }
	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!mod.perm(sender, "fakequit", "fakequit.others")) {
			sender.sendMessage(mod.getCoreMsg("no-perm", false));
			return ;
		}
		String user, m, me, addr = NumUtils.rand(16, 255) + "." + NumUtils.rand(16,255) + "." + NumUtils.rand(16, 255) + "." + NumUtils.rand(16,255);
		HashMap<String, String> rep = new HashMap<>();
		if (args.length > 0) {
			if (!mod.perm(sender, "fakequit.others")) {
				sender.sendMessage(mod.getCoreMsg("no-perm", false));
				return ;
			}
			Player pl = CarbonKit.inst.getServer().getPlayer(args[0]);
			if (pl != null) {
				user = pl.getName();
				if (CarbonToolsModule.addressMap.containsKey(pl.getUniqueId()))
					addr = CarbonToolsModule.addressMap.get(pl.getUniqueId());
			} else user = args[0];
		} else {
			if (!mod.perm(sender, "fakequit")) {
				sender.sendMessage(mod.getCoreMsg("no-perm", false));
				return ;
			}
			if (sender instanceof Player) {
				user = sender.getName();
				if (CarbonToolsModule.addressMap.containsKey(((Player)sender).getUniqueId()))
					addr = CarbonToolsModule.addressMap.get(((Player)sender).getUniqueId());
			} else user = "Herobrine";
		}
		rep.put("{STATUS}", "");
		rep.put("{PLAYER}", user);
		rep.put("{IP}", addr);
		m = mod.getMsg("quit-message", false, rep);
		me = mod.getMsg("quit-message-extended", false, rep);
		String statuses = "";
		for (Player opl : CarbonKit.inst.getServer().getOnlinePlayers())
			if(modInst.perm(opl, "quitmsg.extended")) opl.sendMessage(me); else opl.sendMessage(m);
	}
}
