package com.github.hcsp.redis;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.ArrayList;
import java.util.List;

class DistributedLockTest {
    /**
     * A distributed test which starts 10 JVMs.
     */
    @Test
    public void distributedTest() throws Exception {
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String javaPath = System.getProperty("java.home") + "/bin/java";
            String classPath = System.getProperty("java.class.path");

            ProcessBuilder pb = new ProcessBuilder(javaPath, "-cp", classPath, "com.github.hcsp.redis.TestMain");
            processes.add(pb.start());
        }

        for (Process process : processes) {
            Assertions.assertEquals(0, process.waitFor());
        }

        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .retryPolicy(new RetryNTimes(1, 1000));
        try (CuratorFramework client = builder.build();) {
            client.start();
            System.out.println(new String(client.getData().forPath("/key")));
        }
    }

    @AfterEach
    public void cleanUp() {
/*        JedisPool pool = new JedisPool();
        try (Jedis jedis = pool.getResource()) {
            jedis.del("KeyUnderTest");
        }*/
    }
}
