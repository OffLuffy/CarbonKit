package net.teamcarbon.carbonkit.utils;

import net.teamcarbon.carbonlib.Misc.MiscUtils;
import net.teamcarbon.carbonlib.Misc.TypeUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@SuppressWarnings("UnusedDeclaration")
public class CmdMask {

	public enum MaskType { NUMBER, PLAYER, WORLD, STRING }

	private List<MaskType> types = new ArrayList<>();

	public CmdMask(MaskType ... types) {
		if (types != null && types.length > 0)
			Collections.addAll(this.types, types);
	}

	public boolean matches(String ... args) {
		if ((args == null || args.length == 0) && types.size() == 0) return true;
		int i = -1;
		outer:
		for (MaskType mt : types) {
			i++;
			switch (mt) {
				case NUMBER:
					if (TypeUtils.isDouble(args[i])) { continue outer; } else { return false; }
				case PLAYER:
					if (MiscUtils.getPlayer(args[i], false) != null) { continue outer; } else { return false; }
				case WORLD:
					for (World w : Bukkit.getWorlds()) {
						if (w.getName().equalsIgnoreCase(args[i])) {
							continue outer;
						}
					}
					return false;
				case STRING: break; // Accept anything
			}
		}

		return true;
	}

}
