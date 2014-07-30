package me.johnnywoof.database;

import java.io.File;
import java.util.UUID;

public interface Database {
	
	public void init(File config);
	
	public void resetCache();

	public UUID getUUID(final String name);
	
	public String getIP(final String name);
	
	public void updatePlayer(final String name, final String ip, final UUID uuid);
	
	public void close();
	
}
