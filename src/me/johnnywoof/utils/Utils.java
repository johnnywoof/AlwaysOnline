package me.johnnywoof.utils;

import com.google.common.io.ByteStreams;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Utils {

	/**
	 * Determines if the session server is online
	 *
	 * @return If the session server is online
	 */
	public static boolean isSessionServerOnline() {

		try {

			//Re-use the socket instance?
			//Actually it might not be possible. At least we are closing it.

			Socket socket = new Socket();

			socket.connect(new InetSocketAddress("sessionserver.mojang.com", 443), 10000);

			socket.close();

			return true;

		} catch (IOException e) {

			return false;

		}

	}

	/**
	 * Saves the default plugin configuration file from the jar
	 *
	 * @param datafolder The plugin data folder
	 */
	public static void saveDefaultConfig(File datafolder) {

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
