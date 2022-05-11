package com.github.hcsp.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public class DistributedLock {
    /**
     * The lock name. A lock with same name might be shared in multiple JVMs.
     */
    private String name;

    private static Jedis jedis;

    private static String jvmName;

    public DistributedLock(String name) {
        this.name = name;
    }

    static {
        jedis = new JedisPool().getResource();
        jvmName = ManagementFactory.getRuntimeMXBean().getVmName();
    }

    /**
     * Run a given action under lock.
     *
     * @param callable the action to be executed
     * @param <T>      return type
     * @return the result
     */
    public <T> T runUnderLock(Callable<T> callable) throws Exception {

        lock(name, jedis);
        T result = callable.call();
        unlock(name, jedis);
        return result;
    }

    private void unlock(String name, Jedis jedis) {
        String value = jedis.get(name);
        if (jvmName.equals(value)) {
            jedis.del(name);
            jedis.publish("DistributedLock", "unlock");
        }
    }

    private void lock(String name, Jedis jedis) {
        while (true) {
            Long val = jedis.setnx(name, jvmName);
            if (val == 1) {
                jedis.expire(name, 1);
                return;
            }

            CountDownLatch countDownLatch = new CountDownLatch(1);
            initSubscribe(countDownLatch);
            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void initSubscribe(CountDownLatch countDownLatch) {
        final Thread thread = new Thread(() -> {
            final Jedis jedis = new Jedis();
            jedis.subscribe(new DistributedLockPubSub(countDownLatch), "DistributedLock");
        });
        thread.setDaemon(true);
        thread.start();
    }
}
