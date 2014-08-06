package me.johnnywoof.database;

import java.io.File;
import java.util.UUID;

public interface Database {
	
	/**
	 * 
	 * Initialize the database
	 * @param The configuration file
	 * 
	 * */
	public void init(File config);
	
	/**
	 * 
	 * Resets the cache in the database
	 * Note that this does not do anything at this time
	 * 
	 * */
	public void resetCache();

	/**
	 * 
	 * Returns the UUID of the player name from the database
	 * @param The player's name
	 * @return The player's UUID from database
	 * 
	 * */
	public UUID getUUID(final String name);
	
	/**
	 * 
	 * Returns the IP address of the player name from the database
	 * @param The player's name
	 * @return The player's IP from database
	 * 
	 * */
	public String getIP(final String name);
	
	/**
	 * 
	 * Updates the player data in the database
	 * @param The player's name
	 * @param The player's ip
	 * @param The player's UUID
	 * 
	 * */
	public void updatePlayer(final String name, final String ip, final UUID uuid);
	
	/**
	 * 
	 * Closes the database
	 * 
	 * */
	public void close();
	
}
