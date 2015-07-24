package me.johnnywoof.databases;

import java.io.IOException;
import java.util.UUID;

public interface Database {

	String getIP(String username);

	UUID getUUID(String username);

	void updatePlayer(String username, String ip, UUID uuid);

	/**
	 * Saves the data and clears the cache.
	 * Can be safely ran asynchronously
	 */
	void flushCache() throws IOException;

}
