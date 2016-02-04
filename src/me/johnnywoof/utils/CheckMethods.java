package me.johnnywoof.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class CheckMethods {

	public static boolean directSessionServerStatus(Gson gson) {

		String serverResponse;

		try {
			serverResponse = sendGet("https://sessionserver.mojang.com/");
			if (serverResponse == null)
				return false;
		} catch (IOException e) {
			return false;
		}

		Type type = new TypeToken<Map<String, String>>() {
		}.getType();
		Map<String, String> data = gson.fromJson(serverResponse, type);
		return !data.containsKey("Status") || "OK".equals(data.get("Status"));

	}

	public static boolean mojangHelpPage() {

		String serverResponse;

		try {
			serverResponse = sendGet("https://status.mojang.com/check");
			if (serverResponse == null)
				return false;
		} catch (IOException e) {
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
			if (serverResponse == null)
				return false;
		} catch (IOException e) {
			return false;
		}

		//TODO Use proper json
		return serverResponse.contains("{\"session\":{\"status\":\"up\",\"title\":\"Online\"}");

	}

	private static String sendGet(String url) throws IOException {
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		con.setRequestMethod("GET");
		con.setRequestProperty("Accept", "application/json,text/html");
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con.setRequestProperty("Connection", "close");
		con.setRequestProperty("User-Agent", "AlwaysOnline");

		int responseCode = con.getResponseCode();

		if (responseCode == 200) {

			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));

			String inputLine;
			StringBuilder response = new StringBuilder();

			while ((inputLine = in.readLine()) != null)
				response.append(inputLine);

			in.close();

			return response.toString();

		} else {
			return null;
		}
	}

}
