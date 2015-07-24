package me.johnnywoof.databases;

import java.util.UUID;

public class PlayerData {

	public String ipAddress;
	public UUID uuid;

	public PlayerData(String ipAddress, UUID uuid) {

		this.uuid = uuid;
		this.ipAddress = ipAddress;

	}

}
