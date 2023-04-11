package core.cache;

import java.lang.annotation.*;

/**
 * @className: core.cache-> LocalCache
 * @description: 本地cache缓存，使用ehcache缓存，
 * @author: raoty
 * @Date: 2023-04-03 10:04
 * @version: V1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LocalCache {
    //key
    String key() default "";

    //key生成方法
    String keyGenerator() default "";

    //缓存失效时间,默认不失效,单位秒
    int cacheExpireTime() default -1;

    /**
     * ehcache缓存name
     * @return
     */
    String cacheName() default "local";

    /*
    是否初始化等待（缓存数据未初始化时等待数据初始化完成）
    默认false-不同步,返回null
     */
    boolean waitInit() default false;

    /*
    等待同步的时间间隔，单位ms,默认100ms,配置必须大于10ms
    waitUntilCached为true时生效
     */
    long retryInterval() default 100L;

    /*
    等待同步次数，默认3次
    waitUntilCached为true时生效
     */
    int maxRetryTimes() default 3;
}
