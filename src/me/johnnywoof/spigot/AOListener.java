package me.johnnywoof.spigot;

import me.johnnywoof.databases.Database;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.regex.Pattern;

public class AOListener implements Listener {

	private final Pattern pat = Pattern.compile("^[a-zA-Z0-9_-]{2,16}$");//The regex to verify usernames;

	private final Database database;
	private final String kick_invalid_name;
	private final String kick_not_same_ip;
	private final String kick_new_player;

	public AOListener(Database database, String invalid, String kick_ip, String kick_new) {

		this.database = database;
		this.kick_invalid_name = invalid;
		this.kick_not_same_ip = kick_ip;
		this.kick_new_player = kick_new;

	}

	//Low priority so that we can go first. ignoreCancelled is set to false to prevent some security concern.
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
	public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {

		if (!AlwaysOnline.mojangOnline) {

			String username = event.getName();

			if (!this.validate(username)) {

				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, this.kick_invalid_name);

			}

			String ip = event.getAddress().getHostAddress();

			String lastIP = this.database.getIP(username);

			if (lastIP == null) {

				event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, this.kick_new_player);

			} else {

				if (!lastIP.equals(ip)) {

					event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, this.kick_not_same_ip);

				} else {

					Bukkit.getLogger().info("[AlwaysOnline] " + username + " was successfully authenticated while mojang servers were offline. Connecting IP is " + ip + " and the last authenticated known IP was " + lastIP);

				}

			}

		}

	}

	@EventHandler(ignoreCancelled = true)
	public void onPostLogin(PlayerJoinEvent event) {

		if (AlwaysOnline.mojangOnline) {

			this.database.updatePlayer(event.getPlayer().getName(),
					event.getPlayer().getAddress().getAddress().getHostAddress(),
					event.getPlayer().getUniqueId());

		}

	}

	/**
	 * Validate username with regular expression
	 *
	 * @param username username for validation
	 * @return true valid username, false invalid username
	 */
	public boolean validate(String username) {

		return username != null && pat.matcher(username).matches();

	}

}
