package me.johnnywoof.ao.spigot.authservices;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import me.johnnywoof.ao.databases.Database;
import me.johnnywoof.ao.hybrid.AlwaysOnline;
import me.johnnywoof.ao.spigot.SpigotLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.logging.Level;

public class NMSAuthService extends YggdrasilMinecraftSessionService {

	private final Database database;

	public NMSAuthService(YggdrasilAuthenticationService authenticationService, Database database) {
		super(authenticationService);
		this.database = database;
	}

	@Override
	public GameProfile hasJoinedServer(GameProfile user, String serverId) throws AuthenticationUnavailableException {

		if (AlwaysOnline.MOJANG_OFFLINE_MODE) {

			UUID uuid = this.database.getUUID(user.getName());

			if (uuid != null) {

				return new GameProfile(uuid, user.getName());

			} else {

				SpigotLoader.getPlugin(SpigotLoader.class).log(Level.INFO, user.getName() + " " +
						"never joined this server before when mojang servers were online. Denying their access.");

				throw new AuthenticationUnavailableException("Mojang servers are offline and we can't authenticate the player with our own system.");

			}

		} else {

			return super.hasJoinedServer(user, serverId);

		}

	}

	public static void setUp(SpigotLoader spigotLoader) throws Exception {

		String nmsVersion = spigotLoader.getServer().getClass().getPackage().getName().split("\.")[3];

		String sessionServiceVariableName;
		String sessionAuthVariableName;

		switch (nmsVersion) {
			case "v1_8_R1":
			case "v1_8_R2":
			case "v1_8_R3":
				sessionServiceVariableName = "W";
				sessionAuthVariableName = "V";
				break;
			case "v1_9_R1":
			case "v1_9_R2":
			case "v1_10_R1"
				sessionServiceVariableName = "V";
				sessionAuthVariableName = "U";
				break;
			default:
				spigotLoader.getLogger().severe("AlwaysOnline currently does not support spigot version " + spigotLoader.getServer().getVersion());
				spigotLoader.getLogger().severe("This build of AlwaysOnline only supports minecraft version 1.8 up to 1.10");
				spigotLoader.getPluginLoader().disablePlugin(spigotLoader);
				return;

		}

		Method method = Class.forName("net.minecraft.server." + nmsVersion + ".MinecraftServer").getMethod("getServer");

		Object minecraftServer = method.invoke(null);

		Field sessionServiceVariable = minecraftServer.getClass().getSuperclass().getDeclaredField(sessionServiceVariableName);

		sessionServiceVariable.setAccessible(true);

		Field sessionAuthVariable = minecraftServer.getClass().getSuperclass().getDeclaredField(sessionAuthVariableName);

		sessionAuthVariable.setAccessible(true);

		sessionServiceVariable.set(minecraftServer,
				new NMSAuthService((YggdrasilAuthenticationService) sessionAuthVariable.get(minecraftServer), spigotLoader.alwaysOnline.database));
	}

}
