package me.johnnywoof.bungeecord;

import me.johnnywoof.database.Database;
import me.johnnywoof.database.DatabaseType;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.regex.Pattern;

public class AOListener implements Listener{

	private final Database db;
	
	private final Pattern pat;
	
	private final String kick_invalid_name;
	private final String kick_not_same_ip;
	private final String kick_new_player;
	
	private AlwaysOnline ao;
	
	public AOListener(AlwaysOnline ao, String invalid, String kick_ip, String kick_new, Database db){
		
		this.db = db;
		
		if(this.db.getType() == DatabaseType.MySQL){
			
			this.ao = ao;
			
		}
		
		this.pat = Pattern.compile("^[a-zA-Z0-9_-]{1,16}$");//The regex to verify usernames
		
		this.kick_invalid_name = invalid;
		this.kick_not_same_ip = kick_ip;
		this.kick_new_player = kick_new;
		
	}
	
	//A high priority to allow other plugins to go first
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPreLogin(PreLoginEvent event){
		
		if(event.isCancelled()){return;}//Make sure it is not canceled
		
		if(!AlwaysOnline.mojangonline){//Make sure we are in mojang offline mode
			
			//Verify if the name attempting to connect is even verified
			
			if(event.getConnection().getName().length() > 16){
				
				event.setCancelReason(this.kick_invalid_name);
				
				event.setCancelled(true);
				
				return;
				
			}else if(!this.validate(event.getConnection().getName())){
				
				event.setCancelReason(this.kick_invalid_name);
				
				event.setCancelled(true);
				
				return;
				
			}
			
			//Initialize our hacky stuff
			
			InitialHandler handler = (InitialHandler) event.getConnection();
			
			//Get the connecting ip
			final String ip = handler.getAddress().getAddress().getHostAddress();
			
			//Get last known ip
			final String lastip = db.getIP(event.getConnection().getName());
			
			if(lastip == null){//If null the player connecting is new
				
				event.setCancelReason(this.kick_new_player);
				
				event.setCancelled(true);
				
				ProxyServer.getInstance().getLogger().info("Denied " + event.getConnection().getName() + " from logging in cause their ip [" + ip + "] has never connected to this server before!");
				
			}else{
			
				if(ip.equals(lastip)){//If it matches set handler to offline mode, so it does not authenticate player with mojang
						
					ProxyServer.getInstance().getLogger().info("Skipping session login for player " + event.getConnection().getName() + " [Connected ip: " + ip + ", Last ip: " + lastip + "]!");
						
					handler.setOnlineMode(false);
						
				}else{//Deny the player from joining
						
					ProxyServer.getInstance().getLogger().info("Denied " + event.getConnection().getName() + " from logging in cause their ip [" + ip + "] does not match their last ip!");
					
					handler.setOnlineMode(true);
						
					event.setCancelReason(this.kick_not_same_ip);
						
					event.setCancelled(true);
						
				}
			
			}
			
		}
		
	}
	
	//Set priority to highest to almost guaranteed to have our MOTD displayed
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPing(ProxyPingEvent event){
		
		if(!AlwaysOnline.mojangonline){
			
			if(AlwaysOnline.motdmes != null){
			
				ServerPing sp = event.getResponse();
				
				String s = AlwaysOnline.motdmes;
				
				s = s.replaceAll(".newline.", "\n");
				
				sp.setDescription(s);
				
				event.setResponse(sp);
			
			}
			
		}
		
	}
	
	@SuppressWarnings("deprecation")
	//Set priority to lowest since we'll be needing to go first
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPost(PostLoginEvent event){
		
		if(!AlwaysOnline.mojangonline){
			
			InitialHandler handler = (InitialHandler) event.getPlayer().getPendingConnection();
			
			try{
			
				UUID uuid = db.getUUID(event.getPlayer().getName());
				
				//Now here is our hacky geeky stuff
				
				Field sf = handler.getClass().getDeclaredField("uniqueId");
				sf.setAccessible(true);
				sf.set(handler, uuid);
				
				sf= handler.getClass().getDeclaredField("offlineId");
				sf.setAccessible(true);
				sf.set(handler, uuid);
				
				ProxyServer.getInstance().getLogger().info("Overriding uuid for " + event.getPlayer().getName() + " to " + uuid.toString() + "! New uuid is " + event.getPlayer().getUniqueId().toString());
			
			}catch(Exception e){//Play it safe, if an error deny the player
				
				handler.setOnlineMode(ProxyServer.getInstance().getConfig().isOnlineMode());
				
				event.getPlayer().disconnect("Sorry, the mojang servers are offline and we can't authenticate you with our own system!");
				
				ProxyServer.getInstance().getLogger().warning("Internal error for " + event.getPlayer().getName() + ", reverting to default online-mode!");
				
				e.printStackTrace();
				
			}
			
		}else{
			
			//If we are not in mojang offline mode, update the player data
			
			if(db.getType() == DatabaseType.MySQL){
				
				final String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
				final String name = event.getPlayer().getName();
				final UUID uuid = event.getPlayer().getUniqueId();
				
				if(ao != null){
					
					ao.getProxy().getScheduler().runAsync(ao, new Runnable(){

						@Override
						public void run() {
							
							db.updatePlayer(name, ip, uuid);
							
						}
						
					});
					
				}else{
					
					//Fallback method
					db.updatePlayer(event.getPlayer().getName(), event.getPlayer().getAddress().getAddress().getHostAddress(), event.getPlayer().getUniqueId());
					
				}
				
			}else{
				
				db.updatePlayer(event.getPlayer().getName(), event.getPlayer().getAddress().getAddress().getHostAddress(), event.getPlayer().getUniqueId());
				
			}
			
		}
		
	}

	  /**
	   * Validate username with regular expression
	   * @param username username for validation
	   * @return true valid username, false invalid username
	   */
	  public boolean validate(String username){
		  
		  return this.pat == null || pat.matcher(username).matches();

	  }
	
}
