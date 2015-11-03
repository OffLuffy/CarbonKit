package net.teamcarbon.carbonkit.commands.CarbonEssentials;

import net.teamcarbon.carbonkit.utils.CarbonPlayer;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Misc.TypeUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class VanishCommand extends ModuleCmd {

	public VanishCommand(Module module) { super(module, "vanish"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		Player pl = (Player) sender;
		CarbonPlayer cp = CarbonPlayer.getCarbonPlayer(pl, false);
		if (args.length > 0) {
			if (TypeUtils.isBoolean(args[0])) {
				cp.setVanish(TypeUtils.toBoolean(args[0]));
			} else {
				Player target = Bukkit.getPlayer(args[0]);
				if (target != null) {
					CarbonPlayer tcp = CarbonPlayer.getCarbonPlayer(target, false);
					if (args.length > 1) {
						if (TypeUtils.isBoolean(args[1])) { tcp.setVanish(TypeUtils.toBoolean(args[1])); }
					} else { tcp.toggleVanish(); }
					HashMap<String, String> rep = new HashMap<String, String>();
					rep.put("{TARGET}", target.getName());
					sender.sendMessage(tcp.isVanished() ? CustomMessage.CE_VANISH_OTHER.pre(rep)
							: CustomMessage.CE_UNVANISH_OTHER.pre(rep));
				} else { sender.sendMessage(CustomMessage.GEN_PLAYER_NOT_FOUND.noPre()); }
				return;
			}
		} else {
			cp.toggleVanish();
		}
		sender.sendMessage(cp.isVanished() ? CustomMessage.CE_VANISH.pre() : CustomMessage.CE_UNVANISH.pre());
	}

}
