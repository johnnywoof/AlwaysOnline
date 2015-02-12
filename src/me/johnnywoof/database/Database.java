package me.johnnywoof.database;

import java.util.UUID;

public interface Database {

    /**
     * Initialize the database (currently mysql for now)
     *
     * @param host         The host
     * @param port         The port
     * @param databasename The database name
     * @param username     The username
     * @param password     The database password
     */
    public void init(String host, int port, String databasename, String username, String password);

    /**
     * Resets the cache in the database
     */
    public void resetCache();

    /**
     * Returns the UUID of the player name from the database
     *
     * @param name The player's name
     * @return The player's UUID from database
     */
    public UUID getUUID(final String name);

    /**
     * Returns the IP address of the player name from the database
     *
     * @param name The player's name
     * @return The player's IP from database
     */
    public String getIP(final String name);

    /**
     * Updates the player data in the database
     *
     * @param name The player's name
     * @param ip   The player's ip
     * @param uuid The player's UUID
     */
    public void updatePlayer(final String name, final String ip, final UUID uuid);

    /**
     * Closes the database
     */
    public void close();

    /**
     * Gets the type of database
     *
     * @return The type of database
     */
    public DatabaseType getType();

}
