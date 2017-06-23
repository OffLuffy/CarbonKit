package net.teamcarbon.carbonkit.utils.InvMenuUtils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@SuppressWarnings("unused")
public class OptionRightClickEvent {
	private Player player;
	private int position;
	private String name;
	private boolean close;
	private boolean destroy;
	private ItemStack item;

	public OptionRightClickEvent(Player player, int position, String name, ItemStack item) {
		this.player = player;
		this.position = position;
		this.name = name;
		this.close = true;
		this.destroy = false;
		this.item = item;
	}

	public Player getPlayer() { return player; }

	public int getPosition() { return position; }

	public String getName() { return name; }

	public boolean willClose() { return close; }

	public boolean willDestroy() { return destroy; }

	public void setWillClose(boolean close) { this.close = close; }

	public void setWillDestroy(boolean destroy) { this.destroy = destroy; }

	public ItemStack getItem() { return item; }
}
