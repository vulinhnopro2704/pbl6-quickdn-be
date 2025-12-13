package com.pbl6.order.dto;

import java.util.List;

public record StatusCountsResponse(StatusCountsRequest.Target target, List<StatusCountItem> data) {}
