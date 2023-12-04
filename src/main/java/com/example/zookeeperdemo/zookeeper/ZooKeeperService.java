package com.example.zookeeperdemo.zookeeper;

import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.zookeeper.KeeperException;

public interface ZooKeeperService {
    void create(String path, String data) throws Exception;

    String get(String path, boolean watchFlag) throws Exception;

    void update(String path, byte[] data) throws KeeperException, InterruptedException;

    void delete(String path) throws Exception;

    boolean exists(String path) throws Exception;

    boolean lock(String path);

    boolean release(String path);

    boolean isConnected();

    void registerConnectionStateListener(ConnectionStateListener listener);

    void closeConnection();

    void registerDataListener(String path, CuratorCacheListener cacheListener);
}
