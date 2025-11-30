package com.pbl6.order.dto;

import java.util.List;

public record DistanceMatrixResponse(List<Row> rows) {
  public record Row(List<Element> elements) {}

  public record Element(ValueText distance, ValueText duration, String status) {}

  public record ValueText(String text, int value) {}
}
