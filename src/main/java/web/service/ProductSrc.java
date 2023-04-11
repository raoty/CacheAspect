package web.service;

import core.cache.LocalCache;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @className: service-> ProductService
 * @description: ehcache测试
 * @author: raoty
 * @Date: 2023-02-25 21:49
 * @version: V1.0
 */
@Service
public class ProductSrc {
    protected Log log = LogFactory.getLog(this.getClass());


    /**
     * 测试注入缓存时间，只读，单例
     * 默认缓存时间1min，注入缓存失效时间5min
     *
     * @return
     */
    @LocalCache(cacheName = "product.single", keyGenerator = "key3", cacheExpireTime = 60 * 5)
    public List getProduct3() {
        log.info("come in getProduct3");
        Random random = new Random();
        try {
            Thread.sleep(random.nextInt(3800) + 1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List res = new ArrayList();
        res.add("0");
        res.add("3");
        return res;
    }

    public String key3() {
        return "productService.product3";
    }


    /**
     * 测试注入缓存时间，只读，非单例
     * 默认缓存时间1min，注入缓存失效时间5min,检测不注入 TimeToIdle 是否有问题
     *
     * @return
     */
    @LocalCache(cacheName = "product.mulity", keyGenerator = "key4", cacheExpireTime = 60 * 5)
    public List getProduct4() {
        log.info("come in getProduct4");
        Random random = new Random();
        try {
            Thread.sleep(random.nextInt(3800) + 1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List res = new ArrayList();
        res.add("0");
        res.add("4");

        return res;
    }

    public String key4() {
        return "productService.product4";
    }


    /**
     * 测试默认cachename
     * 自定义命名
     * @return
     */
    @LocalCache(key = "key5", cacheExpireTime = 60 * 2,waitInit=true,retryInterval=500,maxRetryTimes=2)
    public List getProduct5(String str) {
        log.info("come in getProduct5");
        Random random = new Random();
        try {
            Thread.sleep(random.nextInt(3800) + 1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List res = new ArrayList();
        res.add("0");
        res.add("5");
        return res;
    }

    public String key5() {
        return "productService.product5";
    }


    /**
     * 测试等待同步
     * 自定义命名
     *
     * @return
     */
    @LocalCache(cacheExpireTime = 60 * 2,waitInit=true,retryInterval=2000,maxRetryTimes=3)
    public List getProduct6() {
        log.info("come in getProduct6");
        Random random = new Random();
        try {
            Thread.sleep(random.nextInt(3800) + 1200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List res = new ArrayList();
        res.add("0");
        res.add("6");
        return res;
    }
}
