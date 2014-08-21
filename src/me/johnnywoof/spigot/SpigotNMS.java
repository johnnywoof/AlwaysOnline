package me.johnnywoof.spigot;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SpigotNMS {
	
	//Found these two variables on the internet somewhere
	private final static String packageName = Bukkit.getServer().getClass().getPackage().getName();
	private final static String version = packageName.substring(packageName.lastIndexOf(".") + 1);
	
	/*
	 * 
	 * Forces the server to re-read the usercache.json file
	 * @return If success
	 * @deprecated Method is useless
	 * 
	 * 
	@Deprecated
	public static boolean updateCache(){
		
		//net.minecraft.server.v1_7_R4.UserCache
		
		try {
			
			Class<?> userclass = Class.forName("net.minecraft.server." + version + ".UserCache");
			
			String ver = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
			
			Class<?> c = Class.forName(ver + ".MinecraftServer");
				
			Object obj = c.getMethod("getServer").invoke(null);
			
			Object uci = userclass.getConstructor(c, File.class).newInstance(obj, new File("usercache.json"));
			
			Field sf = c.getDeclaredField("X");
			
			sf.setAccessible(true);
			
			sf.set(obj, uci);
			
			return true;
			
		} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
			e.printStackTrace();
		}
		
		return false;
		
	}*/
	
	/**
	 * 
	 * Writes the new UUID almost everywhere as possible
	 * @param The bukkit player
	 * @param The UUID
	 * @return If success
	 * 
	 * */
	public static boolean writeUUID(Player p, UUID uuid){
		
		try {
			
			//Cast the bukkit player to NMS player
			
			Class<?> craftPlayer = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
			
			Object cp = craftPlayer.cast(p);
			
			Object handle = cp.getClass().getMethod("getHandle").invoke(cp);
			
			//Write the spoof UUID
			
			Object con = handle.getClass().getField("playerConnection").get(handle);
            
            Object nm = con.getClass().getField("networkManager").get(con);
			
            Field sf = nm.getClass().getDeclaredField("spoofedUUID");
            
			sf.setAccessible(true);
			
			sf.set(nm, uuid);
			
			//Write the actual UUID into the object
			
			//Cast it to a Craft entity
			
			Class<?> craftEntity = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftEntity");
			
			Object ce = craftEntity.cast(p);
			
			//Get the NMS entity
			handle = ce.getClass().getMethod("getHandle").invoke(ce);
			
			//Set the UUID
			sf = handle.getClass().getField("uniqueID");
            
			sf.setAccessible(true);
			
			sf.set(handle, uuid);
			
			return true;
			
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		return false;
		
	}
	
	/**
	 * 
	 * @param The online mode
	 * @return If success
	 * 
	 * */
	public static boolean setOnlineMode(boolean online){
		
		if(Bukkit.getOnlineMode() == online){
			
			return true;
			
		}
		
		String ver = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		
		try {
			
			//I must say, they did a really good job attempting to prevent reflection
			//But they are no match for the master johnnywoof!
			
			//Set the vanilla online mode
			Class<?> c = Class.forName(ver + ".MinecraftServer");
			
			Object obj = c.getMethod("getServer").invoke(null);
			
			obj.getClass().getMethod("setOnlineMode", boolean.class).invoke(obj, online);
			
			//Set the craftbukkit online mode
			Object server = c.getDeclaredField("server").get(obj);
			
			Field f = server.getClass().getDeclaredField("online");
			
			f.setAccessible(true);
			
			Object wrapper = f.get(server);
			
			Field sf = wrapper.getClass().getDeclaredField("value");
			
			sf.setAccessible(true);
			
			sf.set(wrapper, online);
			
			return true;
			
		} catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | SecurityException  | NoSuchFieldException | InvocationTargetException | NoSuchMethodException e) {
			
			e.printStackTrace();
		
		}
		
		return false;
		
	}
	
}
