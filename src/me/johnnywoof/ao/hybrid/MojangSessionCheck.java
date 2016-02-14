package me.johnnywoof.ao.hybrid;

import com.google.gson.Gson;
import me.johnnywoof.ao.utils.CheckMethods;

public class MojangSessionCheck implements Runnable {

	private final AlwaysOnline alwaysOnline;
	private final boolean useHeadSessionServer, mojangServerStatus, xpaw;
	private final int totalCheckMethods;
	private final String messageMojangOffline, messageMojangOnline;
	private final Gson gson;

	public MojangSessionCheck(AlwaysOnline alwaysOnline) {
		this.alwaysOnline = alwaysOnline;

		int methodCount = 0;

		this.useHeadSessionServer = Boolean.getBoolean(this.alwaysOnline.config.getProperty("http-head-session-server", "false"));

		if (this.useHeadSessionServer) {
			methodCount++;
			this.gson = new Gson();
		} else {
			this.gson = null;
		}

		this.mojangServerStatus = Boolean.getBoolean(this.alwaysOnline.config.getProperty("mojang-server-status", "false"));

		if (this.mojangServerStatus)
			methodCount++;

		this.xpaw = Boolean.getBoolean(this.alwaysOnline.config.getProperty("xpaw-status", "false"));

		if (this.xpaw)
			methodCount++;

		this.totalCheckMethods = methodCount;

		this.messageMojangOffline = this.alwaysOnline.config.getProperty("message-mojang-offline", "§5[§2AlwaysOnline§5]§a Mojang servers are now offline!");
		this.messageMojangOnline = this.alwaysOnline.config.getProperty("message-mojang-online", "§5[§2AlwaysOnline§5]§a Mojang servers are now online!");

	}

	@Override
	public void run() {

		if (!AlwaysOnline.CHECK_SESSION_STATUS)
			return;

		int downServiceReport = 0;

		if (this.useHeadSessionServer && !CheckMethods.directSessionServerStatus(this.gson))
			downServiceReport++;

		if (this.mojangServerStatus && !CheckMethods.mojangHelpPage())
			downServiceReport++;

		if (this.xpaw && !CheckMethods.xpaw())
			downServiceReport++;

		if (downServiceReport >= this.totalCheckMethods) {//Offline

			if (!AlwaysOnline.MOJANG_OFFLINE_MODE) {

				AlwaysOnline.MOJANG_OFFLINE_MODE = true;

				if (!"null".equals(this.messageMojangOffline))
					this.alwaysOnline.nativeExecutor.broadcastMessage(this.messageMojangOffline);

			}

		} else {//Online

			if (AlwaysOnline.MOJANG_OFFLINE_MODE) {

				AlwaysOnline.MOJANG_OFFLINE_MODE = false;

				if (!"null".equals(this.messageMojangOnline))
					this.alwaysOnline.nativeExecutor.broadcastMessage(this.messageMojangOnline);

			}

		}

	}

}
