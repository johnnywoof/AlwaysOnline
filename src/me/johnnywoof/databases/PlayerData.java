package me.johnnywoof.databases;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class PlayerData {

	public String ipAddress;
	public UUID uuid;

	public PlayerData(ProxiedPlayer proxiedPlayer) {

		this.uuid = proxiedPlayer.getUniqueId();
		this.ipAddress = proxiedPlayer.getAddress().getAddress().getHostAddress();

	}

	public PlayerData(String ipAddress, UUID uuid) {

		this.uuid = uuid;
		this.ipAddress = ipAddress;

	}

}
