package me.johnnywoof.bungeecord;

import me.johnnywoof.database.Database;
import me.johnnywoof.database.MultiFile;
import me.johnnywoof.database.MySql;
import me.johnnywoof.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class AlwaysOnline extends Plugin{

	public static boolean mojangonline = true;
	
	public static String motdmes = "";
	
	public static boolean debug = false;
	
	public boolean disabled = false;
	
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
		
		this.getLogger().info("Loading AlwaysOnline on bungeecord. [" + this.getProxy().getVersion() + "]");
		
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
		
			//Why is this is this not in the javadocs?
			//OR IS IT?!?!
			Configuration yml = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.getConfig());
		
			if(yml.getInt("config_version", 0) < 3){
				
				this.getLogger().warning("*-*-*-*-*-*-*-*-*-*-*-*-*-*");
				this.getLogger().warning("Your configuration file is out of date!");
				this.getLogger().warning("Please consider deleting it for a fresh new generated copy!");
				this.getLogger().warning("Once done, do /alwaysonline reload");
				this.getLogger().warning("*-*-*-*-*-*-*-*-*-*-*-*-*-*");
				return;
				
			}

			final String onlinemes = yml.getString("message-broadcast-online").replaceAll("&", String.valueOf(ChatColor.COLOR_CHAR));
			
			final String offlinemes = yml.getString("message-broadcast-offline").replaceAll("&", String.valueOf(ChatColor.COLOR_CHAR));
			
			final long ct = (yml.getLong("check-interval") * 1000);
			
			if(ct < 30000){
				
				this.getLogger().warning("Your check-interval is less than 30 seconds, please set it to a higher number!");
				
			}
			
			final int dm = yml.getInt("down-amount");
			
			int id = yml.getInt("database-type");
			
			AlwaysOnline.motdmes = yml.getString("message-motd-offline").replaceAll("&", String.valueOf(ChatColor.COLOR_CHAR));
			
			AlwaysOnline.debug = yml.getBoolean("debug");
			
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
			
			db.init(yml.getString("host"), yml.getInt("port"), yml.getString("database-name"), yml.getString("database-username"), yml.getString("database-password"));
			
			this.getLogger().info("Database is ready to go!");
			
			if(ct == -1){
				
				this.getLogger().severe("Negative number!");
				return;
				
			}
			
			//Kill existing runnables and listeners (in case of reload)
			
			this.getProxy().getScheduler().cancel(this);
			
			this.getProxy().getPluginManager().unregisterListeners(this);

			//Read the state.txt file and assign variables

			File state_file = new File(this.getDataFolder() + File.separator + "state.txt");

			if(state_file.exists()){

				Scanner scan = new Scanner(state_file);

				String data = scan.nextLine();

				scan.close();

				if(data.contains(":")){

					String[] d = data.split(":");

					this.disabled = Boolean.parseBoolean(d[0]);
					AlwaysOnline.mojangonline = Boolean.parseBoolean(d[1]);

					this.getLogger().info("Successfully loaded previous state variables!");

				}

			}

			//Register our new listener and runnable
			
			this.getProxy().getPluginManager().registerListener(this, new AOListener(this, yml.getString("message-kick-invalid"), yml.getString("message-kick-ip"), yml.getString("message-kick-new"), db));

			this.getProxy().getScheduler().runAsync(this, new Runnable(){//md_5 plz add async timer thx

				int downamount = 0;
				
				@SuppressWarnings("deprecation")
				@Override
				public void run() {
					
					while(true){
						
						if(!disabled){
							
							if(!Utils.isSessionServerOnline()){
								
								downamount = downamount + 1;
								
								if(downamount >= dm){
									
									if(AlwaysOnline.mojangonline){
	
										AlwaysOnline.mojangonline = false;
										
										if(!offlinemes.equals("null")){
											
											getProxy().broadcast(offlinemes);
											
										}
										
										getLogger().info("Mojang servers are now offline!");
										
										for(ProxiedPlayer p : getProxy().getPlayers()){
											
											if(p.hasPermission("alwaysonline.notify")){
												
												p.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_GREEN + "AlwaysOnline" + ChatColor.GOLD + "] " + ChatColor.GREEN + " Mojang servers have been detected offline!");
												
											}
											
										}
										
									}
									
								}
								
							}else{
								
								downamount = 0;
								
								if(!AlwaysOnline.mojangonline){
	
									AlwaysOnline.mojangonline = true;
									
									if(!onlinemes.equals("null")){
										
										getProxy().broadcast(onlinemes);
										
									}
									
									getLogger().info("Mojang servers are now online!");
									
									for(ProxiedPlayer p : getProxy().getPlayers()){
										
										if(p.hasPermission("alwaysonline.notify")){
											
											p.sendMessage(ChatColor.GOLD + "[" + ChatColor.DARK_GREEN + "AlwaysOnline" + ChatColor.GOLD + "] " + ChatColor.GREEN + " Mojang servers are now online!");
											
										}
										
									}
									
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
