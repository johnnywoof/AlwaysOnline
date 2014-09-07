package me.johnnywoof.spigot;

import java.io.File;
import java.io.IOException;

import me.johnnywoof.database.Database;
import me.johnnywoof.database.MultiFile;
import me.johnnywoof.database.MySql;
import me.johnnywoof.utils.Utils;
import me.johnnywoof.utils.XpawManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public class AlwaysOnline extends JavaPlugin{

	public static boolean mojangonline = true;
	
	public static String motdmes = "";
	
	private Database db = null;
	
	private XpawManager xm = null;
	
	public boolean disabled = false;
	
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
		
		this.getLogger().info("Loading AlwaysOnline on spigot. [" + this.getServer().getVersion() + "]");
		
		/*String version = System.getProperty("java.version");
		
	    int pos = 0, count = 0;
	    
	    for ( ; pos<version.length() && count < 2; pos ++) {
	    	
	        if (version.charAt(pos) == '.') count ++;
	        
	    }
	    
	    double jv = Double.parseDouble (version.substring (0, pos));
	    
	    if(jv >= 1.7){
	    
	    	this.getLogger().info("Detected java version is OK: " + jv);
	    
	    }else{
	    	
	    	this.getLogger().warning("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
			this.getLogger().warning("Java 7 is required to run AlwaysOnline in Spigot!");
			this.getLogger().warning("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
			this.getServer().getPluginManager().disablePlugin(this);
			return;
	    	
	    }*/
		
		if(this.isBungeecordEnabled()){
			
			this.getLogger().warning("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
			this.getLogger().warning("You have bungeecord enabled.");
			this.getLogger().warning("Please install AlwaysOnline as a bungeecord plugin");
			this.getLogger().warning("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
			this.getServer().getPluginManager().disablePlugin(this);
			return;
			
		}
		
		//I NEED A INITUUIDEVENT!!
		if(org.spigotmc.SpigotConfig.saveUserCacheOnStopOnly){
			
			this.getLogger().warning("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
			this.getLogger().warning("You have saveUserCacheOnStopOnly set to true.");
			this.getLogger().warning("Please set it to false for this plugin to work");
			this.getLogger().warning("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
			this.getServer().getPluginManager().disablePlugin(this);
			return;
			
		}
		
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
			
			//Set the xm field
			this.xm = null;
			
			this.getLogger().info("Getting HTTP cookies for xpaw...");
				
			this.xm = new XpawManager(yml.getBoolean("offline-quite-slow"));
			
			this.getLogger().info("Finished getting cookies!");
			
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
		
		if(ct == -1 || cm == -1){
			
			this.getLogger().severe("Negative number!");
			return;
			
		}
		
		//Kill existing runnables and listeners (in case of reload)
		
		this.getServer().getScheduler().cancelTasks(this);
		
		HandlerList.unregisterAll(this);
		
		//Register our new listener and runnable
		
		this.getServer().getPluginManager().registerEvents(new AOListener(yml.getString("message-kick-invalid"), yml.getString("message-kick-ip"), yml.getString("message-kick-new"), db), this);
		
		yml = null;
		
		this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable(){

			int downamount = 0;
			
			@Override
			public void run() {
				
				if(!disabled){
				
					boolean online;
					
					if(cm == 1 || cm == 2){
						
						online = Utils.isMojangOnline(getServer().getName(), cm);
						
					}else{
							
						online = xm.isXpawClaimingOnline();
						
					}
					
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
					
				}else if(args[0].equalsIgnoreCase("disable")){
					
					this.disabled = true;
					
					sender.sendMessage(ChatColor.GOLD + "AlwaysOnline has been disabled! AlwaysOnline will no longer check to see if the session server is offline.");
					
				}else if(args[0].equalsIgnoreCase("enable")){
					
					this.disabled = false;
					
					sender.sendMessage(ChatColor.GOLD + "AlwaysOnline has been enabled! AlwaysOnline will now check to see if the session server is offline.");
					
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
	
	private boolean isBungeecordEnabled(){
		
		File config = new File("spigot.yml");
		
		if(config.exists()){
			
			try{
				
				YamlConfiguration yml = new YamlConfiguration();
				
				yml.load(config);
				
				return yml.getBoolean("settings.bungeecord");
				
			}catch(IOException | InvalidConfigurationException e){
				
				e.printStackTrace();
				
			}
			
		}
		
		config = null;
		
		return false;
		
	}
	
	private void displayHelp(CommandSender sender){
		
		sender.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "----------" + ChatColor.GOLD + "[" + ChatColor.DARK_GREEN + "AlwaysOnline " + ChatColor.GRAY + this.getDescription().getVersion() + "" + ChatColor.GOLD + "]" + ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "----------");
		
		sender.sendMessage(ChatColor.GOLD + "/alwaysonline toggle - " + ChatColor.DARK_GREEN + "Toggles between mojang online mode");
		sender.sendMessage(ChatColor.GOLD + "/alwaysonline enable - " + ChatColor.DARK_GREEN + "Enables the plugin");
		sender.sendMessage(ChatColor.GOLD + "/alwaysonline disable - " + ChatColor.DARK_GREEN + "Disables the plugin");
		sender.sendMessage(ChatColor.GOLD + "/alwaysonline reload - " + ChatColor.DARK_GREEN + "Reloads the configuration file");
		
		sender.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "------------------------------");
		
	}

	/*
	 * 
	 * Gets the entire dump of the database and writes it to the usercache.json
	 * 
	 *
	private void writeCache() throws IOException, ParseException{
		
		File servercache = new File("usercache.json");
		
		if(servercache.exists()){
			
			servercache.delete();
			
		}
		
		//AHA OUR OWN JSON
		PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(servercache, true)));
		
		w.println("[");
		
		int index = 0;
		
		ArrayList<String> data = db.getDatabaseDump();
		
		int size = data.size() - 1;
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTime(new Date());
		
		calendar.add(2, 1);
		
		String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(calendar.getTime());
		
		for(String l : data){
			
			String[] d = l.split("§");
			
			if(index == size || index == 0){
			
				w.println("{\"name\":\"" + d[0] + "\",\"uuid\":\"" + d[2] + "\",\"expiresOn\":\"" + date + "\"}");
			
			}else{
				
				w.println(",{\"name\":\"" + d[0] + "\",\"uuid\":\"" + d[2] + "\",\"expiresOn\":\"" + date + "\"}");
				
			}
			
			index++;
			
		}
		
		data.clear();
		
		w.println("]");
		
		w.close();
		
		w = null;
		
		SpigotNMS.updateCache();
		
	}*/
	
}
