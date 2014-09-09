package me.johnnywoof.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;

import me.johnnywoof.bungeecord.AlwaysOnline;
import net.md_5.bungee.api.ProxyServer;

public class XpawManager {

	private static final String AGENT = "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2";
	
	private boolean fire_on_slow;
	
	public XpawManager(boolean fire_on_slow){
		
		this.fire_on_slow = fire_on_slow;
		
		//Hope this doesn't conflict with other plugins...which it should not...
		//Since most plugins don't care about http cookies
		
		CookieManager cm = new CookieManager();
		
		cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		
		CookieHandler.setDefault(cm);
		
		//Send the get request to xpaw to generate the cookie
		
		try{
			
			URL obj = new URL("http://xpaw.ru/mcstatus/");
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	 
			// optional default is GET
			con.setRequestMethod("GET");
	 
			//add request header
			con.setRequestProperty("User-Agent", AGENT);
	 
			con.connect();
			
			int code = con.getResponseCode();
			
			if(code != 200){
				
				ProxyServer.getInstance().getLogger().warning("[AlwaysOnline] xpaw returned http code " + code + " for http://xpaw.ru/mcstatus/!");
				
			}
			
			con.disconnect();
			
			if(AlwaysOnline.debug){
				
				System.out.println("HTTP Code: " + code);
				
				for(HttpCookie c : cm.getCookieStore().getCookies()){
				
					System.out.println("Detected cookie: " + c.toString());
				
				}
				
			}
		
		}catch(IOException e){
			
			e.printStackTrace();
			
		}
		
	}
	
	public boolean isXpawClaimingOnline(){
		
		try{
			
			URL obj = new URL("http://xpaw.ru/mcstatus/status.json");
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
	 
			// optional default is GET
			con.setRequestMethod("GET");
			
			//xpaw, y u make it so complex >.>
			
			con.setRequestProperty("X-Requested-with", "XMLHttpRequest");//PACKET SNIFFER FTW
			
			con.setRequestProperty("Referer", "http://xpaw.ru/mcstatus/");//Packet sniffffffer
			
			con.setRequestProperty("Accept-Language", "en-us");//I'm using the english language
			
			con.setRequestProperty("User-Agent", AGENT);//Super secret spy agent
			
			con.setRequestProperty("Content-Type", "text/html");//application/json
			
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			con.disconnect();
			
			String res = response.toString().toLowerCase();
			
			if(AlwaysOnline.debug){
				
				System.out.println("Response from server: " + res);
				
			}
			
			if(res.contains("\"session\":{\"status\":\"up\",\"title\":\"online\"")){
				
				return true;
				
			}else if(res.contains("\"session\":{\"status\":\"problem\",\"title\":\"Quite Slow\"")){
				
				return !this.fire_on_slow;
				
			}else{
				
				return false;
				
			}
		
		}catch(IOException e){
			
			e.printStackTrace();
			
		}
		
		return true;
		
	}
	
}
