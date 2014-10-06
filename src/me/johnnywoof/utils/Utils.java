package me.johnnywoof.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;

import com.google.common.io.ByteStreams;

public class Utils {
	
	/**
	 * 
	 * Returns if mojang session servers are online
	 * XPAW IS NOT SUPPORTED IN THIS METHOD
	 * @param The server name
	 * @param The session check mode
	 * @return If mojang servers are online
	 * 
	 * */
	public static boolean isMojangOnline(String sn, int mode){
		
		if(mode == 1){
			
			try{
			
				URL obj = new URL("http://status.mojang.com/check?service=session.minecraft.net");
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		 
				// optional default is GET
				con.setRequestMethod("GET");
				
				con.setRequestProperty("Content-Type", "application/json");
		 
				//add request header
				con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2");
		 
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
				String res = response.toString();
				
				if(res.toLowerCase().contains("red")){
					
					return false;
					
				}else{
					
					return true;
					
				}
			
			}catch(IOException e){
				
				e.printStackTrace();
				return false;
				
			}
			
		}else if(mode == 2){
			
			try {
				
				return InetAddress.getByName("session.minecraft.net").isReachable(15000);
				
			} catch (IOException e) {
				
				e.printStackTrace();
				return false;
				
			}
			
		}else{
			
			throw new IllegalArgumentException("Mode \"" + mode + "\" is not valid.");
			
		}
		
	}
	
	/**
	 * 
	 * Saves the default plugin configuration file from the jar
	 * @param The datafolder
	 * 
	 * */
	public static void saveDefaultConfig(File datafolder){
		
		//Was about to say "Make this method work with Java 6"....but realized bungeecord required Java 7 to run!
		
		if (!datafolder.exists()) {
            datafolder.mkdir();
        }
        File configFile = new File(datafolder, "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream is = Utils.class.getResourceAsStream("/config.yml");
                     OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		
	}
	
}
