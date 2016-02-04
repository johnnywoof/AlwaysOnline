package me.johnnywoof.spigot.nms;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import me.johnnywoof.databases.Database;
import me.johnnywoof.hybrid.AlwaysOnline;

import java.util.UUID;

public class CustomAuthService extends YggdrasilMinecraftSessionService {

	private final Database database;

	public CustomAuthService(YggdrasilAuthenticationService authenticationService, Database database) {
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

				throw new AuthenticationUnavailableException("Mojang servers are offline and we can't authenticate the player with our own system.");

			}

		} else {

			return super.hasJoinedServer(user, serverId);

		}

	}

}
