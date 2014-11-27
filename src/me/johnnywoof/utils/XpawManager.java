package me.johnnywoof.utils;

import me.johnnywoof.bungeecord.AlwaysOnline;
import net.md_5.bungee.api.ProxyServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

public class XpawManager {

	private final String AGENT;

	private boolean fire_on_slow;

	public XpawManager(boolean fire_on_slow) {

		long start = System.currentTimeMillis();

		this.fire_on_slow = fire_on_slow;

		this.AGENT = generateRandomAgent();

		ProxyServer
				.getInstance()
				.getLogger()
				.info("[AlwaysOnline] Randomly selected user-agent is \""
						+ this.AGENT + "\"!");

		// Hope this doesn't conflict with other plugins...which it should
		// not...
		// Since most plugins don't care about http cookies

		CookieManager cm = new CookieManager();

		cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

		CookieHandler.setDefault(cm);

		// Send the get request to the xpaw homepage to generate the cookie

		URL obj;
		HttpURLConnection con;

		try {

			obj = new URL("http://xpaw.ru/mcstatus/");
			con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			// add request header
			con.setRequestProperty("User-Agent", AGENT);

			con.connect();

			int code = con.getResponseCode();

			if (code != 200) {

				ProxyServer
						.getInstance()
						.getLogger()
						.warning(
								"[AlwaysOnline] xpaw returned http code "
										+ code
										+ " for http://xpaw.ru/mcstatus/!");

			}

			if (AlwaysOnline.debug) {

				for (Entry<String, List<String>> en : con
						.getRequestProperties().entrySet()) {

					System.out.println(en.getKey() + ": "
							+ en.getValue().toString());

				}

			}

			con.disconnect();

			if (AlwaysOnline.debug) {

				System.out.println("HTTP Code: " + code);

				for (HttpCookie c : cm.getCookieStore().getCookies()) {

					System.out.println("Detected cookie: " + c.toString());

				}

			}

		} catch (IOException e) {

			e.printStackTrace();

		}

		ProxyServer
				.getInstance()
				.getLogger()
				.info("[AlwaysOnline] Finished loading in "
						+ (System.currentTimeMillis() - start)
						+ " milliseconds!");

	}

	public boolean isXpawClaimingOnline() {

		try {

			URL obj = new URL("http://xpaw.ru/mcstatus/status.json");
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();

			// optional default is GET
			con.setRequestMethod("GET");

			// xpaw, y u make it so complex >.>

			con.setRequestProperty("X-Requested-with", "XMLHttpRequest");

			con.setRequestProperty("Referer", "http://xpaw.ru/mcstatus/");

			con.setRequestProperty("Accept-Language", "en-us");

			con.setRequestProperty("User-Agent", AGENT);

			con.setRequestProperty("Content-Type", "text/html");// Or maybe
																// application/json

			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();

			con.disconnect();

			String res = response.toString().toLowerCase();

			if (AlwaysOnline.debug) {

				System.out.println("Response from server: " + res);

			}

			if (res.contains("\"session\":{\"status\":\"up\",\"title\":\"online\"")) {

				return true;

			} else if (res.contains("\"session\":{\"status\":\"problem\",\"title\":\"Quite Slow\"")) {

				return !this.fire_on_slow;

			} else {

				return false;

			}

		} catch (IOException e) {

			e.printStackTrace();

		}

		return true;

	}

	// TODO If anyone can make this better, I'll appreciate it
	private static String generateRandomAgent() {

		Random rand = new Random();

		String agent = null;

		int n = rand.nextInt(100);

		String browser = "firefox";
		String os = "win";

		if (n <= 34) {

			browser = "chrome";

			n = rand.nextInt(100);

			if (n <= 89) {

				os = "win";

			} else if (n <= 98) {

				os = "mac";

			} else {

				os = "lin";

			}

		} else if (n <= 66) {

			browser = "iexplorer";
			os = "win";

		} else if (n <= 91) {

			browser = "firefox";

			n = rand.nextInt(100);

			if (n <= 83) {

				os = "win";

			} else if (n <= 99) {

				os = "mac";

			} else {

				os = "lin";

			}

		} else if (n <= 98) {

			browser = "safari";
			os = "mac";

		} else {

			browser = "opera";

			n = rand.nextInt(100);

			if (n <= 91) {

				os = "win";

			} else if (n <= 97) {

				os = "mac";

			} else {

				os = "lin";

			}

		}

		String temp = null;

		String version = null;

		switch (browser) {
		case "firefox":

			n = (rand.nextInt((3 - 0) + 1) + 0);

			long x = 1262322000;// Number between 1262322000 and System.currentTimeMillis()

			switch (n) {
			case 0:

				version = "Gecko/"
						+ (x + ((long) (rand.nextDouble() * (System
								.currentTimeMillis() - x)))) + " Firefox/"
						+ (rand.nextInt((7 - 5) + 1) + 5) + ".0";

				break;
			case 1:

				version = "Gecko/"
						+ (x + ((long) (rand.nextDouble() * (System
								.currentTimeMillis() - x)))) + " Firefox/"
						+ (rand.nextInt((7 - 5) + 1) + 5) + ".0.1";

				break;
			case 2:

				version = "Gecko/"
						+ (x + ((long) (rand.nextDouble() * (System
								.currentTimeMillis() - x)))) + " Firefox/3.6."
						+ (rand.nextInt((20 - 1) + 1) + 1);

				break;
			default:

				version = "Gecko/"
						+ (x + ((long) (rand.nextDouble() * (System
								.currentTimeMillis() - x)))) + " Firefox/3.8";
				break;
			}

			switch (os) {
			case "lin":
				temp = "(X11; Linux {proc}; rv:"
						+ (rand.nextInt((7 - 5) + 1) + 5) + ".0) " + version;
				break;
			case "mac":
				temp = "(Macintosh; {proc} Mac OS X "
						+ ("10_" + (rand.nextInt((7 - 5) + 1) + 5) + "_" + (rand
								.nextInt((9 - 0) + 1) + 0)) + " rv:"
						+ (rand.nextInt((6 - 2) + 1) + 2) + ".0) " + version;
				break;
			default:
				temp = "(Windows NT "
						+ ((rand.nextInt((6 - 5) + 1) + 5) + "." + (rand
								.nextInt((1 - 0) + 1) + 0))
						+ "; {lang}; rv:1.9." + (rand.nextInt((2 - 0) + 1) + 0)
						+ ".20) " + version;
				break;
			}

			agent = "Mozilla/5.0 " + temp;
			break;
		case "safari":

			String saf = (rand.nextInt((535 - 531) + 1) + 531) + "."
					+ (rand.nextInt((50 - 1) + 1) + 1) + "."
					+ (rand.nextInt((7 - 1) + 1) + 1);

			if (rand.nextBoolean()) {

				version = (rand.nextInt((5 - 4) + 1) + 4) + "."
						+ (rand.nextInt((1 - 0) + 1) + 0);

			} else {

				version = (rand.nextInt((5 - 4) + 1) + 4) + ".0."
						+ (rand.nextInt((5 - 1) + 1) + 1);

			}

			switch (os) {
			case "mac":
				temp = "(Macintosh; U; {proc} Mac OS X "
						+ ("10_" + (rand.nextInt((7 - 5) + 1) + 5) + "_" + (rand
								.nextInt((9 - 0) + 1) + 0)) + " rv:"
						+ (rand.nextInt((6 - 2) + 1) + 2)
						+ ".0; {lang}) AppleWebKit/" + saf
						+ " (KHTML, like Gecko) Version/" + version
						+ " Safari/" + saf;
				break;
			default:
				temp = "(Windows; U; Windows NT "
						+ ((rand.nextInt((6 - 5) + 1) + 5) + "." + (rand
								.nextInt((1 - 0) + 1) + 0)) + ") AppleWebKit/"
						+ saf + " (KHTML, like Gecko) Version/" + version
						+ " Safari/" + saf;
				break;
			}

			agent = "Mozilla/5.0 " + temp;

			break;
		case "iexplorer":

			temp = "(compatible; MSIE "
					+ ((rand.nextInt((9 - 7) + 1) + 7) + ".0")
					+ "; Windows NT "
					+ (((rand.nextInt((6 - 5) + 1) + 5) + "." + (rand
							.nextInt((1 - 0) + 1) + 0)))
					+ "; Trident/"
					+ ((rand.nextInt((5 - 3) + 1) + 3) + "." + (rand
							.nextInt((1 - 0) + 1) + 0)) + ")";

			agent = "Mozilla/5.0 " + temp;
			break;
		case "opera":

			if (os.equals("lin")) {

				temp = "(X11; Linux {proc}; U; {lang}) Presto/"
						+ ("2.9." + (rand.nextInt((190 - 160) + 1) + 160))
						+ " Version/"
						+ ((rand.nextInt((12 - 10) + 1) + 10) + ".00");

			} else {

				temp = "(Windows NT $nt; U; {lang}) Presto/"
						+ ("2.9." + (rand.nextInt((190 - 160) + 1) + 160))
						+ " Version/"
						+ ((rand.nextInt((12 - 10) + 1) + 10) + ".00");

			}

			agent = "Opera/" + (rand.nextInt((9 - 8) + 1) + 8) + "."
					+ (rand.nextInt((99 - 10) + 1) + 10) + " " + temp;
			break;
		default:// Chrome

			saf = (rand.nextInt((536 - 531) + 1) + 531) + ""
					+ (rand.nextInt((2 - 0) + 1) + 0);

			String chrome = (rand.nextInt((15 - 13) + 1) + 13) + ".0."
					+ (rand.nextInt((899 - 800) + 1) + 800) + ".0";

			switch (os) {
			case "lin":
				temp = "(X11; Linux {proc}) AppleWebKit/" + saf
						+ " (KHTML, like Gecko) Chrome/" + chrome + " Safari/"
						+ saf;
				break;
			case "mac":
				temp = "(Macintosh; U; {proc} Mac OS X "
						+ ("10_" + (rand.nextInt((7 - 5) + 1) + 5) + "_" + (rand
								.nextInt((9 - 0) + 1) + 0)) + ") AppleWebKit/"
						+ saf + " (KHTML, like Gecko) Chrome/" + chrome
						+ " Safari/" + saf;
				break;
			default:
				temp = "(Windows NT "
						+ (((rand.nextInt((6 - 5) + 1) + 5) + "." + (rand
								.nextInt((1 - 0) + 1) + 0))) + ") AppleWebKit/"
						+ saf + " (KHTML, like Gecko) Chrome/" + chrome
						+ " Safari/" + saf;
				break;
			}

			agent = "Mozilla/5.0 " + temp;
			break;
		}

		String proc = null;
		// (rand.nextInt((max - min) + 1) + min)

		switch (os) {
		case "lin":

			switch ((rand.nextInt((3 - 0) + 1) + 0)) {

			case 0:
				proc = "i686";
				break;
			case 1:
				proc = "x86_64";
				break;

			}

			break;
		case "mac":

			switch ((rand.nextInt((3 - 0) + 1) + 0)) {
			case 0:
				proc = "Intel";
				break;
			case 1:
				proc = "PPC";
				break;
			case 2:
				proc = "U; Intel";
				break;
			case 3:
				proc = "U; PPC";
				break;
			}

			break;
		default:
			break;
		}

		if (proc != null) {

			agent = agent.replace("{proc}", proc);

		}

		agent = agent.replace("{lang}", "en-US");// Kind of pointless...

		agent = agent.trim();

		return agent;

	}

}
