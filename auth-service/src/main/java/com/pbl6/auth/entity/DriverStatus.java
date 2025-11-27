package com.pbl6.auth.entity;

public enum DriverStatus {
    PENDING,      // đã đăng ký, chờ phê duyệt
    APPROVED,     // đã phê duyệt, active
    REJECTED,     // bị từ chối
    SUSPENDED,    // tạm đình chỉ
    INACTIVE      // không hoạt động / rời nền tảng
}
