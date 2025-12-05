package com.pbl6.order.dto;

import java.util.Map;

public class NotifyPayload {
    public String title;
    public String body;
    public Map<String, String> data;

    public NotifyPayload(String title, String body, Map<String, String> data) {
        this.title = title;
        this.body = body;
        this.data = data;
    }
}
