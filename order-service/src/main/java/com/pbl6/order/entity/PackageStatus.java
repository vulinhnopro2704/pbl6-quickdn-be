package com.pbl6.order.entity;

public enum PackageStatus {
  // Pickup state
  WAITING_FOR_PICKUP, // Chờ được lấy
  PICKED_UP, // Đã lấy thành công
  PICKUP_ATTEMPT_FAILED, // Không liên lạc được người gửi / thử lấy thất bại
  PICKUP_FAILED, // Lấy thất bại

  // Delivery state
  WAITING_FOR_DELIVERY, // Chờ giao
  DELIVERY_IN_PROGRESS, // Đang giao
  DELIVERY_ATTEMPT_FAILED, // Không liên lạc được người nhận
  DELIVERY_FAILED, // Giao thất bại
  DELIVERED, // Giao thành công

  // Returning
  RETURNING, // Đang hoàn trả về người gửi
  RETURNED, // Hoàn trả thành công

  // Cancelled
  CANCELLED // Bị hủy (bởi sender/driver/system)
}
