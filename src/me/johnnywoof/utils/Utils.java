package me.johnnywoof.utils;

import com.google.common.io.ByteStreams;

import java.io.*;
import java.net.Socket;

public class Utils {

	/**
	 *
	 * Determines if the session server is online
	 * @return If the session server is online
	 *
	 * */
	public static boolean isSessionServerOnline(){

		try{

			new Socket("sessionserver.mojang.com", 443).close();

			return true;

		}catch(IOException e){

			return false;

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
