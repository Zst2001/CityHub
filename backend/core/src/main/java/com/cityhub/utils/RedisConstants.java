package com.cityhub.utils;

public class RedisConstants {
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;

    public static final Long CACHE_NULL_TTL = 2L;

    public static final Long CACHE_ACTIVITY_TTL = 30L;
    public static final Long CACHE_ACTIVITY_TTL_JITTER = 5L;
    public static final String CACHE_ACTIVITY_KEY = "cache:activity:";

    public static final String LOCK_ACTIVITY_KEY = "lock:activity:";
    public static final Long LOCK_ACTIVITY_TTL = 10L;

    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
    public static final String BLOG_LIKED_KEY = "blog:liked:";

    public static final String USER_FOLLOW_KEY = "user:follows:";

    public static final String FEED_KEY = "feed:";
    public static final String ACTIVITY_GEO_KEY = "activity:geo:";
    public static final String USER_SIGN_KEY = "sign:";
}
