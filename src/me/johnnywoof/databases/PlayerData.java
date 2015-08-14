package me.johnnywoof.databases;

import com.google.common.base.Preconditions;

import java.util.UUID;

public class PlayerData {

	public String ipAddress;
	public UUID uuid;

	public PlayerData(String ipAddress, UUID uuid) {
		Preconditions.checkNotNull(uuid);
		this.uuid = uuid;
		this.ipAddress = ipAddress;
	}

	@Override
	public boolean equals(Object o) {
		return o == this || (o instanceof PlayerData && ((PlayerData) o).uuid.equals(this.uuid));
	}

}
