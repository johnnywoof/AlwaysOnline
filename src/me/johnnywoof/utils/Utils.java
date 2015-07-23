package me.johnnywoof.utils;

import com.google.common.io.ByteStreams;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;

public class Utils {

	/**
	 * Determines if the session server is online
	 *
	 * @return If the session server is online
	 */
	public static boolean isSessionServerOnline() {

		try {

			HttpsURLConnection connection = (HttpsURLConnection) new URL("sessionserver.mojang.com").openConnection();
			connection.setConnectTimeout(10000);
			connection.setReadTimeout(10000);
			connection.setRequestMethod("HEAD");
			int responseCode = connection.getResponseCode();
			connection.disconnect();
			return (200 <= responseCode && responseCode <= 399);

		} catch (IOException exception) {
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
