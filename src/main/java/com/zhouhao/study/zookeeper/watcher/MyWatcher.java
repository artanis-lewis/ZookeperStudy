package com.zhouhao.study.zookeeper.watcher;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * Zookeeper连接的监控器
 * 负责监视连接的健康状况，也能用于监控Zookeeper数据的变化
 *
 * @author : zhouhao
 * @date : Created in 2019/5/2 10:32
 */
public class MyWatcher implements Watcher {
    @Override
    public void process(WatchedEvent watchedEvent) {
        System.out.println(watchedEvent);
    }
}
