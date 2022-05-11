package com.github.hcsp.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.Callable;

public class DistributedLockV1 {
    /**
     * The lock name. A lock with same name might be shared in multiple JVMs.
     */
    private String name;

    private static final long TIME_OUT = 1000;

    public DistributedLockV1(String name) {
        this.name = name;
    }

    /**
     * Run a given action under lock.
     *
     * @param callable the action to be executed
     * @param <T>      return type
     * @return the result
     */
    public <T> T runUnderLock(Callable<T> callable) throws Exception {
        JedisPool pool = new JedisPool();
        try (Jedis resource = pool.getResource()) {
            String timestamp = Long.toString(System.currentTimeMillis());
            long time = 0;
            while (time < TIME_OUT) {
                Long result = resource.setnx(name, timestamp);
                if (result == 1) {
                    T callRes = callable.call();
                    resource.del(name);
                    System.out.println("执行完毕...");
                    return callRes;
                } else {
                    time += 10;
                    Thread.sleep(10);
                }
            }
        }
        return null;
    }
}
