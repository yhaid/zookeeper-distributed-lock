package com.github.hcsp.redis;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;

public class TestMain {
    public static void main(String[] args) throws Exception {
        init();
        new DistributedLock("lock").runUnderLock(() -> {
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                    .retryPolicy(new RetryNTimes(1, 1000));
            try (CuratorFramework client = builder.build();) {
                client.start();
                String value = new String(client.getData().forPath("/key"));
                client.setData().forPath("/key",((Integer.parseInt(value) + 1) + "").getBytes());
                System.out.println(new String(client.getData().forPath("/key")));
            }
            return null;
        });
    }

    private static void init() {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString("127.0.0.1:2181")
                .retryPolicy(new RetryNTimes(1, 1000));
        try (CuratorFramework client = builder.build();) {
            client.start();
            client.create().withMode(CreateMode.PERSISTENT).forPath("/key", "0".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
