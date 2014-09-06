package me.johnnywoof.spigot;

import java.util.regex.Pattern;

import me.johnnywoof.database.Database;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.ServerListPingEvent;

public class AOListener implements Listener{

	private Database db;
	
	private Pattern pat = null;
	
	private final String kick_invalid_name;
	private final String kick_not_same_ip;
	private final String kick_new_player;
	
	public AOListener(String invalid, String kick_ip, String kick_new, Database db){
		
		this.db = db;
		
		this.pat = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");//The regex to verify usernames
		
		this.kick_invalid_name = invalid;
		this.kick_not_same_ip = kick_ip;
		this.kick_new_player = kick_new;
		
	}
	
	//A high priority to allow other plugins to go first
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onServerListPingEvent(ServerListPingEvent event){
		
		if(!AlwaysOnline.mojangonline){
	
			if(AlwaysOnline.motdmes != null){
				
				event.setMotd(AlwaysOnline.motdmes);
			
			}
			
		}
		
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)//We need to go first
	public void onPlayerJoinEvent(AsyncPlayerPreLoginEvent event){
			
		if(AlwaysOnline.mojangonline){
			
			db.updatePlayer(event.getName(), event.getAddress().getHostAddress(), event.getUniqueId());
			
		}else{//Make sure we are in mojang offline mode
			
			//Verify if the name attempting to connect is even verified
			
			if(event.getName().length() > 16){
				
				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, this.kick_invalid_name);
				
				return;
				
			}else if(!this.validate(event.getName())){
				
				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, this.kick_invalid_name);
				
				return;
				
			}
			
			//Get the connecting ip
			final String ip = event.getAddress().getHostAddress();
			
			//Get last known ip
			final String lastip = db.getIP(event.getName());
			
			if(ip == null){//If null the player connecting is new
				
				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, this.kick_new_player);
				
				Bukkit.getLogger().info("[AlwaysOnline] Denied " + event.getName() + " from logging in cause their ip [" + ip + "] has never connected to this server before!");
				
				return;
				
			}else{
			
				if(ip.equals(lastip)){//If it matches set handler to offline mode, so it does not authenticate player with mojang
							
					Bukkit.getLogger().info("[AlwaysOnline] Skipping session login for player " + event.getName() + " [Connected ip: " + ip + ", Last ip: " + lastip + ", UUID: " + event.getUniqueId() + "]!");
						
					event.allow();
					
				}else{//Deny the player from joining
						
					Bukkit.getLogger().info("[AlwaysOnline] Denied " + event.getName() + " from logging in cause their ip [" + ip + "] does not match their last ip!");
						
					event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, this.kick_not_same_ip);
						
				}
			
			}
					
		}
		
	}

	  /**
	   * Validate username with regular expression
	   * @param username username for validation
	   * @return true valid username, false invalid username
	   */
	  public boolean validate(String username){
		  
		  if(this.pat == null){
			  
			  return true;
			  
		  }
		  
		  return pat.matcher(username).matches();

	  }
	
}
