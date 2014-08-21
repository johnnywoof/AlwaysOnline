package me.johnnywoof.spigot;

import me.johnnywoof.database.Database;
import me.johnnywoof.database.MultiFile;
import me.johnnywoof.database.MySql;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class AlwaysOnline extends JavaPlugin{

	public static boolean mojangonline = true;
	
	public static String motdmes = "";
	
	private Database db = null;
	
	public void onEnable(){
		
		//Init the plugin
		
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
		
		this.saveDefaultConfig();
		
		FileConfiguration yml = this.getConfig();
		
		final int cm = yml.getInt("session-check-mode");
		
		if(cm == 1){
			
			this.getLogger().info("Session check mode will use mojang help support API!");
			
		}else if(cm == 2){
			
			this.getLogger().info("Session check mode will be doing direct ping tests!");
			
		}else{
			
			this.getLogger().info("Session check mode will be using xpaw!");
			
		}
		
		final String onlinemes = yml.getString("message-broadcast-online").replaceAll("&", ChatColor.COLOR_CHAR + "");
		
		final String offlinemes = yml.getString("message-broadcast-offline").replaceAll("&", ChatColor.COLOR_CHAR + "");
		
		final int ct = (yml.getInt("check-interval") * 20);
		
		if(ct < 600){
			
			this.getLogger().warning("WARNING! Your check-interval is less than 30 seconds, this can get your IP banned on various sites!");
			
		}
		
		final int dm = yml.getInt("down-amount");
		
		int id = yml.getInt("database-type");
		
		AlwaysOnline.motdmes = yml.getString("message-motd-offline");
		
		if(AlwaysOnline.motdmes.equals("null")){
			
			AlwaysOnline.motdmes = null;
			
		}else{
			
			AlwaysOnline.motdmes = AlwaysOnline.motdmes.replaceAll("&", ChatColor.COLOR_CHAR + "");
			
			AlwaysOnline.motdmes = AlwaysOnline.motdmes.replaceAll(".newline.", "\n");
			
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
		
		yml = null;
		
		if(ct == -1 || cm == -1){
			
			this.getLogger().severe("Negative number!");
			return;
			
		}
		
		yml = null;
		
		//Kill existing runnables and listeners (in case of reload)
		
		this.getServer().getScheduler().cancelTasks(this);
		
		HandlerList.unregisterAll(this);
		
		//Register our new listener and runnable
		
		this.getServer().getPluginManager().registerEvents(new AOListener(db), this);
		
		this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable(){

			int downamount = 0;
			
			@Override
			public void run() {
				
				boolean online = Utils.isMojangOnline(getServer().getName(), cm);
				
				if(!online){
					
					downamount = downamount + 1;
					
					if(downamount >= dm){
						
						if(AlwaysOnline.mojangonline){

							AlwaysOnline.mojangonline = false;
							
							SpigotNMS.setOnlineMode(false);
							
							if(!offlinemes.equals("null")){
								
								for(Player p : Bukkit.getOnlinePlayers()){//Make sure the players get it
									
									p.sendMessage(offlinemes);
									
								}
								
								getLogger().info("Mojang servers are now offline!");
								
							}
							
						}
						
					}
					
				}else{
					
					downamount = 0;
					
					if(!AlwaysOnline.mojangonline){

						AlwaysOnline.mojangonline = true;
						
						if(!SpigotNMS.setOnlineMode(true)){
							
							getLogger().severe("Failed to set online-mode back to true! Turning off server for security purposes.");
							getServer().shutdown();
							
						}
						
						if(!onlinemes.equals("null")){
							
							for(Player p : Bukkit.getOnlinePlayers()){//Make sure the players get it
								
								p.sendMessage(onlinemes);
								
							}
							
							getLogger().info("Mojang servers are now online!");
							
						}
						
					}
					
				}
				
			}
			
		}, 10, ct);
		
	}
	
	public void onDisable(){
		
		if(this.db != null){//Close that database
			
			this.db.close();
			
		}
		
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		
		if(sender.hasPermission("alwaysonline.usage")){
		
			if(args.length <= 0){
				
				this.displayHelp(sender);
				
			}else{
				
				if(args[0].equalsIgnoreCase("toggle")){
					
					AlwaysOnline.mojangonline = !AlwaysOnline.mojangonline;
					
					if(AlwaysOnline.mojangonline){
						
						if(!SpigotNMS.setOnlineMode(true)){
							
							sender.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "[Severe] Failed to set online-mode to true correctly! Please restart the server ASAP");
							this.getLogger().severe("Failed to set online-mode back to true! (Issued command by " + sender.getName());
							
						}
						
					}else{
						
						SpigotNMS.setOnlineMode(false);
					
					}
					
					sender.sendMessage(ChatColor.GOLD + "Mojang offline mode is now " + ((!AlwaysOnline.mojangonline ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled")) + ChatColor.GOLD + "!");
					
				}else if(args[0].equalsIgnoreCase("reload")){
					
					this.reload();
					
					sender.sendMessage(ChatColor.GOLD + "Configuration file has been reloaded!");
					
				}else{
					
					this.displayHelp(sender);
					
				}
				
			}
		
		}else{
			
			sender.sendMessage(ChatColor.RED + "You do not have permission to run this command.");
			
		}
		
		return true;
		
	}
	
	private void displayHelp(CommandSender sender){
		
		sender.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "----------" + ChatColor.GOLD + "[" + ChatColor.DARK_GREEN + "AlwaysOnline " + ChatColor.GRAY + this.getDescription().getVersion() + "" + ChatColor.GOLD + "]" + ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "----------");
		
		sender.sendMessage(ChatColor.GOLD + "/alwaysonline toggle - " + ChatColor.DARK_GREEN + "Toggles between mojang online mode");
		sender.sendMessage(ChatColor.GOLD + "/alwaysonline reload - " + ChatColor.DARK_GREEN + "Reloads the configuration file");
		
		sender.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "------------------------------");
		
	}
	
}
