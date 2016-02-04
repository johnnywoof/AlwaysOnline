package me.johnnywoof.bungee;

import me.johnnywoof.NativeExecutor;
import me.johnnywoof.hybrid.AlwaysOnline;
import net.md_5.bungee.api.plugin.Plugin;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BungeeLoader extends Plugin implements NativeExecutor {

	public final AlwaysOnline alwaysOnline = new AlwaysOnline(this);

	@Override
	public void onEnable() {
		this.alwaysOnline.reload();
		//Execute native setup
	}

	@Override
	public void onDisable() {
		this.alwaysOnline.disable();
	}

	@Override
	public int runAsyncRepeating(Runnable runnable, long delay, long period, TimeUnit timeUnit) {
		return this.getProxy().getScheduler().schedule(this, runnable, delay, period, timeUnit).getId();
	}

	@Override
	public void cancelTask(int taskID) {
		this.getProxy().getScheduler().cancel(taskID);
	}

	@Override
	public void cancelAllOurTasks() {
		this.getProxy().getScheduler().cancel(this);
	}

	@Override
	public void unregisterAllListeners() {
		this.getProxy().getPluginManager().unregisterListeners(this);
	}

	@Override
	public void log(Level level, String message) {
		this.getLogger().log(level, message);
	}

	@Override
	public Path dataFolder() {
		return this.getDataFolder().toPath();
	}

	@Override
	public void disablePlugin() {
		//Bungeecord not supported...
	}

	@Override
	public void registerListener() {
		this.getProxy().getPluginManager().registerListener(this, new AOListener(this));
	}

	@Override
	public AlwaysOnline getAOInstance() {
		return this.alwaysOnline;
	}
}
