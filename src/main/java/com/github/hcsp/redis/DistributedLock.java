package com.github.hcsp.redis;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Callable;

public class DistributedLock {
    /**
     * The lock name. A lock with same name might be shared in multiple JVMs.
     */
    private String name;

    private static String jvmName;

    public DistributedLock(String name) {
        this.name = name;
    }

    static {
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

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .retryPolicy(new RetryNTimes(1, 1000));
        try (CuratorFramework client = builder.build();) {
            client.start();
            while (true) {
                try {
                    client.create().withMode(CreateMode.EPHEMERAL).forPath("/" + name, "0".getBytes());
                    return callable.call();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Thread.sleep(100);
            }
        }

    }


}
