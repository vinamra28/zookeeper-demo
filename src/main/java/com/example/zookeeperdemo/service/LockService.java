package com.example.zookeeperdemo.service;

import com.example.zookeeperdemo.zookeeper.ZooKeeperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
@Slf4j
public class LockService {

    @Autowired
    private ZooKeeperService zooKeeper;

    public boolean setLock(String key) throws  Exception {
        log.info("setting lock on key " + key);
        String zkey = "/key/"+key;
        if(zooKeeper.exists(zkey)) {
            log.warn("lock already applied on this key");
            return false;
        }
        zooKeeper.create(zkey, String.valueOf(LocalTime.now()));
        zooKeeper.lock(zkey);
        return true;
    }

    public boolean unlock(String key) throws Exception {
        log.info("unlock the key " + key);
        String zkey = "/key/"+key;
        if (!zooKeeper.exists(zkey)) {
            log.warn("lock already released");
            return false;
        }
        zooKeeper.release(zkey);
        zooKeeper.delete(zkey);
        return true;
    }
}
