package com.pbl6.order.constant;

public final class RedisKeyConstants {
    private RedisKeyConstants() {}

    public static final String DRIVERS_GEO_KEY = "drivers:geo";
    public static final String DRIVER_HASH_PREFIX = "driver:"; // full key = driver:{driverId}
    public static final String ORDER_OFFERS_PREFIX = "order:%s:offers"; // String.format
    public static final String ORDER_STATE_PREFIX = "order:%s:state";
    public static final String DRIVER_FCM_TOKEN = "driver:%s:fcm_token";
    public static final String USER_FCM_TOKEN = "user:%s:fcm_token";
    public static final String ORDER_ASSIGNEE_KEY_PATTERN = "order:%s:assignee";
    public static final String ORDER_CHANNEL_PATTERN = "order:channel:%s"; // optional nếu dùng pub/sub
    public static final String DRIVER_DELIVERING_ORDER_KEY = "driver:%s:delivering_order_id";

}

