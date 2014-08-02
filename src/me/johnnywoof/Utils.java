package me.johnnywoof;

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

	public static boolean isMojangOnline(String sn, int mode){
		
		if(mode == 0){
			
			try{
			
				URL obj = new URL("http://status.mojang.com/check?service=session.minecraft.net");
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		 
				// optional default is GET
				con.setRequestMethod("GET");
		 
				//add request header
				con.setRequestProperty("User-Agent", "Server-" + sn);
		 
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
			
			try{
			
				URL obj = new URL("http://xpaw.ru/mcstatus/status.json");
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		 
				// optional default is GET
				con.setRequestMethod("GET");
		 
				//add request header
				con.setRequestProperty("User-Agent", "Server-" + sn);
		 
				BufferedReader in = new BufferedReader(
				        new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuffer response = new StringBuffer();
		 
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
				if(response.toString().toLowerCase().contains("\"session\":{\"status\":\"up\",\"title\":\"online\"")){
					
					return true;
					
				}else if(!response.toString().toLowerCase().contains("\"session\":{\"status\":\"problem\",\"title\":\"Quite Slow\"")){
					
					return false;
					
				}
			
			}catch(IOException e){
				
				e.printStackTrace();
				
			}
			
		}
		
		return true;
		
	}
	
	public static void saveDefaultConfig(File datafolder){
		
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
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }
		
	}
	
}
