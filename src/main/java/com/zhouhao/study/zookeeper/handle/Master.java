package com.zhouhao.study.zookeeper.handle;

import com.zhouhao.study.zookeeper.watcher.MyWatcher;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.Random;

import static org.apache.zookeeper.ZooDefs.Ids.OPEN_ACL_UNSAFE;

/**
 * 类功能描述
 *
 * @author : zhouhao
 * @date : Created in 2019/5/2 14:48
 */
public class Master {
    /**
     * Zookeeper句柄
     */
    private ZooKeeper zooKeeper;

    /**
     * Zookeeper集群路径，需要包含所有的服务器地址，不然会话对象不会自动切换和重连
     */
    private static final String ZOOKEEPER_SERVER_PATH = "192.168.42.128:2181,192.168.42.128:2182,192.168.42.128:2183";

    /**
     * 主节点标志
     */
    private boolean isLeader;

    /**
     * 主节点信息
     */
    private String serverId = Integer.toHexString(new Random().nextInt());

    /**
     * 构造方法，创建Zookeeper连接
     *
     * @throws IOException IO异常
     * @author : zhouhao
     * @date : 2019/5/2 15:00
     */
    public Master() throws IOException {
        zooKeeper = new ZooKeeper(ZOOKEEPER_SERVER_PATH,5000,new MyWatcher());
    }


    public void syncRunForMaster() throws KeeperException, InterruptedException {
        while(true){
            try{
                //通过创建master节点获取master权限
                zooKeeper.create("/master",serverId.getBytes(),OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                isLeader = true;
                break;
            }catch (KeeperException.NodeExistsException e){
                System.out.println("当前已存在主节点");
                isLeader = false;
                break;
            }catch (KeeperException.ConnectionLossException e){
                System.out.println("网络异常，正在请求重连。。。");
            }

            if(this.syncCheckMaster()){
                break;
            }
        }
    }

    private boolean syncCheckMaster() throws KeeperException, InterruptedException {
        while(true){
            try{
                byte[] data = zooKeeper.getData("/master",false,new Stat());
                isLeader = new String(data).equals(serverId);
                return true;
            }catch (KeeperException.NoNodeException e){
                System.out.println("当前还不存在主节点");
                return false;
            }catch (KeeperException.ConnectionLossException e){
                System.out.println("网络异常，正在请求重连。。。");
            }
        }
    }



    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        //定义Zookeeper服务器集群地址
        String connectionAddr = "192.168.42.128:2181,192.168.42.128:2182,192.168.42.128:2183";

        //创建zookeeper连接
        ZkClient zkClient = new ZkClient(new ZkConnection(connectionAddr),5000);

        //创建master节点，获取管理权
        zkClient.createEphemeral("/master",Integer.toHexString(new Random().nextInt()));

        //创建元数据节点
        zkClient.createPersistent("/workers","workers");
        zkClient.createPersistent("/tasks","tasks");
        zkClient.createPersistent("/assign","assign");
        zkClient.createPersistent("/status","status");

        //查看节点数据
        System.out.println("workers节点数据:" + zkClient.readData("/workers"));
        System.out.println("tasks节点数据:" + zkClient.readData("/tasks"));
        System.out.println("assign节点数据:" + zkClient.readData("/assign"));
        System.out.println("status节点数据:" + zkClient.readData("/status"));

        //注册两个执行任务的从节点
        zkClient.createEphemeralSequential("/workers/worker-","Idle");
        zkClient.createEphemeralSequential("/workers/worker-","Idle");

        //获取并查看工作节点数据
        for(String worker : zkClient.getChildren("/workers")){
            System.out.println(worker + " : " + zkClient.readData("/workers/" + worker));
        }

        //注册任务
        zkClient.createPersistentSequential("/tasks/task-","command1");
        zkClient.createPersistentSequential("/tasks/task-","command2");

        //获取并查看任务节点数据
        for(String task : zkClient.getChildren("/tasks")){
            System.out.println(task + " : " + zkClient.readData("/tasks/" + task));
        }


        //递归删除永久性节点
        zkClient.deleteRecursive("/workers");
        zkClient.deleteRecursive("/tasks");
        zkClient.deleteRecursive("/assign");
        zkClient.delete("/status");
    }
}
























