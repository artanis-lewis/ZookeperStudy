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
 * �๦������
 *
 * @author : zhouhao
 * @date : Created in 2019/5/2 14:48
 */
public class Master {
    /**
     * Zookeeper���
     */
    private ZooKeeper zooKeeper;

    /**
     * Zookeeper��Ⱥ·������Ҫ�������еķ�������ַ����Ȼ�Ự���󲻻��Զ��л�������
     */
    private static final String ZOOKEEPER_SERVER_PATH = "192.168.42.128:2181,192.168.42.128:2182,192.168.42.128:2183";

    /**
     * ���ڵ��־
     */
    private boolean isLeader;

    /**
     * ���ڵ���Ϣ
     */
    private String serverId = Integer.toHexString(new Random().nextInt());

    /**
     * ���췽��������Zookeeper����
     *
     * @throws IOException IO�쳣
     * @author : zhouhao
     * @date : 2019/5/2 15:00
     */
    public Master() throws IOException {
        zooKeeper = new ZooKeeper(ZOOKEEPER_SERVER_PATH,5000,new MyWatcher());
    }


    public void syncRunForMaster() throws KeeperException, InterruptedException {
        while(true){
            try{
                //ͨ������master�ڵ��ȡmasterȨ��
                zooKeeper.create("/master",serverId.getBytes(),OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                isLeader = true;
                break;
            }catch (KeeperException.NodeExistsException e){
                System.out.println("��ǰ�Ѵ������ڵ�");
                isLeader = false;
                break;
            }catch (KeeperException.ConnectionLossException e){
                System.out.println("�����쳣��������������������");
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
                System.out.println("��ǰ�����������ڵ�");
                return false;
            }catch (KeeperException.ConnectionLossException e){
                System.out.println("�����쳣��������������������");
            }
        }
    }



    public static void main(String[] args) throws IOException, KeeperException, InterruptedException {
        //����Zookeeper��������Ⱥ��ַ
        String connectionAddr = "192.168.42.128:2181,192.168.42.128:2182,192.168.42.128:2183";

        //����zookeeper����
        ZkClient zkClient = new ZkClient(new ZkConnection(connectionAddr),5000);

        //����master�ڵ㣬��ȡ����Ȩ
        zkClient.createEphemeral("/master",Integer.toHexString(new Random().nextInt()));

        //����Ԫ���ݽڵ�
        zkClient.createPersistent("/workers","workers");
        zkClient.createPersistent("/tasks","tasks");
        zkClient.createPersistent("/assign","assign");
        zkClient.createPersistent("/status","status");

        //�鿴�ڵ�����
        System.out.println("workers�ڵ�����:" + zkClient.readData("/workers"));
        System.out.println("tasks�ڵ�����:" + zkClient.readData("/tasks"));
        System.out.println("assign�ڵ�����:" + zkClient.readData("/assign"));
        System.out.println("status�ڵ�����:" + zkClient.readData("/status"));

        //ע������ִ������Ĵӽڵ�
        zkClient.createEphemeralSequential("/workers/worker-","Idle");
        zkClient.createEphemeralSequential("/workers/worker-","Idle");

        //��ȡ���鿴�����ڵ�����
        for(String worker : zkClient.getChildren("/workers")){
            System.out.println(worker + " : " + zkClient.readData("/workers/" + worker));
        }

        //ע������
        zkClient.createPersistentSequential("/tasks/task-","command1");
        zkClient.createPersistentSequential("/tasks/task-","command2");

        //��ȡ���鿴����ڵ�����
        for(String task : zkClient.getChildren("/tasks")){
            System.out.println(task + " : " + zkClient.readData("/tasks/" + task));
        }


        //�ݹ�ɾ�������Խڵ�
        zkClient.deleteRecursive("/workers");
        zkClient.deleteRecursive("/tasks");
        zkClient.deleteRecursive("/assign");
        zkClient.delete("/status");
    }
}
























