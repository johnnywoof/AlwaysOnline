package me.johnnywoof.databases;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.johnnywoof.utils.JedisFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class RedisDatabase implements Database {

    private JedisFactory jFactory;
    private final String host;
    private final int port;
    private final String password;

    private final ConcurrentHashMap<String, PlayerData> cache = new ConcurrentHashMap<>();

    public RedisDatabase(String host, int port, String password) {

        this.host = host;
        this.port = port;
        this.password = password;

        this.connect();

    }

    private void connect() {

        try {
            jFactory = new JedisFactory(this.host, this.port, 5, this.password);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public String getIP(String username) {

        PlayerData playerData = this.cache.get(username);

        if (playerData != null) {

            return playerData.ipAddress;

        } else {

            if (this.loadDataFromRedis(username)) {

                return this.cache.get(username).ipAddress;

            }

        }

        return null;

    }

    private boolean loadDataFromRedis(String username) {

        PlayerData playerData = null;

        Jedis jedis = this.jFactory.getJedis();

        try {

            Map<String, String> playerRedisData = jedis.hgetAll("AlwaysOnline:player:" + username);
            if (playerRedisData.size() > 0) {
                playerData = new PlayerData(playerRedisData.get("ip"), UUID.fromString(playerRedisData.get("uuid")));
            }

        } catch (JedisConnectionException jex) {
            this.jFactory.returnBrokenResource(jedis);
        } finally {
            this.jFactory.returnResource(jedis);
        }

        if (playerData != null) {

            this.cache.put(username, playerData);
            return true;

        }

        return false;

    }

    @Override
    public UUID getUUID(String username) {

        PlayerData playerData = this.cache.get(username);

        if (playerData != null) {

            return playerData.uuid;

        } else {

            if (this.loadDataFromRedis(username)) {

                return this.cache.get(username).uuid;

            }

        }

        return null;

    }

    @Override
    public void updatePlayer(String username, String ip, UUID uuid) {

        this.cache.put(username, new PlayerData(ip, uuid));

        Map<String, String> playerRedisMap = new HashMap<>();

        playerRedisMap.put("ip", ip);
        playerRedisMap.put("uuid", uuid.toString());

        Jedis jedis = this.jFactory.getJedis();

        //if(checkPlayerExists(p.getUniqueId(), jedis)){ // Spieler hat bereits vorher gejoint, deswegen
        try {
            jedis.hmset("AlwaysOnline:player:" + username, playerRedisMap);

            //jedis.disconnect();
        } catch (JedisConnectionException jex) {
            this.jFactory.returnBrokenResource(jedis);
        } finally {
            this.jFactory.returnResource(jedis);
        }

    }

    @Override
    public void save() throws Exception {

        Jedis jedis = this.jFactory.getJedis();

        //if(checkPlayerExists(p.getUniqueId(), jedis)){ // Spieler hat bereits vorher gejoint, deswegen
        try {

            Pipeline pipe = jedis.pipelined();
            for (Map.Entry<String, PlayerData> en : this.cache.entrySet()) {
                pipe.hset("AlwaysOnline:player:" + en.getKey(), "ip", en.getValue().ipAddress);
                pipe.hset("AlwaysOnline:player:" + en.getKey(), "uuid", en.getValue().uuid.toString());

            }
            pipe.sync();

            //jedis.disconnect();
        } catch (JedisConnectionException jex) {
            this.jFactory.returnBrokenResource(jedis);
        } finally {
            this.jFactory.returnResource(jedis);
        }

    }

    @Override
    public void close() {
        this.jFactory.getJedisPool().close();
        this.jFactory.getJedisPool().destroy();

    }

}
