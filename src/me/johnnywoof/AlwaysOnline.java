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
	
	public void onEnable(){
		
		if(!this.getDataFolder().exists()){
			
			this.getDataFolder().mkdir();
			
		}
		
		if(!this.getConfig().exists()){
			
			Utils.saveDefaultConfig(this.getDataFolder());
			
		}
		
		Database db = null;
		
		try{
		
			Configuration yml = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.getConfig());
		
			final int cm = yml.getInt("session-check-mode");
			
			final String onlinemes = yml.getString("message-broadcast-online");
			
			final String offlinemes = yml.getString("message-broadcast-offline");
			
			final long ct = (yml.getLong("check-interval") * 1000);
			
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
			
			this.getProxy().getPluginManager().registerListener(this, new AOListener(db));
			
			this.getProxy().getScheduler().runAsync(this, new Runnable(){

				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					
					while(true){
							
						boolean online = Utils.isMojangOnline(getProxy().getName(), cm);
						
						if(online){
							
							if(!AlwaysOnline.mojangonline){

								AlwaysOnline.mojangonline = true;
								
								if(!onlinemes.equals("null")){
									
									getProxy().broadcast(onlinemes.replaceAll("&", ChatColor.COLOR_CHAR + ""));
									
									getLogger().info("Mojang servers are now online!");
									
								}
								
							}
							
						}else{
							
							if(AlwaysOnline.mojangonline){

								AlwaysOnline.mojangonline = false;
								
								if(!offlinemes.equals("null")){
									
									getProxy().broadcast(offlinemes.replaceAll("&", ChatColor.COLOR_CHAR + ""));
									getLogger().info("Mojang servers are now offline!");
									
								}
								
							}
							
						}
						
						try {
							
							Thread.sleep(ct);
							
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					}
					
				}
				
			});
			
		}catch(IOException e){
			
			e.printStackTrace();
			
		}
		
	}
	
	public void onDisable(){
		
		
		
	}
	
	private File getConfig(){
		
		return new File(this.getDataFolder() + File.separator + "config.yml");
		
	}
	
}
