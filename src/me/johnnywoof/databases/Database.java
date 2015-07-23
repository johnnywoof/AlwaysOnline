package me.johnnywoof.databases;

import java.io.IOException;
import java.util.UUID;

public interface Database {

	String getIP(String username);

	UUID getUUID(String username);

	void updatePlayer(String username, String ip, UUID uuid);

	void saveData() throws IOException;

	void resetCache();

}
