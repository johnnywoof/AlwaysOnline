package me.johnnywoof.utils;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;

public class Utils {

	public static final Charset fileCharset = Charset.forName("ISO-8859-1");

	/**
	 * Determines if the session server is online
	 *
	 * @return If the session server is online
	 */
	public static boolean isSessionServerOnline() {

		try {

			HttpsURLConnection connection = (HttpsURLConnection) new URL("https://sessionserver.mojang.com").openConnection();
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

}
