package www.ezrpro.com.kafka;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;

/**
 * Hello world!
 *
 */
public class ConsumerThreadHandler<K,V> {

    private final KafkaConsumer<K,V> consumer;
    private ExecutorService executors;
    private final Map<TopicPartition,OffsetAndMetadata> offsets = new HashMap<TopicPartition,OffsetAndMetadata>();

    public ConsumerThreadHandler(String brokerList,String groupId,String topic){
        Properties props = new Properties();
        props.put("bootstrap.servers", brokerList);
        props.put("group.id", groupId);
        props.put("enable.auto.commit", "false");
//      earliest: 当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，从头开始消费
//      latest:   当各分区下有已提交的offset时，从提交的offset开始消费；无提交的offset时，消费新产生的该分区下的数据
        props.put("auto.offset.reset","earliest");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        

        System.out.println("brokerList:"+brokerList+"\ttopic:"+topic+"\tgroupId:"+groupId);
        consumer = new KafkaConsumer<K,V>(props);
        consumer.subscribe(Arrays.asList(topic),new ConsumerRebalanceListener(){
        
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                consumer.commitSync();
            }
        
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                offsets.clear();
            }
        });
    }


    /**
     * 消费主方法
     * @param threadNum
     */
    public void consume(int threadNum){
        executors = new ThreadPoolExecutor(
            threadNum, 
            threadNum, 0L, 
            TimeUnit.MILLISECONDS, 
            new ArrayBlockingQueue<Runnable>(1000),
            new ThreadPoolExecutor.CallerRunsPolicy());

            try {
                while(true){
                    ConsumerRecords<K,V> records = consumer.poll(1000L);
                    if(!records.isEmpty()){
                        executors.submit(new ConsumerWorker<K,V>(records,offsets));
                    }
                    commitOffsets();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }finally{
                commitOffsets();
                consumer.close();
            }
    }

    private void commitOffsets() {
        //尽量降低synchronized块对offsets锁定的时间
        Map<TopicPartition,OffsetAndMetadata> unmodfiedMap;

        synchronized(offsets){
            if(offsets.isEmpty()){
                return;
            }
            unmodfiedMap = Collections.unmodifiableMap(new HashMap<TopicPartition,OffsetAndMetadata>(offsets));
            offsets.clear();
        }
        consumer.commitSync(unmodfiedMap);
    }

    public void close(){
        consumer.wakeup();
        executors.shutdown();
    }

}
