package com.example.zookeeperdemo.zookeeper;


import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ZooKeeper implements ZooKeeperService {

    private static final int LOCK_ATTEMPTS = 5;
    private final int sleepMsBetweenRetries = 100;
    private final int MAX_RETRY = 3;
    private final String connectString;

    CuratorFramework client;
    InterProcessSemaphoreMutex sharedLock;

    ExecutorService executorService = Executors.newCachedThreadPool();

    @Autowired
    public ZooKeeper(@Value("${zookeeper.url}") String connectString) {
        this.connectString = connectString;
        initialize();
    }

    private void initialize() {
        RetryPolicy retryPolicy = new RetryNTimes(MAX_RETRY, sleepMsBetweenRetries);
        client = CuratorFrameworkFactory.newClient(connectString, retryPolicy);
        client.start();
    }

    @Override
    public boolean isConnected() {
        return client.getZookeeperClient().isConnected();
    }

    @Override
    public void registerConnectionStateListener(ConnectionStateListener listener) {
        client.getConnectionStateListenable().addListener(listener);
    }

    @Override
    public void closeConnection() {
        client.close();
    }

    @Override
    public void create(String path, String data) throws Exception {
        client.create().creatingParentsIfNeeded().forPath(path, data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String get(String path, boolean watchFlag) throws Exception {
        return new String(client.getData().forPath(path), StandardCharsets.UTF_8);
    }

    @Override
    public void update(String path, byte[] data) {
        // implement me
    }

    @Override
    public void delete(String path) throws Exception {
        client.delete().forPath(path);
    }

    @Override
    public boolean exists(String path) throws Exception {
        return Objects.nonNull(client.checkExists().forPath(path));
    }

    @Override
    public boolean lock(String path) {
        try {
            sharedLock = new InterProcessSemaphoreMutex(client, path);
            for (int attempt = 1; attempt <= LOCK_ATTEMPTS; attempt++) {
                if (sharedLock.acquire(10, TimeUnit.SECONDS)) {
                    log.info("Lock acquired successfully in try " + attempt + " for path : " + path);
                    return true;
                }
                log.error("{}/{} Retrying lock for : {}", attempt, LOCK_ATTEMPTS, path);
            }
        } catch (Exception e) {
            log.info("Exception in Lock : " + e);
        }
        log.info("Lock Failed : " + path);
        return false;
    }

    @Override
    public boolean release(String path) {
        try {
            sharedLock.release();
        } catch (Exception e) {
            log.info("Release Failed : " + path);
            return false;
        }
        return true;
    }

    @Override
    public void registerDataListener(String path, CuratorCacheListener cacheListener) {
        CuratorCache cc = CuratorCache.build(client, path);
        cc.listenable().addListener(cacheListener, executorService);
        cc.start();
    }
}
