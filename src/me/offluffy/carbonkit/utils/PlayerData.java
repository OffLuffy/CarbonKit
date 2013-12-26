package me.offluffy.carbonkit.utils;

import java.util.HashMap;

public class PlayerData {
	private static HashMap<String, int[]> coords = new HashMap<String, int[]>();
	
	public static void setCoords(String player, int x, int z) {
		coords.put(player, new int[]{x,z});
	}
	
	public static int[] getCoords(String player) {
		if (!coords.containsKey(player))
			return null;
		else
			return coords.get(player);
	}
}
