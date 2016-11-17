package net.teamcarbon.carbonkit.commands.CarbonSkulls;

import net.milkbowl.vault.economy.EconomyResponse;
import net.teamcarbon.carbonkit.modules.CarbonSkullsModule;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import net.teamcarbon.carbonkit.CarbonKit;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class DupeHeadCommand extends ModuleCmd {

	public DupeHeadCommand(Module module) { super(module, "dupehead"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		Player pl = (Player) sender;
		if (!mod.perm(sender, "getskull")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		double price = CarbonKit.inst().getConf().getDouble("CarbonSkulls.price", 5000.0);
		if (mod.perm(sender, "skull.free") || price < 0) price = 0;
		if (CarbonSkullsModule.hasSavedSkull(pl)) {
			if (pl.getInventory().firstEmpty() > -1) {
				if (price > 0.0) {
					if (CarbonKit.econ().has(pl, price)) {
						EconomyResponse er = CarbonKit.econ().withdrawPlayer(pl, price);
						if (!er.transactionSuccess()) {
							sender.sendMessage(CustomMessage.CS_TRANSACTION_FAILED.pre());
							return;
						}
					} else {
						sender.sendMessage(CustomMessage.CS_NOT_ENOUGH_MONEY.pre());
						return;
					}
				}
				ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1);
				skull.setDurability((short) 3);
				SkullMeta sm = (SkullMeta) skull.getItemMeta();
				sm.setOwner(CarbonSkullsModule.getSavedSkull(pl));
				skull.setItemMeta(sm);
				pl.getInventory().addItem(skull);
				HashMap<String, String> rep = new HashMap<>();
				rep.put("{SKULLOWNER}", CarbonSkullsModule.getSavedSkull(pl));
				rep.put("{PRICE}", price + "");
				if (price <= 0.0 || mod.perm(sender, "skull.free")) {
					sender.sendMessage(CustomMessage.CS_SKULL_GIVEN_FREE.pre(rep));
				} else {
					sender.sendMessage(CustomMessage.CS_SKULL_GIVEN.pre(rep));
				}
			} else {
				sender.sendMessage(CustomMessage.CS_INVENTORY_FULL.pre());
			}
		} else {
			sender.sendMessage(CustomMessage.CS_NO_SKULL_SAVED.pre());
		}
	}
}
