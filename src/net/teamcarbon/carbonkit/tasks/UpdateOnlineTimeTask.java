package net.teamcarbon.carbonkit.tasks;

import net.teamcarbon.carbonkit.modules.CarbonToolsModule;

import java.util.UUID;

public class UpdateOnlineTimeTask implements Runnable {
	private UUID instanceID;
	public UpdateOnlineTimeTask(UUID instanceID) {
		this.instanceID = instanceID;
	}

	@Override
	public void run() {
		if (instanceID.equals(CarbonToolsModule.instId)) { if (CarbonToolsModule.inst.isEnabled()) { CarbonToolsModule.inst.updateAllOnlineTimes(); } }
	}
}
