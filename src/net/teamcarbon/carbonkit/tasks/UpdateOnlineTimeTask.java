package net.teamcarbon.carbonkit.tasks;

import net.teamcarbon.carbonkit.modules.MiscModule;

import java.util.UUID;

public class UpdateOnlineTimeTask implements Runnable {
	private UUID instanceID;
	public UpdateOnlineTimeTask(UUID instanceID) {
		this.instanceID = instanceID;
	}

	@Override
	public void run() {
		if (instanceID.equals(MiscModule.instId)) { if (MiscModule.inst.isEnabled()) { MiscModule.inst.updateAllOnlineTimes(); } }
	}
}
