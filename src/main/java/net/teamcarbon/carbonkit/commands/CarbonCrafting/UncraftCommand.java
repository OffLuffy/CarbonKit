package net.teamcarbon.carbonkit.commands.CarbonCrafting;

import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import net.teamcarbon.carbonkit.utils.Messages.Clr;
import net.teamcarbon.carbonkit.utils.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.List;

public class UncraftCommand extends ModuleCmd {

	public UncraftCommand(Module module) { super(module, "carbonuncraft"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) { return; }
		Player pl = (Player) sender;
		ItemStack is = pl.getInventory().getItemInMainHand();
		List<Recipe> recipes = Bukkit.getRecipesFor(is);
		for (Recipe r : new ArrayList<>(recipes)) { if (r instanceof FurnaceRecipe) recipes.remove(r); }
		sender.sendMessage(Clr.AQUA + "Found " + recipes.size() + MiscUtils.plural(recipes.size(), " recipe", " recipies") + " for " + is.getType().name());
	}
}
