package me.johnnywoof.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class MySql implements Database{

	private final HashMap<String, String> cache = new HashMap<String, String>();
	
	private Statement st = null;
	
	@Override
	public void init(File config) {
		
		try {
			
			Configuration yml = ConfigurationProvider.getProvider(YamlConfiguration.class).load(config);
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			
		    Connection conn = DriverManager.getConnection("jdbc:mysql://" + yml.getString("host") + ":" + yml.getInt("port") + "/" + yml.getString("database-name") + "?autoReconnect=true&useUnicode=yes", yml.getString("database-username"), yml.getString("database-password"));  
		   
		    this.st = conn.createStatement();
		    
		    if(!this.doesTableExist("always_online")){
		    	
		    	st.executeUpdate("CREATE TABLE IF NOT EXISTS always_online (`name` varchar(16), `ip` varchar(40), `uuid` varchar(50))");
		    	st.executeUpdate("ALTER TABLE always_online ADD INDEX (`name`)");
		    	
		    }
			
		    yml = null;
		    
		} catch (IOException | SQLException | InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			
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
			
			this.st.executeUpdate("DELETE FROM always_online WHERE name = '" + name.replaceAll("'", "") + "';");
			
			this.st.executeUpdate("INSERT INTO always_online (name, ip, uuid) VALUES ('" + name.replaceAll("'", "") + "', '" + ip.replaceAll("'", "") + "', '" + uuid.toString().replaceAll("'", "") + "');");
			
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
	
	private boolean doesTableExist(String tablename) throws SQLException{
    	ResultSet rs = st.getConnection().getMetaData().getTables(null, null, tablename, null);
    	if (rs.next()) {
    		rs.close();
    	  return true;
    	}
    	rs.close();
    	return false;
    }

}
