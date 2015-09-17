package me.johnnywoof;

public interface NativeExecutor {

	int runAsyncRepeating(Runnable runnable, long millisecondPeriod);

	void cancelTask(int taskID);

}
