package me.johnnywoof.bungee;

import me.johnnywoof.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AOCommand extends Command {

	private final Path state_path;

	private final AlwaysOnline ao;

	public AOCommand(AlwaysOnline ao) {
		super("alwaysonline", "alwaysonline.usage", "ao");

		this.ao = ao;

		this.state_path = ao.getDataFolder().toPath().resolve("state.txt");

	}

	public void execute(CommandSender sender, String[] args) {

		if (args.length <= 0) {

			this.displayHelp(sender);

		} else {

			switch (args[0].toLowerCase()) {
				case "toggle":

					AlwaysOnline.mojangOnline = !AlwaysOnline.mojangOnline;

					ao.disabled = !AlwaysOnline.mojangOnline;

					sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "Mojang offline mode is now " + ((!AlwaysOnline.mojangOnline ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled")) + ChatColor.GOLD + "!"));

					if (AlwaysOnline.mojangOnline) {

						sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "AlwaysOnline will now treat the mojang servers as being online."));

					} else {

						sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "AlwaysOnline will no longer treat the mojang servers as being online."));

					}

					break;
				case "disable":

					ao.disabled = true;

					sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "AlwaysOnline has been disabled! AlwaysOnline will no longer check to see if the session server is offline."));

					break;
				case "enable":

					ao.disabled = false;

					sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "AlwaysOnline has been enabled! AlwaysOnline will now check to see if the session server is offline."));

					break;
				case "reload":

					ao.reload();

					sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "Configuration file has been reloaded!"));

					break;
				default:

					this.displayHelp(sender);

					break;
			}

			try {

				Files.write(this.state_path, (ao.disabled + ":" + AlwaysOnline.mojangOnline).getBytes(Utils.fileCharset));

			} catch (IOException e) {

				ao.getLogger().warning("Failed to save state. This error is not severe. [" + e.getMessage() + "]");

			}

		}

	}

	private void displayHelp(CommandSender sender) {

		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "----------" + ChatColor.GOLD + "[" + ChatColor.DARK_GREEN + "AlwaysOnline " + ChatColor.GRAY + ao.getDescription().getVersion() + "" + ChatColor.GOLD + "]" + ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "----------"));

		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "/alwaysonline toggle - " + ChatColor.DARK_GREEN + "Toggles between mojang online mode"));
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "/alwaysonline enable - " + ChatColor.DARK_GREEN + "Enables the plugin"));
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "/alwaysonline disable - " + ChatColor.DARK_GREEN + "Disables the plugin"));
		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "/alwaysonline reload - " + ChatColor.DARK_GREEN + "Reloads the configuration file"));

		sender.sendMessage(TextComponent.fromLegacyText(ChatColor.GOLD + "" + ChatColor.STRIKETHROUGH + "------------------------------"));

	}

}
