package me.johnnywoof.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

public class MySql implements Database{

	/**
	 * 
	 * Cache hashmap format:
	 * <Player Name>, <Player IP~Player UUID>
	 * Where ~ is the delimiter
	 * 
	 * */
	private final HashMap<String, String> cache = new HashMap<String, String>();
	
	/**
	 * 
	 * Our mysql connection variable
	 * 
	 * */
	private Statement st = null;
	
	private String reconnectinfo = null;
	
	//I normally do not do this
	private final Logger log = Logger.getLogger("Minecraft");
	
	@Override
	public void init(String host, int port, String databasename, String username, String password) {
		
		this.reconnectinfo = host + "§" + port + "§" + databasename + "§" + username + "§" + password;//Update our string for auto reconnect
		
		try {
			
			if(this.st != null){
				
				try{
				
					if(!st.isClosed()){
						
						st.close();
						
					}
					
				}catch(SQLException ex){
					
					this.logMessage(Level.WARNING, "[AlwaysOnline] Failed to cleanly close connection to database! [" + ex.getMessage() + "]");
					
				}
				
			}
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();//Maybe I should test to see if I really need to this on every connection
			
		    Connection conn = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + databasename, username, password);  
		   
		    this.st = conn.createStatement();
		    
		    if(!this.doesTableExist("always_online")){
		    	
		    	st.executeUpdate("CREATE TABLE IF NOT EXISTS always_online (`name` varchar(16), `ip` varchar(40), `uuid` varchar(50))");
		    	st.executeUpdate("ALTER TABLE always_online ADD INDEX (`name`)");
		    	
		    }
		    
		} catch (SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override
	public UUID getUUID(String name) {
		
		if(this.cache.containsKey(name)){
			
			return UUID.fromString(this.cache.get(name).split("~")[0]);
			
		}else{
			
			try{
				
				ResultSet rs = this.st.executeQuery("SELECT * FROM always_online WHERE name = '" + name.replaceAll("'", "") + "' LIMIT 1");
				
				UUID n = null;
				String ip = null;
				
				while(rs.next()){
					
					n = UUID.fromString(rs.getString(3));
					ip = rs.getString(2);
					
				}
				
				rs.close();
				
				this.cache.put(name, n + "~" + ip);
				
				return n;
				
			}catch (CommunicationsException e){
				
				this.logMessage(Level.INFO, "Lost connection to mysql database, reconnecting! [" + e.getMessage() + "]");
				this.reconnect();
				return this.getUUID(name);//Maybe I should add a safe check for endless loop....
				
			}catch(SQLException e){
				
				e.printStackTrace();
				
			}
			
			
		}
		
		return null;
		
	}

	@Override
	public String getIP(String name) {
		
		if(this.cache.containsKey(name)){
			
			return this.cache.get(name).split("~")[1];
			
		}else{
			
			try{
				
				ResultSet rs = this.st.executeQuery("SELECT * FROM always_online WHERE name = '" + name.replaceAll("'", "") + "' LIMIT 1");
				
				UUID n = null;
				String ip = null;
				
				while(rs.next()){
					
					n = UUID.fromString(rs.getString(3));
					ip = rs.getString(2);
					
				}
				
				rs.close();
				
				this.cache.put(name, n + "~" + ip);
				
				return ip;
				
			}catch (CommunicationsException e){
				
				this.logMessage(Level.INFO, "Lost connection to mysql database, reconnecting! [" + e.getMessage() + "]");
				this.reconnect();
				return this.getIP(name);
				
			}catch(SQLException e){
				
				e.printStackTrace();
				
			}
			
		}
		
		return null;
		
	}

	@Override
	public void updatePlayer(String name, String ip, UUID uuid) {
		
		this.cache.put(name, uuid.toString() + "~" + ip);
		
		try{
			
			this.st.executeUpdate("DELETE FROM always_online WHERE name = '" + name.replaceAll("'", "") + "' LIMIT 1;");
			
			this.st.executeUpdate("INSERT INTO always_online (name, ip, uuid) VALUES ('" + name.replaceAll("'", "") + "', '" + ip.replaceAll("'", "") + "', '" + uuid.toString().replaceAll("'", "") + "');");
			
		}catch (CommunicationsException e){
			
			this.logMessage(Level.INFO, "Lost connection to mysql database, reconnecting! [" + e.getMessage() + "]");
			this.reconnect();
			this.updatePlayer(name, ip, uuid);
			
		}catch(SQLException e){
			
			e.printStackTrace();
			
		}
		
	}
	
	@Override
	public void resetCache() {
		
		this.cache.clear();
		
	}

	@Override
	public void close() {
		
		if(this.st != null){
			
			try {
				
				if(!this.st.isClosed()){
					
					this.st.close();
					
				}
				
			} catch (SQLException e) {
				
				e.printStackTrace();
				
			}
			
		}
		
	}
	
	/**
	 * 
	 * Determines if a table exists
	 * @param The table name
	 * @return If the table exists
	 * 
	 * */
	private boolean doesTableExist(String tablename) throws SQLException{
    	ResultSet rs = st.getConnection().getMetaData().getTables(null, null, tablename, null);
    	if (rs.next()) {
    		rs.close();
    	  return true;
    	}
    	rs.close();
    	return false;
    }
	
	/**
	 * 
	 * Reconnects to the database
	 * 
	 * */
	private void reconnect(){

		String[] data = this.reconnectinfo.split("§");
		
		this.init(data[0], Integer.parseInt(data[1]), data[2], data[3], data[4]);
		
	}
	
	/**
	 * 
	 * Logs a message to the console/file
	 * @param The message to log (without [AlwaysOnline])
	 * 
	 * */
	private void logMessage(Level level, String mes){
		
		log.log(level, "[AlwaysOnline] " + mes);
		
	}

	@Override
	public ArrayList<String> getDatabaseDump() {
		
		ArrayList<String> data = new ArrayList<String>();
		
		try {
			
			ResultSet rs = this.st.executeQuery("SELECT * FROM always_online;");
			
			while(rs.next()){
				
				data.add(rs.getString(1) + "§" + rs.getString(2) + "§" + rs.getString(3));
				
			}
			
			rs.close();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
		}
		
		return data;
		
	}

}
