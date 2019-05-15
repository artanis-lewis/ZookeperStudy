package com.zhouhao.study.zookeeper.handle;

import com.zhouhao.study.zookeeper.watcher.MyWatcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * 类功能描述
 *
 * @author : zhouhao
 * @date : Created in 2019/5/2 10:37
 */
public class ZookeeperTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        //创建Zookeeper连接
        ZooKeeper zooKeeper = new ZooKeeper("192.168.42.128:2181,192.168.42.128:2182,192.168.42.128:2183",5000,new MyWatcher());
        while (true) {
            Thread.sleep(1000);
            System.out.println(zooKeeper.getState());
        }

    }
}
