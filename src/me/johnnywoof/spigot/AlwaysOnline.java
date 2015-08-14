package me.johnnywoof.spigot;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import me.johnnywoof.databases.Database;
import me.johnnywoof.databases.FileDatabase;
import me.johnnywoof.databases.MySQLDatabase;
import me.johnnywoof.spigot.nms.CustomAuthService;
import me.johnnywoof.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class AlwaysOnline extends JavaPlugin {

	public static boolean mojangOnline = true;

	public boolean disabled = false;

	public Database db = null;

	private Path stateFile;

	public void onEnable() {

		if (!this.getServer().getOnlineMode()) {

			this.getLogger().info("This server is running in offline mode, so this plugin will have no use on this server!");
			this.getLogger().info("If you are running bungeecord, please put AlwaysOnline in the bungeecord plugins directory.");
			this.getLogger().info("If you are not running bungeecord, then please remove AlwaysOnline.");
			this.getPluginLoader().disablePlugin(this);
			return;

		}

		this.getLogger().info("Loading AlwaysOnline " + this.getDescription().getVersion() + " on spigot version " + this.getServer().getVersion());

		this.getLogger().info("Loading the configuration...");

		this.saveDefaultConfig();

		if (this.getConfig().getInt("config_version", 0) < 5) {

			this.getLogger().warning("*-*-*-*-*-*-*-*-*-*-*-*-*-*");
			this.getLogger().warning("Your configuration file is out of date!");
			this.getLogger().warning("Please consider deleting it for a fresh new generated copy!");
			this.getLogger().warning("Once done, restart the server");
			this.getLogger().warning("*-*-*-*-*-*-*-*-*-*-*-*-*-*");
			this.getPluginLoader().disablePlugin(this);
			return;

		}

		this.getLogger().info("Loading the database...");

		Path dataFolder = this.getDataFolder().toPath();

		if (this.getConfig().getBoolean("use_mysql", false) || this.getConfig().getInt("database-type", 0) == 2) {

			this.getLogger().info("Loading MySQL database...");

			try {

				this.db = new MySQLDatabase(this.getConfig().getString("host"), this.getConfig().getInt("port"),
						this.getConfig().getString("database-name"),
						this.getConfig().getString("database-username"), this.getConfig().getString("database-password"));

			} catch (SQLException e) {
				this.getLogger().severe("Failed to load the MySQL database, falling back to file database.");
				e.printStackTrace();
				this.db = new FileDatabase(dataFolder.resolve("playerData.txt"));
			}

		} else {

			this.getLogger().info("Loading file database...");
			this.db = new FileDatabase(dataFolder.resolve("playerData.txt"));

		}

		this.getLogger().info("Database is ready to go!");

		//Read the state.txt file and assign variables

		this.stateFile = dataFolder.resolve("state.txt");

		if (Files.isReadable(this.stateFile)) {

			try {

				String data = new String(Files.readAllBytes(stateFile), Utils.fileCharset);

				if (data.contains(":")) {

					String[] d = data.split(Pattern.quote(":"));

					this.disabled = Boolean.parseBoolean(d[0]);
					AlwaysOnline.mojangOnline = Boolean.parseBoolean(d[1]);

					this.getLogger().info("Successfully loaded previous state variables!");

				}

			} catch (IOException e) {

				e.printStackTrace();
				this.getLogger().info("The error is not critical and can be safely ignored.");

			}

		}

		this.getLogger().info("Registering listener...");

		this.getServer().getPluginManager().registerEvents(new AOListener(this,
				this.getConfig().getString("message-kick-invalid"), this.getConfig().getString("message-kick-ip"),
				this.getConfig().getString("message-kick-new")), this);

		this.getLogger().info("Overriding authentication handler...");

		try {

			//I somehow have a feeling that this code is probably going to
			// be "stolen" and would be implemented into other plugins. Oh well. Least I was the first (I think)
			// to implement a custom authentication system in spigot :)

			String nmsVersion = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];

			String sessionServiceVariableName;
			String sessionAuthVariableName;

			switch (nmsVersion) {

				case "v1_8_R3":
					sessionServiceVariableName = "W";
					sessionAuthVariableName = "V";
					break;
				default:
					this.getLogger().severe("AlwaysOnline currently does not support spigot version " + this.getServer().getVersion());
					this.getLogger().severe("This build of AlwaysOnline only supports minecraft versions 1.8.7 or higher.");
					this.getPluginLoader().disablePlugin(this);
					return;

			}

			Method method = Class.forName("net.minecraft.server." + nmsVersion + ".MinecraftServer").getMethod("getServer");

			Object minecraftServer = method.invoke(null);

			Field sessionServiceVariable = minecraftServer.getClass().getSuperclass().getDeclaredField(sessionServiceVariableName);

			sessionServiceVariable.setAccessible(true);

			Field sessionAuthVariable = minecraftServer.getClass().getSuperclass().getDeclaredField(sessionAuthVariableName);

			sessionAuthVariable.setAccessible(true);

			sessionServiceVariable.set(minecraftServer,
					new CustomAuthService((YggdrasilAuthenticationService) sessionAuthVariable.get(minecraftServer), this.db));

		} catch (Exception e) {

			e.printStackTrace();
			this.getLogger().severe("Failed to override the authentication handler. Due to possible security risks, the server will now shut down.");
			this.getLogger().severe("If this issue persists, please contact the author and remove AlwaysOnline from your server temporarily.");
			this.getServer().shutdown();

		}

		this.getLogger().info("Starting the session check task...");

		int ct = this.getConfig().getInt("check-interval", 30);

		if (ct < 15) {

			this.getLogger().warning("Your check-interval is less than 15 seconds. This can cause a lot of false positives, so please set it to a higher number!");

		}

		final String mojangOnlineMessage = ChatColor.translateAlternateColorCodes('&',
				this.getConfig().getString("message-mojang-online",
						"&5[&2AlwaysOnline&5]&a Mojang servers are now online!"));

		final String mojangOfflineMessage = ChatColor.translateAlternateColorCodes('&',
				this.getConfig().getString("message-mojang-offline",
						"&5[&2AlwaysOnline&5]&a Mojang servers are now offline!"));

		this.getServer().getScheduler().runTaskTimerAsynchronously(this, new Runnable() {

			private boolean previousOnlineState = true;

			@Override
			public void run() {

				if (!AlwaysOnline.this.disabled) {

					boolean isOnline = Utils.isSessionServerOnline();

					if ((this.previousOnlineState && isOnline) && !AlwaysOnline.mojangOnline) {

						AlwaysOnline.mojangOnline = true;

						getLogger().info("Mojang session servers are back online!");

						for (Player p : Bukkit.getOnlinePlayers()) {

							if (p.hasPermission("alwaysonline.notify")) {

								p.sendMessage(mojangOnlineMessage);

							}

						}

					} else if ((!this.previousOnlineState && !isOnline) && AlwaysOnline.mojangOnline) {

						AlwaysOnline.mojangOnline = false;

						getLogger().info("Mojang session servers are now offline!");

						for (Player p : Bukkit.getOnlinePlayers()) {

							if (p.hasPermission("alwaysonline.notify")) {

								p.sendMessage(mojangOfflineMessage);

							}

						}

					}

					this.previousOnlineState = isOnline;

				}

			}
		}, 5, (ct * 20));

		this.getLogger().info("Loaded and ready!");

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

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {

		if (args.length <= 0) {

			this.displayHelp(sender);

		} else {

			switch (args[0].toLowerCase()) {
				case "toggle":

					mojangOnline = !mojangOnline;

					this.disabled = !mojangOnline;

					sender.sendMessage(ChatColor.GOLD + "Mojang offline mode is now " + ((!mojangOnline ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled")) + ChatColor.GOLD + "!");

					if (mojangOnline) {

						sender.sendMessage(ChatColor.GOLD + "AlwaysOnline will now treat the mojang servers as being online.");

					} else {

						sender.sendMessage(ChatColor.GOLD + "AlwaysOnline will no longer treat the mojang servers as being online.");

					}

					break;
				case "disable":

					this.disabled = true;

					sender.sendMessage(ChatColor.GOLD + "AlwaysOnline has been disabled! AlwaysOnline will no longer check to see if the session server is offline.");

					break;
				case "enable":

					this.disabled = false;

					sender.sendMessage(ChatColor.GOLD + "AlwaysOnline has been enabled! AlwaysOnline will now check to see if the session server is offline.");

					break;
				case "reload":

					sender.sendMessage(ChatColor.RED + "The reload command is not supported when running AlwaysOnline with spigot.");

					break;
				default:

					this.displayHelp(sender);

					break;
			}

			try {

				Files.write(this.stateFile, (this.disabled + ":" + mojangOnline).getBytes(Utils.fileCharset));

			} catch (IOException e) {

				this.getLogger().warning("Failed to save state. This error is not severe. [" + e.getMessage() + "]");

			}

		}

		return true;

	}

	private void displayHelp(CommandSender sender) {

		sender.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "----------" + ChatColor.GOLD + "[" + ChatColor.DARK_GREEN + "AlwaysOnline " + ChatColor.GRAY + this.getDescription().getVersion() + "" + ChatColor.GOLD + "]" + ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "----------");

		sender.sendMessage(ChatColor.GOLD + "/alwaysonline toggle - " + ChatColor.DARK_GREEN + "Toggles between mojang online mode");
		sender.sendMessage(ChatColor.GOLD + "/alwaysonline enable - " + ChatColor.DARK_GREEN + "Enables the plugin");
		sender.sendMessage(ChatColor.GOLD + "/alwaysonline disable - " + ChatColor.DARK_GREEN + "Disables the plugin");

		sender.sendMessage(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "------------------------------");

	}

}
