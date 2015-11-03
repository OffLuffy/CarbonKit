package net.teamcarbon.carbonkit.commands.CarbonSkulls;

import net.milkbowl.vault.economy.EconomyResponse;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonlib.Misc.Messages.Clr;
import net.teamcarbon.carbonlib.Misc.MiscUtils;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class UpdateHeadCommand extends ModuleCmd {

	public UpdateHeadCommand(Module module) { super(module, "updatehead"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		if (!mod.perm(sender, "update")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		double price = mod.getConfig().getDouble("update-price", 500.0);
		if (mod.perm(sender, "skull.free") || price < 0) price = 0;
		if (args.length > 0) {
			String pName = args[0];
			OfflinePlayer opl = MiscUtils.getPlayer(args[0], CarbonKit.checkOffline);
			if (opl != null) pName = opl.getName();
			if (price != 0.0 && CarbonKit.econ.has((OfflinePlayer)sender, price)) {
				if (price > 0.0) {
					EconomyResponse er = CarbonKit.econ.withdrawPlayer((OfflinePlayer) sender, price);
					if (!er.transactionSuccess()) {
						sender.sendMessage(CustomMessage.SS_TRANSACTION_FAILED.pre());
						return;
					}
				}
				ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1);
				skull.setDurability((short) 3);
				SkullMeta sm = (SkullMeta) skull.getItemMeta();
				sm.setOwner(pName);
				skull.setItemMeta(sm);
				((Player) sender).getInventory().addItem(skull);
				HashMap<String, String> rep = new HashMap<String, String>();
				rep.put("{SKULLOWNER}", pName);
				if (price <= 0.0 || mod.perm(sender, "skull.free")) {
					sender.sendMessage(CustomMessage.SS_SKULL_GIVEN_FREE.pre(rep));
				} else {
					rep.put("{PRICE}", price + "");
					sender.sendMessage(CustomMessage.SS_SKULL_GIVEN.pre(rep));
				}
			} else {
				sender.sendMessage(CustomMessage.SS_NOT_ENOUGH_MONEY.pre());
			}
		} else {
			double p = mod.getConfig().getDouble("update-price", 500.0);
			CustomMessage.printHeader(sender, "CarbonSkulls - Update");
			if (mod.perm(sender, "skull.free") || price <= 0)
				sender.sendMessage(Clr.AQUA + "/uskull <player>" + Clr.DARKAQUA + " - Set in-hand skull skin");
			else if (mod.perm(sender, "skull"))
				sender.sendMessage(Clr.AQUA + "/uskull <player>" + Clr.DARKAQUA + " - Set in-hand skull's skin " + ((price > 0)?" for $" + price:""));
			sender.sendMessage(Clr.GRAY + "Right click a skull to check who it is and save it.");
		}
	}

}