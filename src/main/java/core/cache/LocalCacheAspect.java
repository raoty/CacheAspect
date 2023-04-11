package core.cache;

import com.google.gson.Gson;
import core.utils.Util;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * @className: core.cache-> LocalCacheAspect
 * @description: ehcache缓存切面实现
 * @author: raoty
 * @Date: 2023-04-03 10:18
 * @version: V1.0
 */
@Aspect
@Component
public class LocalCacheAspect {
    Gson gson = new Gson();
    ConcurrentHashMap<String, AtomicBoolean> concurrentHashMap = new ConcurrentHashMap();
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Value("${LocalCache.cacheDebug:false}")
    private boolean cacheDebug;

    CacheManager ehCacheManager = CacheManager.newInstance(this.getClass().getResourceAsStream("/config/cache/local_ehcache.xml"));

    @Pointcut("execution(* *(..)) && @annotation(localCache)")
    public void localCache(LocalCache localCache) {
    }


    /**
     * 本地缓存注解
     * 1.从cache中获取数据
     * 2.判断是否首页加载
     *  若是首次加载
     *      - 判断是否需要初始化等待
     *          需要初始化等待，则执行初始化等待
     *      - 否则直接返回null
     *  若不是，则执行步骤3
     * 3.判断缓存的时间是否过期
     *  若过期，则抢到线程锁的线程执行缓存数据更新，没抢到的返回上次缓存的数据
     *  若没过期，则直接返回缓存
     *
     * @param joinPoint  切面
     * @param localCache 缓存注解
     * @return 缓存数据，没有缓存返回null
     * @throws Throwable
     */
    @Around("localCache(localCache)")
    public Object localCacheProcess(ProceedingJoinPoint joinPoint, LocalCache localCache) throws Throwable {
        long t1 = System.currentTimeMillis();
        String cacheKey = this.generateCacheKey(joinPoint, localCache.key(),localCache.keyGenerator(), joinPoint.getArgs());

        Cache cache = ehCacheManager.getCache(localCache.cacheName());
        Element e = cache.get(cacheKey);
        concurrentHashMap.putIfAbsent(cacheKey, new AtomicBoolean(false));

        if (e == null) {
            if (tryLock(cacheKey)) {
                //抢到锁，执行数据初始化
                try {
                    e = loadCache(joinPoint, cacheKey, cache);
                } finally {
                    unlock(cacheKey);
                }
            } else if (localCache.waitInit()) {
                doWait(localCache.maxRetryTimes(), localCache.retryInterval(), cacheKey);
                e = cache.get(cacheKey);
            }
        } else if (isExpireTime(e,localCache.cacheExpireTime()) && tryLock(cacheKey)) {
            try {
                e = loadCache(joinPoint, cacheKey, cache);
            } finally {
                unlock(cacheKey);
            }
        }

        Object res = e == null ? null : e.getObjectValue();
        if (cacheDebug) {
            logger.info("----LocalCache:" + e + "on time:" + (System.currentTimeMillis() - t1));
        }
        return res;
    }

    /**
     * 检测是否超时，
     * @param e 缓存对象
     * @param expireTime 超时时间，单位秒
     * @return
     */
    private boolean isExpireTime(Element e,int expireTime) {
        return System.currentTimeMillis()/1000 - e.getCreationTime()/1000 > expireTime;
    }

    /**
     * 获取锁
     * @param cacheKey
     * @return true-获取锁成功；false-获取锁失败
     */
    private boolean tryLock(String cacheKey) {
        return concurrentHashMap.get(cacheKey).compareAndSet(false, true);
    }

    /**
     * 释放锁
     * @param cacheKey 缓存key
     */
    private void unlock(String cacheKey) {
        concurrentHashMap.get(cacheKey).set(false);
    }

    /**
     * 重试等待
     * @param maxRetryTimes 最大重试次数
     * @param retryInterval 等待时间
     * @param cacheKey      缓存key
     * @throws InterruptedException
     */
    private void doWait(int maxRetryTimes, long retryInterval, String cacheKey) throws InterruptedException {
        //重试次数，最少1
        maxRetryTimes = maxRetryTimes < 1 ? 1 : maxRetryTimes;
        //等待时间，最后100ms
        retryInterval = retryInterval < 100 ? 100 : retryInterval;
        while (concurrentHashMap.get(cacheKey).get() && maxRetryTimes > 0) {
            maxRetryTimes--;
            //等待时间，100ms
            Thread.sleep(retryInterval);
        }
    }

    /**
     * 加载缓存
     * @param joinPoint 切面
     * @param cacheKey 缓存key
     * @param cache 缓存容器
     * @return
     * @throws Throwable
     */
    private Element loadCache(ProceedingJoinPoint joinPoint, String cacheKey, Cache cache) throws Throwable {
        Element e = new Element(cacheKey, joinPoint.proceed());
        cache.put(e);
        return e;
    }


    /**
     * 生成缓存key
     * @param joinPoint      切面
     * @param key 缓存key
     * @param keyGenerator 缓存名称获取方法
     * @param args           切面参数
     * @return 缓存key
     */
    private String generateCacheKey(ProceedingJoinPoint joinPoint,String key, String keyGenerator, Object[] args) throws Exception {
        String cacheKey;

        if (!Util.isNull(keyGenerator)) {
            cacheKey = key;
        }else if (!Util.isNull(keyGenerator)) {
            Method joinMethod = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Class[] argClasses = joinMethod.getParameterTypes();
            Method method = joinPoint.getTarget().getClass()
                    .getDeclaredMethod(keyGenerator, argClasses);
            method.setAccessible(true);
            cacheKey = (String) method.invoke(joinPoint.getTarget(), args);
        } else {
            cacheKey = new StringBuilder(joinPoint.getSignature().toString())
                    .append(".").append(getParamsUuid(args)).toString()
                    .replaceAll("\\s+", ".");
            if (cacheDebug) {
                logger.info("----cacheKey:" + cacheKey);
            }
        }
        return cacheKey;
    }

    /**
     * @param args
     * @return
     */
    private String getParamsUuid(Object[] args) {
        return UUID.nameUUIDFromBytes(gson.toJson(args).getBytes()).toString().replace("-", "");
    }
}
