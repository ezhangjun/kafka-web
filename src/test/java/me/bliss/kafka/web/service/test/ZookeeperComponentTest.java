package me.bliss.kafka.web.service.test;

import me.bliss.kafka.web.component.ZookeeperComponent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 *
 *
 * @author lanjue
 * @version $Id: me.bliss.kafka.web.service.test, v 0.1 3/3/15
 *          Exp $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration("file:src/main/webapp/WEB-INF/mvc-dispatcher-servlet.xml")
public class ZookeeperComponentTest {


    @Test
    public void testGetBroker(){
        ZookeeperComponent.getBrokers();
    }

    @Test
    public void testGetTopicDetail(){
        ZookeeperComponent.getTopicDetail("build");
    }
}
