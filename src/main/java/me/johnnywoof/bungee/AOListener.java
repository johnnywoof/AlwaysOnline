package me.johnnywoof.bungee;

import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.connection.InitialHandler;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.UUID;
import java.util.regex.Pattern;

public class AOListener implements Listener {

	private final Pattern pat = Pattern.compile("^[a-zA-Z0-9_-]{2,16}$");//The regex to verify usernames;

	private final String kick_invalid_name;
	private final String kick_not_same_ip;
	private final String kick_new_player;
	private final String motdOffline;

	private final AlwaysOnline ao;

	public AOListener(AlwaysOnline ao, String invalid, String kick_ip, String kick_new, String motdOffline) {

		this.ao = ao;

		if ("null".equals(motdOffline) || motdOffline == null) {

			this.motdOffline = null;

		} else {

			this.motdOffline = ChatColor.translateAlternateColorCodes('&', motdOffline);

		}

		this.kick_invalid_name = ChatColor.translateAlternateColorCodes('&', invalid);
		this.kick_not_same_ip = ChatColor.translateAlternateColorCodes('&', kick_ip);
		this.kick_new_player = ChatColor.translateAlternateColorCodes('&', kick_new);

	}

	//A high priority to allow other plugins to go first
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPreLogin(PreLoginEvent event) {

		//Make sure it is not canceled
		if (event.isCancelled()) {
			return;
		}

		if (!AlwaysOnline.mojangOnline) {//Make sure we are in mojang offline mode

			//Verify if the name attempting to connect is even verified

			if (event.getConnection().getName().length() > 16) {

				event.setCancelReason(this.kick_invalid_name);

				event.setCancelled(true);

				return;

			} else if (!this.validate(event.getConnection().getName())) {

				event.setCancelReason(this.kick_invalid_name);

				event.setCancelled(true);

				return;

			}

			//Initialize our hacky stuff

			InitialHandler handler = (InitialHandler) event.getConnection();

			//Get the connecting ip
			final String ip = handler.getAddress().getAddress().getHostAddress();

			//Get last known ip
			final String lastip = this.ao.db.getIP(event.getConnection().getName());

			if (lastip == null) {//If null the player connecting is new

				event.setCancelReason(this.kick_new_player);

				event.setCancelled(true);

				this.ao.getLogger().info("Denied " + event.getConnection().getName() + " from logging in cause their ip [" + ip + "] has never connected to this server before!");

			} else {

				if (ip.equals(lastip)) {//If it matches set handler to offline mode, so it does not authenticate player with mojang

					this.ao.getLogger().info("Skipping session login for player " + event.getConnection().getName() + " [Connected ip: " + ip + ", Last ip: " + lastip + "]!");

					handler.setOnlineMode(false);

				} else {//Deny the player from joining

					this.ao.getLogger().info("Denied " + event.getConnection().getName() + " from logging in cause their ip [" + ip + "] does not match their last ip!");

					handler.setOnlineMode(true);

					event.setCancelReason(this.kick_not_same_ip);

					event.setCancelled(true);

				}

			}

		}

	}

	//Set priority to highest to almost guaranteed to have our MOTD displayed
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPing(ProxyPingEvent event) {

		if (!AlwaysOnline.mojangOnline && this.motdOffline != null) {

			ServerPing sp = event.getResponse();

			String s = this.motdOffline;

			s = s.replaceAll(".newline.", "\n");

			sp.setDescription(s);

			event.setResponse(sp);

		}

	}

	@SuppressWarnings("deprecation")
	//Set priority to lowest since we'll be needing to go first
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPost(PostLoginEvent event) {

		if (!AlwaysOnline.mojangOnline) {

			InitialHandler handler = (InitialHandler) event.getPlayer().getPendingConnection();

			try {

				UUID uuid = this.ao.db.getUUID(event.getPlayer().getName());

				//Reflection

				Field sf = handler.getClass().getDeclaredField("uniqueId");
				sf.setAccessible(true);
				sf.set(handler, uuid);

				sf = handler.getClass().getDeclaredField("offlineId");
				sf.setAccessible(true);
				sf.set(handler, uuid);

				Collection<String> g = this.ao.getProxy().getConfigurationAdapter().getGroups(event.getPlayer().getName());
				g.addAll(this.ao.getProxy().getConfigurationAdapter().getGroups(event.getPlayer().getUniqueId().toString()));

				UserConnection userConnection = (UserConnection) event.getPlayer();

				for (String s : g) {
					userConnection.addGroups(s);
				}

				this.ao.getLogger().info(event.getPlayer().getName() + " successfully logged in while mojang servers were offline!");

				//ProxyServer.getInstance().getLogger().info("Overriding uuid for " + event.getPlayer().getName() + " to " + uuid.toString() + "! New uuid is " + event.getPlayer().getUniqueId().toString());

			} catch (Exception e) {//Play it safe, if an error deny the player

				event.getPlayer().disconnect("Sorry, the mojang servers are offline and we can't authenticate you with our own system!");

				this.ao.getLogger().warning("Internal error for " + event.getPlayer().getName() + ", preventing login.");

				e.printStackTrace();

			}

		} else {

			//If we are not in mojang offline mode, update the player data

			final String username = event.getPlayer().getName();
			final String ip = event.getPlayer().getAddress().getAddress().getHostAddress();
			final UUID uuid = event.getPlayer().getUniqueId();

			this.ao.getProxy().getScheduler().runAsync(this.ao, new Runnable() {
				@Override
				public void run() {
					AOListener.this.ao.db.updatePlayer(username, ip, uuid);
				}
			});

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
