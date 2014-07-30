package me.johnnywoof;

import java.lang.reflect.Field;
import java.util.UUID;
import java.util.regex.Pattern;

import me.johnnywoof.database.Database;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class AOListener implements Listener{

	private Database db;
	
	private Pattern pat = null;
	
	public AOListener(Database db){
		
		this.db = db;
		this.pat = Pattern.compile("^[a-zA-Z0-9_-]{1,16}$");
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPreLogin(PreLoginEvent event){
		
		if(event.isCancelled()){return;}
		
		if(!AlwaysOnline.mojangonline){
			
			if(event.getConnection().getName().length() > 16){
				
				event.setCancelReason("Invalid username. Hacking?");
				
				event.setCancelled(true);
				
				return;
				
			}else if(!this.validate(event.getConnection().getName())){
				
				event.setCancelReason("Invalid username. Hacking?");
				
				event.setCancelled(true);
				
				return;
				
			}
			
			InitialHandler handler = (InitialHandler) event.getConnection();
			
			final String ip = handler.getAddress().getAddress().getHostAddress();
			
			String lastip = db.getIP(event.getConnection().getName());
			
			if(ip == null){
				
				event.setCancelReason("We can not let you login because the mojang servers are offline!");
				
				event.setCancelled(true);
				
				ProxyServer.getInstance().getLogger().info("Denied " + event.getConnection().getName() + " from logging in cause their ip [" + ip + "] does not match!");
				
			}else{
			
				if(ip.equals(lastip)){
						
					ProxyServer.getInstance().getLogger().info("Skipping session login for player " + event.getConnection().getName() + "!");
						
					handler.setOnlineMode(false);
						
				}else{
						
					handler.setOnlineMode(true);
						
					event.setCancelReason("We can't let you in since you're not on the same computer you logged on before!");
						
					event.setCancelled(true);
						
				}
			
			}
			
		}
		
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPing(ProxyPingEvent event){
		
		if(!AlwaysOnline.mojangonline){
			
			if(AlwaysOnline.motdmes != null){
			
				ServerPing sp = event.getResponse();
				
				String s = AlwaysOnline.motdmes;
				
				s = s.replaceAll("&", ChatColor.COLOR_CHAR + "");
				
				s = s.replaceAll(".newline.", "\n");
				
				sp.setDescription(s);
				
				event.setResponse(sp);
			
			}
			
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPost(PostLoginEvent event){
		
		if(!AlwaysOnline.mojangonline){
			
			InitialHandler handler = (InitialHandler) event.getPlayer().getPendingConnection();
			
			try{
			
				UUID uuid = db.getUUID(event.getPlayer().getName());
				
				Field sf = handler.getClass().getDeclaredField("uniqueId");
				sf.setAccessible(true);
				sf.set(handler, uuid);
				
				sf= handler.getClass().getDeclaredField("offlineId");
				sf.setAccessible(true);
				sf.set(handler, uuid);
				
				ProxyServer.getInstance().getLogger().info("Overriding uuid for " + event.getPlayer().getName() + " to " + uuid.toString() + "! New uuid is " + event.getPlayer().getUniqueId().toString());
			
			}catch(Exception e){
				
				handler.setOnlineMode(ProxyServer.getInstance().getConfig().isOnlineMode());
				
				event.getPlayer().disconnect("Sorry, the mojang servers are offline and we can't authenticate you with our own system!");
				
				ProxyServer.getInstance().getLogger().warning("Internal error for " + event.getPlayer().getName() + ", reverting to default online-mode!");
				
				e.printStackTrace();
				
			}
			
		}else{
			
			db.updatePlayer(event.getPlayer().getName(), event.getPlayer().getAddress().getAddress().getHostAddress(), event.getPlayer().getUniqueId());
			
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
