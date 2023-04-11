package web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import web.service.ProductSrc;

import java.util.List;

/**
 * @className: controller-> Test
 * @description: ehcache
 * @author: raoty
 * @Date: 2023-02-25 21:55
 * @version: V1.0
 */
@RestController
public class TestController {

    @Autowired
    private ProductSrc productSrc;

    @RequestMapping(value = "test3", method = RequestMethod.GET)
    public List test3() {
        List res = productSrc.getProduct3();
        if(res !=null && res.size() < 6000) {
            res.add((res.size() + 3) + "");
        }
        return res;
    }

    @RequestMapping(value = "test4", method = RequestMethod.GET)
    public List test4() {
        List res = productSrc.getProduct4();
        if(res != null){
            res.add((res.size() + 4) + "");
        }
        return res;
    }


    @RequestMapping(value = "test5", method = RequestMethod.GET)
    public List test5() {
        List res = productSrc.getProduct5("asdasd");
        if(res != null){
            res.add((res.size() + 5) + "");
        }
        return res;
    }


    @RequestMapping(value = "test6", method = RequestMethod.GET)
    public List test6() {
        List res = productSrc.getProduct6();
        if(res != null){
            res.add((res.size() + 6) + "");
        }
        return res;
    }


}
