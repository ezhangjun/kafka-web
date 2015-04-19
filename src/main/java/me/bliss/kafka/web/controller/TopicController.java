package me.bliss.kafka.web.controller;

import me.bliss.kafka.web.component.ZookeeperComponent;
import me.bliss.kafka.web.component.model.Topic;
import me.bliss.kafka.web.exception.SimpleConsumerLogicException;
import me.bliss.kafka.web.service.TopicService;
import org.apache.zookeeper.KeeperException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

/**
 *
 *
 * @author lanjue
 * @version $Id: me.bliss.kafka.web.controller, v 0.1 4/4/15
 *          Exp $
 */
@Controller
@RequestMapping(value = "/topic")
public class TopicController {

    @Autowired
    private TopicService topicService;

    @Autowired
    private ZookeeperComponent zookeeperComponent;

    @RequestMapping(value = "/list",method = RequestMethod.GET)
    @ResponseBody
    public List<Topic> getList(){
        try {
            return topicService.getAllTopics();
        } catch (KeeperException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SimpleConsumerLogicException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value = "/messages",method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Map<Integer, List<String>>> getAllMessages(){
        return topicService.getMessage();
    }

    public void setTopicService(TopicService topicService) {
        this.topicService = topicService;
    }

    public void setZookeeperComponent(ZookeeperComponent zookeeperComponent) {
        this.zookeeperComponent = zookeeperComponent;
    }
}
