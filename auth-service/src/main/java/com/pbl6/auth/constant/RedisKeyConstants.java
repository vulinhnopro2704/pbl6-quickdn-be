package com.pbl6.auth.constant;

public final class RedisKeyConstants {
    private RedisKeyConstants() {}

    public static final String DRIVERS_GEO_KEY = "drivers:geo";
    public static final String DRIVER_HASH_PREFIX = "driver:"; // full key = driver:{driverId}
    public static final String ORDER_OFFERS_PREFIX = "order:%s:offers"; // String.format
    public static final String ORDER_STATE_PREFIX = "order:%s:state";
    public static final String DRIVER_FCM_TOKEN = "driver:%s:fcm_token";

}

