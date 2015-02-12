package me.johnnywoof.database;

import net.md_5.bungee.api.ProxyServer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class FlatFile implements Database {

    private final File accounts_file;

    /**
     * Cache hashmap format:
     * <Player Name>, <Player IP~Player UUID>
     * Where ~ is the delimiter
     */
    private final HashMap<String, String> cache = new HashMap<>();

    public FlatFile(File pluginRootDir) {

        this.accounts_file = new File(pluginRootDir + File.separator + "accounts.txt");

        if (!this.accounts_file.exists()) {

            try {

                if (!this.accounts_file.createNewFile()) {

                    ProxyServer.getInstance().getLogger().log(Level.SEVERE, "[AlwaysOnline] Failed to create file " + this.accounts_file.getAbsolutePath());

                }

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void init(String host, int port, String databasename, String username, String password) {
    }

    @Override
    public void resetCache() {
        this.cache.clear();
    }

    @Override
    public UUID getUUID(String name) {

        if (this.cache.containsKey(name)) {

            return UUID.fromString(this.cache.get(name).split("~")[0]);

        } else {

            try {

                BufferedReader br = new BufferedReader(new FileReader(accounts_file));

                String l;

                while ((l = br.readLine()) != null) {

                    if (l.startsWith(name)) {

                        String[] d = l.split(Pattern.quote(":"));

                        this.cache.put(d[0], d[1] + "~" + d[2]);

                        return UUID.fromString(d[1]);

                    }

                }

                br.close();

            } catch (IOException e) {

                e.printStackTrace();

            }

            return null;

        }

    }

    @Override
    public String getIP(String name) {

        if (this.cache.containsKey(name)) {

            return this.cache.get(name).split("~")[1];

        } else {

            try {

                BufferedReader br = new BufferedReader(new FileReader(accounts_file));

                String l;

                while ((l = br.readLine()) != null) {

                    if (l.startsWith(name)) {

                        String[] d = l.split(Pattern.quote(":"));

                        this.cache.put(d[0], d[1] + "~" + d[2]);

                        return d[2];

                    }

                }

                br.close();

            } catch (IOException e) {

                e.printStackTrace();

            }

            return null;

        }

    }

    @Override
    public void updatePlayer(String name, String ip, UUID uuid) {
        this.cache.put(name, uuid.toString() + "~" + ip);

        ArrayList<String> data = new ArrayList<>();

        try {

            BufferedReader br = new BufferedReader(new FileReader(accounts_file));

            String l;

            while ((l = br.readLine()) != null) {

                if (!l.startsWith(name)) {

                    data.add(l);

                }

            }

            br.close();

            data.add(name + ":" + uuid.toString() + ":" + ip);

            PrintWriter w = new PrintWriter(new FileWriter(this.accounts_file, false));

            for (String s : data) {

                w.println(s);

            }

            w.close();

        } catch (IOException e) {

            e.printStackTrace();

        }

        data.clear();

    }

    @Override
    public void close() {
    }

    @Override
    public DatabaseType getType() {
        return DatabaseType.FlatFile;
    }

}
