package net.teamcarbon.carbonkit.commands.Misc;

import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.teamcarbon.carbonkit.modules.MiscModule;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.MiscUtils;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class FakeQuitCommand extends ModuleCmd {
	public FakeQuitCommand(Module module) { super(module, "fakequit"); }
	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!MiscUtils.perm(sender, "carbonkit.misc.fakequit") && !MiscUtils.perm(sender, "carbonkit.misc.fakequit.others")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return ;
		}
		String user, m, me, addr = MiscUtils.rand(16,255) + "." + MiscUtils.rand(16,255) + "." + MiscUtils.rand(16, 255) + "." + MiscUtils.rand(16,255);
		HashMap<String, String> rep = new HashMap<String, String>();
		if (args.length > 0) {
			if (!MiscUtils.perm(sender, "carbonkit.misc.fakequit.others")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return ;
			}
			Player pl = CarbonKit.inst.getServer().getPlayer(args[0]);
			if (pl != null) {
				user = pl.getName();
				if (MiscModule.addressMap.containsKey(pl.getUniqueId()))
					addr = MiscModule.addressMap.get(pl.getUniqueId());
			} else user = args[0];
		} else {
			if (!MiscUtils.perm(sender, "carbonkit.misc.fakequit")) {
				sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
				return ;
			}
			if (sender instanceof Player) {
				user = sender.getName();
				if (MiscModule.addressMap.containsKey(((Player)sender).getUniqueId()))
					addr = MiscModule.addressMap.get(((Player)sender).getUniqueId());
			} else user = "Herobrine";
		}
		rep.put("{STATUS}", "");
		rep.put("{PLAYER}", user);
		rep.put("{IP}", addr);
		m = MiscUtils.massReplace(CustomMessage.MISC_QUIT.noPre(), rep);
		me = MiscUtils.massReplace(CustomMessage.MISC_QUIT_EXT.noPre(), rep);
		Player p = (Player)sender;
		String statuses = "";
		for (Player opl : CarbonKit.inst.getServer().getOnlinePlayers())
			if(MiscUtils.perm(opl, "carbonkit.misc.quitmsg.extended")) opl.sendMessage(me); else opl.sendMessage(m);
	}
}
