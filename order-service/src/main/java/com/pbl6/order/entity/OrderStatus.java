package com.pbl6.order.entity;

public enum OrderStatus {
  PENDING,
  CONFIRMED,
  PICKED,
  CANCELLED,
  // 4 cái phía trên sẽ remove sau khi push code lên live

  // Driver assignment – Giai đoạn tìm tài xế
  PENDING_PAYMENT, // Chờ thanh toán (online)
  FINDING_DRIVER, // Đang tìm tài xế
  DRIVER_ASSIGNED, // Đã có tài xế nhận đơn

  // Pickup phase – Giai đoạn lấy hàng
  DRIVER_EN_ROUTE_PICKUP, // Tài xế đang đến điểm lấy
  ARRIVED_PICKUP, // Tài xế đã đến điểm lấy
  PICKUP_ATTEMPT_FAILED, // Không liên lạc được người gửi
  PICKUP_FAILED, // Không lấy được hàng
  PACKAGE_PICKED, // Đã lấy hàng

  // Delivery phase – Giai đoạn giao hàng
  EN_ROUTE_DELIVERY, // Đang giao hàng
  ARRIVED_DELIVERY, // Tài xế đã đến điểm giao
  DELIVERY_ATTEMPT_FAILED, // Không liên lạc được người nhận
  DELIVERY_FAILED, // Giao thất bại
  DELIVERED, // Giao thành công

  // Return – Hoàn trả
  RETURNING_TO_SENDER, // Đang hoàn trả hàng về
  RETURNED, // Đã trả hàng lại cho người gửi

  // Special cases – Các trường hợp đặc biệt
  DRIVER_ISSUE_REPORTED, // Tài xế gặp sự cố
  REASSIGNING_DRIVER, // Đang đổi tài xế

  // Cancel – Hủy đơn
  CANCELLED_BY_SENDER, // Người gửi hủy
  CANCELLED_BY_DRIVER, // Tài xế hủy
  CANCELLED_NO_DRIVER, // Hệ thống không tìm được tài xế
  ORDER_CANCELLED // Đơn hàng bị hủy (trạng thái kết thúc)
}
