package me.johnnywoof.ao.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.*;
import java.util.List;
import java.util.Map;

public class CheckMethods {

	private static final CookieHandler COOKIE_MANAGER = new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER);

	public static boolean directSessionServerStatus(Gson gson) {

		String serverResponse;

		try {
			serverResponse = sendGet("https://sessionserver.mojang.com/");
			if (serverResponse.isEmpty())
				return false;
		} catch (IOException | URISyntaxException e) {
			return false;
		}

		Type type = new TypeToken<Map<String, String>>() {}.getType();
		try {
			Map<String, String> data = gson.fromJson(serverResponse, type);
			return !data.containsKey("Status") || "OK".equals(data.get("Status"));
		} catch (IllegalStateException e) {
			// Mojang servers are down (Thus we're not getting a JSON response)
			return false;
		}

	}

	public static boolean mojangHelpPage() {

		String serverResponse;

		try {
			serverResponse = sendGet("https://status.mojang.com/check");
			if (serverResponse.isEmpty())
				return false;
		} catch (IOException | URISyntaxException e) {
			return false;
		}

		JsonArray jsonResponse = new JsonParser().parse(serverResponse).getAsJsonArray();
		for (int i = 0; i < jsonResponse.size(); i++) {
			JsonObject status = jsonResponse.get(i).getAsJsonObject();
			if (status.get("sessionserver.mojang.com") != null) {
				return !"red".equals(status.get("sessionserver.mojang.com").getAsString());
			}
		}

		return true;

	}

	public static boolean xpaw() {

		String serverResponse;

		try {
			serverResponse = sendGet("http://xpaw.ru/mcstatus/status.json");
			if (serverResponse.isEmpty())
				return false;
			else if (serverResponse.contains("<meta http-equiv=\"Refresh\" content=\"1; URL=http://xpaw.ru/mcstatus/\">"))
				return xpaw();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			return false;
		}

		return new JsonParser().parse(serverResponse).getAsJsonObject().getAsJsonObject("report").getAsJsonObject("session").get("title").equals("Online")

	}

	private static String sendGet(String url) throws IOException, URISyntaxException {

		URL obj = new URL(url);
		URI uri = obj.toURI();

		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		con.setDefaultUseCaches(false);

		con.setRequestMethod("GET");
		con.setRequestProperty("Accept", "application/json,text/html");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con.setRequestProperty("Connection", "close");
		con.setRequestProperty("User-Agent", "AlwaysOnline");

		for (Map.Entry<String, List<String>> pair : COOKIE_MANAGER.get(uri, con.getRequestProperties()).entrySet()) {
			String key = pair.getKey();

			for (String cookie : pair.getValue())
				con.addRequestProperty(key, cookie);

		}

		InputStream serverResponseStream;

		try {
			serverResponseStream = con.getInputStream();
		} catch (IOException e) {
			serverResponseStream = con.getErrorStream();
		}

		if (serverResponseStream == null)
			return "";

		COOKIE_MANAGER.put(uri, con.getHeaderFields());

		BufferedReader in = new BufferedReader(new InputStreamReader(serverResponseStream));

		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null)
			response.append(inputLine);

		serverResponseStream.close();

		con.disconnect();

		return response.toString();

	}

}
