package www.ezrpro.com.kafka;

public class Main{
    public static void main(String[] args) {

        ///opt/kafka/bin/kafka-topics.sh --create --zookeeper slave1:2181,slave2:2181 --replication-factor 3 --partitions 3 --topic testVsCode
        String brokerList = "cluster2-slave2:9092,cluster2-slave3:9092,cluster2-slave4:9092";
        String topic = "testVsCode";
        String groupId = "testWorkThread";
        final ConsumerThreadHandler<byte[], byte[]> handler = new ConsumerThreadHandler<byte[],byte[]>(brokerList, groupId, topic);
        final int cpuCount = Runtime.getRuntime().availableProcessors();
        
        Runnable runnable = new Runnable(){
        
            public void run() {
                handler.consume(cpuCount);
            }
        };

        new Thread(runnable).start();
        System.out.println("Starting to close the consumer......");
    }
}