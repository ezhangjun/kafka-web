package me.bliss.kafka.web.component;

import me.bliss.kafka.web.constant.ServiceContants;
import me.bliss.kafka.web.model.Broker;
import me.bliss.kafka.web.model.Topic;
import me.bliss.kafka.web.result.ServiceResult;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.*;

/**
 *
 *
 * @author lanjue
 * @version $Id: me.bliss.kafka.web.service, v 0.1 3/3/15
 *          Exp $
 */

public class ZookeeperComponent {

    private static String host = "10.210.12.204";

    private static int port = 2181;

    private static int timeout = 60000;

    private static ZooKeeper zooKeeper = null;

    static {
        try {
            zooKeeper = new ZooKeeper(host + ":" + port, timeout, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    System.out.println("触发了事件" + event.getType());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ServiceResult<String> create(String path, String data) {
        final ServiceResult<String> result = new ServiceResult<String>();
        try {
            final Stat exists = zooKeeper.exists(path, false);
            if (exists != null && exists.getDataLength() > 0) {
                result.setSuccess(false);
                result.setErrorMsg("path Already exists");
                return result;
            }
            String createResult = zooKeeper
                    .create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                            CreateMode.PERSISTENT);
            result.setSuccess(true);
            result.setResult(createResult);
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ServiceResult<String> getData(String path) {
        final ServiceResult<String> result = new ServiceResult<String>();
        try {
            byte[] data = zooKeeper.getData(path, false, null);
            result.setResult(new String(data));
            result.setSuccess(true);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setErrorMsg(e.getMessage());
        }
        return result;
    }

    public static ServiceResult deleteNode(String path) {
        final ServiceResult<Boolean> result = new ServiceResult<Boolean>();
        try {
            zooKeeper.delete(path, -1);
            result.setResult(true);
            result.setSuccess(true);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ServiceResult<List<String>> getChildren(String path) {
        final ServiceResult<List<String>> result = new ServiceResult<List<String>>();

        final ArrayList<String> nodes = new ArrayList<String>();
        return result;
    }

    public static ServiceResult<Map<String, Object>> getBrokers() {
        final ServiceResult<Map<String, Object>> serviceResult = new ServiceResult<Map<String, Object>>();
        final HashMap<String, Object> brokers = new HashMap<String, Object>();
        final List<Topic> topics = new ArrayList<Topic>();
        try {
            final List<String> topicsChildren = zooKeeper
                    .getChildren(ServiceContants.KAFKA_BROKERS_TOPIC_PATH, false);
            for (String topicChild : topicsChildren) {
                topics.add(handleTopic(topicChild));
            }
            brokers.put("ids", handleBroker());
            brokers.put("topics", topics);
            serviceResult.setSuccess(true);
            serviceResult.setResult(brokers);
        } catch (Exception e) {
            e.printStackTrace();
            serviceResult.setSuccess(false);
            serviceResult.setErrorMsg("interface invoke error!");
        }
        return serviceResult;
    }

    public static ServiceResult<Topic> getTopicDetail(String name) {
        final ServiceResult<Topic> result = new ServiceResult<Topic>();
        try {
            final Topic topic = handleTopic(name);
            result.setResult(topic);
            result.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ServiceResult<Map<String,Broker>> getBrokersDetail(){
        final ServiceResult<Map<String, Broker>> result = new ServiceResult<Map<String, Broker>>();
        try {
            result.setResult(handleBroker());
            result.setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Map<String,Broker> handleBroker()
            throws KeeperException, InterruptedException, IOException {
        final HashMap<String, Broker> brokers = new HashMap<String, Broker>();
        final ObjectMapper mapper = new ObjectMapper();
        final List<String> idsChildren = zooKeeper
                .getChildren(ServiceContants.KAFKA_BROKERS_IDS_PATH, false);
        for (String idsChild : idsChildren) {
            final byte[] bytes = zooKeeper
                    .getData(ServiceContants.KAFKA_BROKERS_IDS_PATH + "/" + idsChild, false,
                            null);
            brokers.put(idsChild, mapper.readValue(new String(bytes), Broker.class));
        }
        return brokers;
    }

    private static Topic handleTopic(String topicName)
            throws KeeperException, InterruptedException, IOException {
        final byte[] bytes = zooKeeper
                .getData(ServiceContants.KAFKA_BROKERS_TOPIC_PATH + "/" + topicName, false,
                        null);
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode root = mapper.readTree(new String(bytes));
        final HashMap<String, String> partitions = new HashMap<String, String>();
        final HashMap<String, Integer> brokerPartitions = new HashMap<String, Integer>();
        final Topic topic = new Topic();

        final Iterator<String> fieldNames = root.findPath("partitions").getFieldNames();

        int replication = 0;
        while (fieldNames.hasNext()) {
            final String field = fieldNames.next();
            final String partition = root.findPath("partitions").get(field).toString();
            accumulateBrokerPartitions(partition, brokerPartitions);
            partitions.put(field, removeStartAndEndChar(partition));
            replication = judgeReplication(partition, replication);
        }

        topic.setName(topicName);
        topic.setPartitions(partitions);
        topic.setBrokerPartitions(brokerPartitions);
        topic.setBrokerPartitionsDetail(getPartitionsForBrokers(partitions));
        topic.setReplication(replication);

        return topic;
    }

    /**
     *  get partitions for broker  eg: {100: [0,1,2]}
     * @param partitions  partitions detail  eg: {0: "101,100", 1: "100,101"}
     * @return
     */
    private static Map<String,List<String>> getPartitionsForBrokers(Map<String,String> partitions){
        final HashMap<String, List<String>> result = new HashMap<String, List<String>>();
        final Iterator<Map.Entry<String, String>> iterator = partitions.entrySet().iterator();
        final ArrayList<String> brokersList = new ArrayList<String>();
        while (iterator.hasNext()){
            final Map.Entry<String, String> entry = iterator.next();
            for (String partition : entry.getValue().split(",")){
                if (!brokersList.contains(partition)) brokersList.add(partition);
            }
            for (String broker : brokersList){
                final List<String> partitionList = result.get(broker) != null ? result.get(broker) : new ArrayList<String>();
                partitionList.add(entry.getKey());
                result.remove(broker);
                result.put(broker,partitionList);
            }
        }
        return result;
    }

    /**
     *  remove start and end char for String
     * @param partition
     * @return
     */
    private static String removeStartAndEndChar(String partition) {
        return partition.substring(1, partition.length() - 1);
    }

    private static int judgeReplication(String parition, int replication) {
        final int length = parition.split(",").length;
        return length > replication ? length : replication;
    }

    /**
     *  accumulate one topic partitions sum
     * @param data
     * @param brokersPartitions
     *
     *
     */
    private static void accumulateBrokerPartitions(String data,
                                                   Map<String, Integer> brokersPartitions) {
        final String field = removeStartAndEndChar(data);
        final String[] keys = field.split(",");
        for (String key : keys) {
            if (brokersPartitions.containsKey(key)) {
                int count = brokersPartitions.get(key);
                brokersPartitions.put(key, count + 1);
            } else {
                brokersPartitions.put(key, 1);
            }
        }

    }

    private static String convertListToString(List<String> lists) {
        final StringBuffer stringBuffer = new StringBuffer();
        final Iterator<String> iterator = lists.iterator();
        while (iterator.hasNext()) {
            stringBuffer.append("/").append(iterator.next());
        }
        return stringBuffer.toString();
    }

}
