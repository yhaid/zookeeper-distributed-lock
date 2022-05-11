# Redis实战：实现一个分布式锁

利用Redis编写了一个简单的分布式锁

- 使用`Thread.sleep()`代替通知机制。这种方式比较浪费资源，应该使用其他的方式（比如Redis的发布/订阅，因为上锁的线程可能处于不同JVM中）；
- 没有Timeout机制。

实现一个分布式锁

测试需要首先启动一个监听`localhost:6379`的Redis实例。

`mvn clean verify`
