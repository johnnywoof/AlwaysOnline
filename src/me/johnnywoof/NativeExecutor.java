package me.johnnywoof;

import java.util.concurrent.TimeUnit;

public interface NativeExecutor {

	int runAsyncRepeating(Runnable runnable, long delay, long period, TimeUnit timeUnit);

	void cancelTask(int taskID);

}
