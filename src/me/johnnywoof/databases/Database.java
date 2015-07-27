package me.johnnywoof.databases;

import java.util.UUID;

public interface Database {

	String getIP(String username);

	UUID getUUID(String username);

	/**
	 * Caches the information and updates the database.
	 * Highly recommended to run async
	 *
	 * @param username The username
	 * @param ip       The IP
	 * @param uuid     The UUID
	 */
	void updatePlayer(String username, String ip, UUID uuid);

	/**
	 * Saves the data.
	 * Can be safely ran asynchronously
	 */
	void save() throws Exception;

	/**
	 * Resets the cache
	 */
	void resetCache();

}
