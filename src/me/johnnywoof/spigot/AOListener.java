package me.johnnywoof.spigot;

import java.util.UUID;
import java.util.regex.Pattern;

import me.johnnywoof.database.Database;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.server.ServerListPingEvent;

public class AOListener implements Listener{

	private Database db;
	
	private Pattern pat = null;
	
	public AOListener(Database db){
		
		this.db = db;
		
		this.pat = Pattern.compile("^[a-zA-Z0-9_]{2,16}$");//The regex to verify usernames
		
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
	public void onPlayerJoinEvent(PlayerJoinEvent event){
		
		if(AlwaysOnline.mojangonline){
			
			db.updatePlayer(event.getPlayer().getName(), event.getPlayer().getAddress().getAddress().getHostAddress(), event.getPlayer().getUniqueId());
			
		}else{//We are in mojang offline mode
			
			UUID uuid = db.getUUID(event.getPlayer().getName());
			
			if(uuid == null){//Should be impossible, rather be safe than sorry
				
				event.getPlayer().kickPlayer("Something went wrong on logging you in.");
				event.setJoinMessage(null);
				
			}
			
			if(SpigotNMS.writeUUID(event.getPlayer(), uuid)){
				
				Bukkit.getLogger().info("[AlwaysOnlineSpigot] " + event.getPlayer().getName() + " has a new UUID of " + event.getPlayer().getUniqueId().toString());
				
			}else{
				
				event.getPlayer().kickPlayer("Something went wrong on logging you in.");
				event.setJoinMessage(null);
				
			}
			
		}
		
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)//We need to go first
	public void onPlayerLoginEvent(PlayerLoginEvent event){
		
		if(!AlwaysOnline.mojangonline){//Make sure we are in mojang offline mode
			
			//Verify if the name attempting to connect is even verified
			
			if(event.getPlayer().getName().length() > 16){
				
				event.disallow(Result.KICK_OTHER, "Invalid username. Hacking?");
				
				return;
				
			}else if(!this.validate(event.getPlayer().getName())){
				
				event.disallow(Result.KICK_OTHER, "Invalid username. Hacking?");
				
				return;
				
			}
			
			//Get the connecting ip
			final String ip = event.getAddress().getHostAddress();
			
			//Get last known ip
			final String lastip = db.getIP(event.getPlayer().getName());
			
			if(ip == null){//If null the player connecting is new
				
				event.disallow(Result.KICK_OTHER, "We can not let you login because the mojang servers are offline!");
				
				Bukkit.getLogger().info("[AlwaysOnlineSpigot] Denied " + event.getPlayer().getName() + " from logging in cause their ip [" + ip + "] has never connected to this server before!");
				
				return;
				
			}else{
			
				if(ip.equals(lastip)){//If it matches set handler to offline mode, so it does not authenticate player with mojang
							
					Bukkit.getLogger().info("Skipping session login for player " + event.getPlayer().getName() + " [Connected ip: " + ip + ", Last ip: " + lastip + ", Old UUID: " + event.getPlayer().getUniqueId() + "]!");
						
					event.allow();
					
				}else{//Deny the player from joining
						
					Bukkit.getLogger().info("Denied " + event.getPlayer().getName() + " from logging in cause their ip [" + ip + "] does not match their last ip!");
						
					event.disallow(Result.KICK_OTHER, "We can't let you in since you're not on the same computer you logged on before!");
						
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
