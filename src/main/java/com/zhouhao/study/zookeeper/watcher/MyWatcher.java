package com.zhouhao.study.zookeeper.watcher;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * Zookeeper���ӵļ����
 * ����������ӵĽ���״����Ҳ�����ڼ��Zookeeper���ݵı仯
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
