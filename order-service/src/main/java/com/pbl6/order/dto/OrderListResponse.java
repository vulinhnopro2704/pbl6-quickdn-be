package com.pbl6.order.dto;

import java.util.List;

public record OrderListResponse(List<OrderListItemResponse> orders) {}
