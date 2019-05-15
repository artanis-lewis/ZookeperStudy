package com.zhouhao.study.zookeeper.handle;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkMarshallingError;
import org.I0Itec.zkclient.serialize.ZkSerializer;

import java.nio.charset.StandardCharsets;

/**
 * Zookeeper�Ự����
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
     * Zookeeper��Ⱥ��ַ
     */
    private static final String ZK_SERVER_ADDR = "192.168.42.128:2181,192.168.42.128:2182,192.168.42.128:2183";

    /**
     * Zookeeper�ͻ��˶���
     */
    private final ZkClient zkClient;

    /**
     * ���췽��������Zookeeper����,��ע���Լ��Ľ�ɫ(master/worker)
     *
     * @author : zhouhao
     * @date : 2019/5/6 7:17
     */
    public ZkSession(){
        zkClient = new ZkClient(ZK_SERVER_ADDR,5000);
        //�������л������粻���ã���ʹ��Ĭ�����л����󣬿��ܻ����IO�쳣
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
     * ����master����ע��Ϊmaster������master����ע��Ϊworker
     *
     * @author : zhouhao
     * @date : 2019/5/6 7:49
     */
    private void register(){
        if(zkClient.exists("/master")){
            //��workers�����ڣ��򴴽�
            if(!zkClient.exists("/workers")){
                zkClient.createPersistent("/workers");
            }

            //������ʱworker
            zkClient.createEphemeralSequential("/workers/worker-","worker");

            //����master���Ա���master����ʱ���д���
            zkClient.subscribeDataChanges("/master", this.getMasterListener());
        }else{
            System.out.println("master�����ɹ�" + this);
            zkClient.createEphemeral("/master","master");
        }
    }

    /**
     * master��������master�޸�������޸Ĳ����ּ�����masterɾ��������ע��master
     *
     * @return : org.I0Itec.zkclient.IZkDataListener
     * @author : zhouhao
     * @date : 2019/5/6 7:41
     */
    private IZkDataListener getMasterListener(){
        return new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data){
                System.out.println("master�����޸�,�������ּ���");
                zkClient.subscribeDataChanges("/master",this);
            }

            @Override
            public void handleDataDeleted(String dataPath) {
                System.out.println("master��ɾ����ע���µ�master");
                ZkSession.this.register();
            }
        };
    }
}


