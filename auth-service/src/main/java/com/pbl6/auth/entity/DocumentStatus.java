package com.pbl6.auth.entity;

public enum DocumentStatus {
    PENDING,   // chờ phê duyệt
    APPROVED,  // đã phê duyệt
    REJECTED   // bị từ chối (kèm note)
}