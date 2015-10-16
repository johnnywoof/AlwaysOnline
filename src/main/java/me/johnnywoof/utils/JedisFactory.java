package me.johnnywoof.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class JedisFactory {

    private static JedisPool jedisPool;

    public JedisFactory(final String host, final int port, final int timeout, final String password) {

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(3);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMaxIdle(99);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTimeBetweenEvictionRunsMillis(50L);
        jedisPool = new JedisPool(poolConfig, host, port);
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    public void returnBrokenResource(Jedis j) {
        jedisPool.returnBrokenResource(j);
    }

    public void returnResource(Jedis j) {
        jedisPool.returnResource(j);
    }

}
