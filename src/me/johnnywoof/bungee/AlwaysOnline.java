package me.johnnywoof.bungee;

import com.google.common.io.ByteStreams;
import me.johnnywoof.databases.Database;
import me.johnnywoof.databases.FileDatabase;
import me.johnnywoof.databases.MySQLDatabase;
import me.johnnywoof.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.*;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class AlwaysOnline extends Plugin {

	public static boolean mojangOnline = true;

	public boolean disabled = false;

	public Database db = null;

	public void onEnable() {

		//Register our command
		this.getProxy().getPluginManager().registerCommand(this, new AOCommand(this));

		this.reload();

	}

	/**
	 * Reloads the plugin
	 */
	public void reload() {

		this.getLogger().info("Loading AlwaysOnline " + this.getDescription().getVersion() + " on bungeecord version " + this.getProxy().getVersion());

		if (this.db != null) {//Close existing open database connections on reload

			this.getLogger().info("Detected reload! Saving existing data...");

			try {
				this.db.save();
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.db = null;

		}

		if (!this.getDataFolder().exists()) {

			if (!this.getDataFolder().mkdir()) {

				this.getLogger().severe("Failed to create directory " + this.getDataFolder().getAbsolutePath());

			}

		}

		if (!this.getConfig().exists()) {

			this.saveDefaultConfig();

		}

		try {

			//Why is this is this not in the javadocs?
			//OR IS IT?!?!
			Configuration yml = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.getConfig());

			if (yml.getInt("config_version", 0) < 4) {

				this.getLogger().warning("*-*-*-*-*-*-*-*-*-*-*-*-*-*");
				this.getLogger().warning("Your configuration file is out of date!");
				this.getLogger().warning("Please consider deleting it for a fresh new generated copy!");
				this.getLogger().warning("Once done, do /alwaysonline reload");
				this.getLogger().warning("*-*-*-*-*-*-*-*-*-*-*-*-*-*");
				return;

			}

			int ct = yml.getInt("check-interval", 30);

			if (ct < 15) {

				this.getLogger().warning("Your check-interval is less than 15 seconds. This can cause a lot of false positives, so please set it to a higher number!");

			}

			if (yml.getBoolean("use_mysql", false) || yml.getInt("database-type", 0) == 2) {

				this.getLogger().info("Loading MySQL database...");

				try {

					this.db = new MySQLDatabase(yml.getString("host"), yml.getInt("port"), yml.getString("database-name"), yml.getString("database-username"), yml.getString("database-password"));

				} catch (SQLException e) {
					this.getLogger().severe("Failed to load the MySQL database, falling back to file database.");
					e.printStackTrace();
					this.db = new FileDatabase(new File(this.getDataFolder(), "playerData.txt"));
				}

			} else {

				this.getLogger().info("Loading file database...");
				this.db = new FileDatabase(new File(this.getDataFolder(), "playerData.txt"));

			}

			this.getLogger().info("Database is ready to go!");

			if (ct == -1) {

				this.getLogger().severe("Negative number!");
				return;

			}

			//Kill existing runnables and listeners (in case of reload)

			this.getProxy().getScheduler().cancel(this);

			this.getProxy().getPluginManager().unregisterListeners(this);

			//Read the state.txt file and assign variables

			File state_file = new File(this.getDataFolder(), "state.txt");

			if (state_file.exists()) {

				Scanner scan = new Scanner(state_file);

				String data = scan.nextLine();

				scan.close();

				if (data != null && data.contains(":")) {

					String[] d = data.split(Pattern.quote(":"));

					this.disabled = Boolean.parseBoolean(d[0]);
					AlwaysOnline.mojangOnline = Boolean.parseBoolean(d[1]);

					this.getLogger().info("Successfully loaded previous state variables!");

				}

			}

			//Register our new listener and runnable

			this.getProxy().getPluginManager().registerListener(this, new AOListener(this, yml.getString("message-kick-invalid"), yml.getString("message-kick-ip"), yml.getString("message-kick-new"), yml.getString("message-motd-offline", null)));

			//It appears all scheduled threads are async, interesting.

			final BaseComponent[] mojangOnlineMessage = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
					yml.getString("message-mojang-online",
							"&5[&2AlwaysOnline&5]&a Mojang servers are now online!")));

			final BaseComponent[] mojangOfflineMessage = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&',
					yml.getString("message-mojang-offline",
							"&5[&2AlwaysOnline&5]&a Mojang servers are now offline!")));

			this.getProxy().getScheduler().schedule(this, new Runnable() {

				private boolean previousOnlineState = true;

				@SuppressWarnings("deprecation")
				@Override
				public void run() {

					if (!AlwaysOnline.this.disabled) {

						boolean isOnline = Utils.isSessionServerOnline();

						if ((this.previousOnlineState && isOnline) && !AlwaysOnline.mojangOnline) {

							AlwaysOnline.mojangOnline = true;

							getLogger().info("Mojang session servers are back online!");

							for (ProxiedPlayer p : getProxy().getPlayers()) {

								if (p.hasPermission("alwaysonline.notify")) {

									p.sendMessage(mojangOnlineMessage);

								}

							}

						} else if ((!this.previousOnlineState && !isOnline) && AlwaysOnline.mojangOnline) {

							AlwaysOnline.mojangOnline = false;

							getLogger().info("Mojang session servers are now offline!");

							for (ProxiedPlayer p : getProxy().getPlayers()) {

								if (p.hasPermission("alwaysonline.notify")) {

									p.sendMessage(mojangOfflineMessage);

								}

							}

						}

						this.previousOnlineState = isOnline;

					}

				}

			}, 0, ct, TimeUnit.SECONDS);

		} catch (IOException e) {

			e.printStackTrace();

		}

	}

	public void onDisable() {

		if (this.db != null) {

			this.getLogger().info("Saving data...");

			try {
				this.db.save();
			} catch (Exception e) {
				e.printStackTrace();
			}

			this.getLogger().info("Successfully saved the data!");

		}

	}

	/**
	 * Generates a file object for the config file
	 *
	 * @return The config file object
	 */
	private File getConfig() {

		return new File(this.getDataFolder(), "config.yml");

	}

	/**
	 * Saves the default plugin configuration file from the jar
	 */
	public void saveDefaultConfig() {

		if (!this.getDataFolder().exists()) {
			this.getDataFolder().mkdir();
		}
		File configFile = new File(this.getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			try {
				configFile.createNewFile();
				try (InputStream is = this.getClass().getResourceAsStream("/config.yml");
					 OutputStream os = new FileOutputStream(configFile)) {
					ByteStreams.copy(is, os);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

}
