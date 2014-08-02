package me.johnnywoof;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class AOCommand extends Command{
	
	private AlwaysOnline ao;
	
	public AOCommand(AlwaysOnline ao) {
        super("alwaysonline", "alwaysonline.usage", "ao");
        
        this.ao = ao;
        
    }
 
	public void execute(CommandSender sender, String[] args) {
    
		if(args.length <= 0){
			
			this.displayHelp(sender);
			
		}else{
			
			if(args[0].equalsIgnoreCase("toggle")){
				
				AlwaysOnline.mojangonline = !AlwaysOnline.mojangonline;
				
			if (!AlwaysOnline.mojangonline ){
				sender.sendMessage(new ComponentBuilder("Mojang offline mode is now ").color(ChatColor.GOLD).append("enabled").color(ChatColor.GREEN).append("!").color(ChatColor.GOLD).create());
				}
			else{
					
				}
				sender.sendMessage(new ComponentBuilder("Mojang offline mode is now ").color(ChatColor.GOLD).append("enabled").color(ChatColor.RED).append("!").color(ChatColor.GOLD).create());
				
			}else if(args[0].equalsIgnoreCase("reload")){
				
				ao.reload();
				
<<<<<<< HEAD
				sender.sendMessage(new ComponentBuilder("Configuration file has been reloaded!").color(ChatColor.GOLD).create());
=======
				sender.sendMessage(new ComponentBuilder("Configuration file has been reloaded!").color(ChatColor.GOLD).create();
>>>>>>> refs/remotes/origin/master
				
			}else{
				
				this.displayHelp(sender);
				
			}
			
		}
    	
    }
	
	private void displayHelp(CommandSender sender){
		
		sender.sendMessage(new ComponentBuilder("").color(ChatColor.GOLD).append("----------").color(ChatColor.STRIKETHROUGH).color(ChatColor.GOLD).append("[").color(ChatColor.GOLD).append("AlwaysOnline").color(ChatColor.DARK_GREEN).append(ao.getDescription().getVersion()).color(ChatColor.GRAY).append("").append("]").color(ChatColor.GOLD).append("").append("----------").color(ChatColor.STRIKETHROUGH).color(ChatColor.GOLD).create());
		
		sender.sendMessage(new ComponentBuilder("/alwaysonline toggle - ").color(ChatColor.GOLD).append("Toggles between mojang online mode").color( ChatColor.DARK_GREEN).create());
		sender.sendMessage(new ComponentBuilder("/alwaysonline reload - ").color(ChatColor.GOLD).append("Reloads the configuration file").color( ChatColor.DARK_GREEN).create());
		
		sender.sendMessage(new ComponentBuilder("------------------------------").color(ChatColor.GOLD).append("").color(ChatColor.STRIKETHROUGH).create());
			
	}
	
}
