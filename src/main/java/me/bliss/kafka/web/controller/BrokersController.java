package me.bliss.kafka.web.controller;

import me.bliss.kafka.web.result.ServiceResult;
import me.bliss.kafka.web.service.ZookeeperService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

/**
 *
 *
 * @author lanjue
 * @version $Id: me.bliss.kafka.web.controller, v 0.1 3/3/15
 *          Exp $
 */
@Controller
public class BrokersController {

    @RequestMapping(value = "/brokers",method = RequestMethod.GET)
    public String detail(ModelMap modelMap){
        final ServiceResult serviceResult = ZookeeperService.getBrokers();
        modelMap.put("brokers",serviceResult.getResult());
        return "brokers";
    }

    @RequestMapping(value = "/topics",method = RequestMethod.GET)
    public String topic(ModelMap modelMap){
        final ServiceResult<Map<String,Object>> serviceResult = ZookeeperService.getBrokers();
        modelMap.put("topics",serviceResult.getResult().get("topics"));
        return "topics";
    }
}
