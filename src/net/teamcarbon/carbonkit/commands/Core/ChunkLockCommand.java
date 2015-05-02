package net.teamcarbon.carbonkit.commands.Core;

import net.teamcarbon.carbonkit.modules.CoreModule;
import net.teamcarbon.carbonkit.utils.CustomMessages.CustomMessage;
import net.teamcarbon.carbonlib.MiscUtils;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import net.teamcarbon.carbonkit.utils.Module;
import net.teamcarbon.carbonkit.utils.ModuleCmd;
import org.bukkit.entity.Player;

import java.util.HashMap;

@SuppressWarnings("UnusedDeclaration")
public class ChunkLockCommand extends ModuleCmd {

	public ChunkLockCommand(Module module) { super(module, "chunklock"); }

	@Override
	public void execModCmd(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(CustomMessage.GEN_NOT_ONLINE.noPre());
			return;
		}
		if (!MiscUtils.perm(sender, "carbonkit.chunklock")) {
			sender.sendMessage(CustomMessage.GEN_NO_PERM.noPre());
			return;
		}
		Player pl = (Player) sender;
		Chunk c = pl.getLocation().getChunk();
		CoreModule.toggleLockedChunk(c);
		String cLoc = "(" + c.getX() + "," + c.getZ() + ")";
		HashMap<String, String> rep = new HashMap<String, String>();
		rep.put("{CHUNKCOORD}", cLoc);
		sender.sendMessage(MiscUtils.massReplace(CoreModule.isLockedChunk(c) ? CustomMessage.CORE_CHUNK_LOCKED.pre() : CustomMessage.CORE_CHUNK_UNLOCKED.pre(), rep));
	}

}
