package com.zhouhao.study.zookeeper.handle;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.nio.charset.StandardCharsets;

/**
 * Zookeeper会话对象
 *
 * @author : zhouhao
 * @date : Created in 2019/5/6 7:11
 */
public class ZkSession{
    public static void main(String[] args) throws InterruptedException {
        ZkSession leader = new ZkSession();
        ZkSession follow1 = new ZkSession();
        ZkSession follow2 = new ZkSession();
        while(true){
            System.out.println(".....");
            Thread.sleep(1000);
        }
    }

    /**
     * Zookeeper集群地址
     */
    private static final String ZK_SERVER_ADDR = "192.168.42.128:2181,192.168.42.128:2182,192.168.42.128:2183";

    /**
     * Zookeeper客户端对象
     */
    private final ZkClient zkClient;

    /**
     * 构造方法，创建Zookeeper连接,并注册自己的角色(master/worker)
     *
     * @author : zhouhao
     * @date : 2019/5/6 7:17
     */
    public ZkSession(){
        zkClient = new ZkClient(ZK_SERVER_ADDR,5000);
        //设置序列化对象，如不设置，则使用默认序列化对象，可能会出现IO异常
        zkClient.setZkSerializer(new ZkSerializer() {
            @Override
            public Object deserialize(byte[] bytes) throws ZkMarshallingError {
                return new String(bytes, StandardCharsets.UTF_8);
            }

            @Override
            public byte[] serialize(Object obj) throws ZkMarshallingError {
                return String.valueOf(obj).getBytes(StandardCharsets.UTF_8);
            }
        });
        this.register();
    }


    /**
     * 若无master，则注册为master，若有master，则注册为worker
     *
     * @author : zhouhao
     * @date : 2019/5/6 7:49
     */
    private void register(){
        if(zkClient.exists("/master")){
            //若workers不存在，则创建
            if(!zkClient.exists("/workers")){
                zkClient.createPersistent("/workers");
            }

            //创建临时worker
            zkClient.createEphemeralSequential("/workers/worker-","worker");

            //监视master，以便于master故障时进行处理
            zkClient.subscribeDataChanges("/master", this.getMasterListener());
        }else{
            System.out.println("master建立成功" + this);
            zkClient.createEphemeral("/master","master");
        }
    }

    /**
     * master监听器，master修改则输出修改并保持监听，master删除则重新注册master
     *
     * @return : org.I0Itec.zkclient.IZkDataListener
     * @author : zhouhao
     * @date : 2019/5/6 7:41
     */
    private IZkDataListener getMasterListener(){
        return new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data){
                System.out.println("master内容修改,继续保持监听");
                zkClient.subscribeDataChanges("/master",this);
            }

            @Override
            public void handleDataDeleted(String dataPath) {
                System.out.println("master已删除，注册新的master");
                ZkSession.this.register();
            }
        };
    }
}


