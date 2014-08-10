package me.johnnywoof;

import java.io.File;
import java.io.IOException;

import me.johnnywoof.database.Database;
import me.johnnywoof.database.MultiFile;
import me.johnnywoof.database.MySql;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class AlwaysOnline extends Plugin{

	public static boolean mojangonline = true;
	
	public static String motdmes = "";
	
	private Database db = null;
	
	public void onEnable(){
		
		//Register our command
		this.getProxy().getPluginManager().registerCommand(this, new AOCommand(this));
		
		this.reload();
		
	}
	
	/**
	 * 
	 * Reloads the plugin
	 * 
	 * */
	public void reload(){
		
		if(this.db != null){//Close existing open database connections on reload
			
			this.db.close();
			this.db = null;
			
		}
		
		if(!this.getDataFolder().exists()){
			
			this.getDataFolder().mkdir();
			
		}
		
		if(!this.getConfig().exists()){
			
			Utils.saveDefaultConfig(this.getDataFolder());
			
		}
		
		try{
		
			Configuration yml = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.getConfig());
		
			final int cm = yml.getInt("session-check-mode");
			
			if(cm == 1){
				
				this.getLogger().info("Session check mode will use mojang help support API!");
				
			}else if(cm == 2){
				
				this.getLogger().info("Session check mode will be doing direct ping tests!");
				
			}else{
				
				this.getLogger().info("Session check mode will be using xpaw!");
				
			}
			
			final String onlinemes = yml.getString("message-broadcast-online");
			
			final String offlinemes = yml.getString("message-broadcast-offline");
			
			final long ct = (yml.getLong("check-interval") * 1000);
			
			if(ct < 30000){
				
				this.getLogger().warning("WARNING! Your check-interval is less than 30 seconds, this can get your IP banned on various sites!");
				
			}
			
			final int dm = yml.getInt("down-amount");
			
			int id = yml.getInt("database-type");
			
			AlwaysOnline.motdmes = yml.getString("message-motd-offline");
			
			if(AlwaysOnline.motdmes.equals("null")){
				
				AlwaysOnline.motdmes = null;
				
			}
			
			if(id == 2){
				
				db = new MySql();
				this.getLogger().info("Using a mysql database style!");

			}else{
				
				db = new MultiFile();
				this.getLogger().info("Using a multifile database style!");
				
			}
			
			db.init(this.getConfig());
			
			this.getLogger().info("Database is ready to go!");
			
			yml = null;
			
			if(ct == -1 || cm == -1){
				
				this.getLogger().severe("Negative number!");
				return;
				
			}
			
			//Kill existing runnables and listeners (in case of reload)
			
			this.getProxy().getScheduler().cancel(this);
			
			this.getProxy().getPluginManager().unregisterListeners(this);
			
			//Register our new listener and runnable
			
			this.getProxy().getPluginManager().registerListener(this, new AOListener(db));
			
			this.getProxy().getScheduler().runAsync(this, new Runnable(){//md_5 plz add async timer thx

				int downamount = 0;
				
				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					
					while(true){
							
						boolean online = Utils.isMojangOnline(getProxy().getName(), cm);
						
						if(!online){
							
							downamount = downamount + 1;
							
							if(downamount >= dm){
								
								if(AlwaysOnline.mojangonline){

									AlwaysOnline.mojangonline = false;
									
									if(!offlinemes.equals("null")){
										
										getProxy().broadcast(offlinemes.replaceAll("&", ChatColor.COLOR_CHAR + ""));
										getLogger().info("Mojang servers are now offline!");
										
									}
									
								}
								
							}
							
						}else{
							
							downamount = 0;
							
							if(!AlwaysOnline.mojangonline){

								AlwaysOnline.mojangonline = true;
								
								if(!onlinemes.equals("null")){
									
									getProxy().broadcast(onlinemes.replaceAll("&", ChatColor.COLOR_CHAR + ""));
									
									getLogger().info("Mojang servers are now online!");
									
								}
								
							}
							
						}
						
						try {
							
							Thread.sleep(ct);
							
						} catch (InterruptedException e) {
							
							//Removed to prevent confused errors
							
						}
						
					}
					
				}
				
			});
			
		}catch(IOException e){
			
			e.printStackTrace();
			
		}
		
	}
	
	public void onDisable(){
		
		if(this.db != null){//Close that database
			
			this.db.close();
			
		}
		
	}
	
	/**
	 * 
	 * Generates a file object for the config file
	 * @return The config file object
	 * 
	 * */
	private File getConfig(){
		
		return new File(this.getDataFolder() + File.separator + "config.yml");
		
	}
	
}
