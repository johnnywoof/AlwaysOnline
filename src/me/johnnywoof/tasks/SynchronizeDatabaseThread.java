package me.johnnywoof.tasks;

import me.johnnywoof.databases.Database;

import java.io.IOException;

public class SynchronizeDatabaseThread implements Runnable {

	private final Database database;

	public SynchronizeDatabaseThread(Database database) {

		this.database = database;

	}

	@Override
	public void run() {

		try {

			this.database.flushCache();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
