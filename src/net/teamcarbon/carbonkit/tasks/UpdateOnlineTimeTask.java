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
		if (CarbonToolsModule.inst.isEnabled() && instanceID.equals(CarbonToolsModule.instId)) {
			CarbonToolsModule.inst.updateAllOnlineTimes();
		}
	}
}
