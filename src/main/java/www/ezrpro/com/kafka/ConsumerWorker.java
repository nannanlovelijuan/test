package www.ezrpro.com.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;

import java.util.List;
import java.util.Map;

public class ConsumerWorker<K,V> implements Runnable {

    private final ConsumerRecords<K,V> records;
    private final Map<TopicPartition,OffsetAndMetadata> offsets;

    public ConsumerWorker(ConsumerRecords<K,V> records,Map<TopicPartition,OffsetAndMetadata> offsets){

        this.records = records;
        this.offsets = offsets;
    }

    public void run() {

        for(TopicPartition partition : records.partitions()) {
            List<ConsumerRecord<K, V>> partitionRecords = records.records(partition);
            
            for(ConsumerRecord<K,V> record:partitionRecords){
                //插入消息处理逻辑
                System.out.println(String.format("record=%s,topic=%s, partition=%d, offset=%d", record.value(),record.topic()
                ,record.partition(),record.offset()));
            }
            //上报位移信息
            long lastoffset = partitionRecords.get(partitionRecords.size() - 1).offset();
            synchronized(offsets){
                if(!offsets.containsKey(partition)){
                    offsets.put(partition, new OffsetAndMetadata(lastoffset + 1));
                }else{
                    long curr = offsets.get(partition).offset();
                    if(curr <= lastoffset + 1){
                        offsets.put(partition, new OffsetAndMetadata(lastoffset + 1));
                    }
                }
            }


        }
    }

}
