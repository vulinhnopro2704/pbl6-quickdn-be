package com.pbl6.order.entity;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Package category types")
public enum PackageCategory {

  // ─── Hàng thời trang ────────────────────────────────────────
  THOI_TRANG, // Quần áo, giày dép, mũ, phụ kiện
  TRANG_SUC, // Nhẫn, vòng tay, phụ kiện đắt tiền

  // ─── Làm đẹp ────────────────────────────────────────────────
  MY_PHAM, // Mỹ phẩm, skincare
  NUOC_HOA, // Nước hoa (thường cần lưu ý đặc biệt)

  // ─── Thực phẩm ──────────────────────────────────────────────
  THUC_PHAM_KHO, // Mì, bánh kẹo, đồ khô
  THUC_PHAM_TUOI, // Thịt, cá, rau, hải sản
  DO_UONG, // Trà, cà phê, đồ uống đóng chai
  DO_AN_NHAU, // Đồ ăn nấu sẵn (delivery)

  // ─── Đồ công nghệ ───────────────────────────────────────────
  DIEN_TU, // Laptop, điện thoại
  PHU_KIEN_CONG_NGHE, // Sạc, cáp, tai nghe

  // ─── Đồ gia dụng ────────────────────────────────────────────
  GIA_DUNG, // Nồi, bếp, đồ dùng gia đình
  NOI_THAT_NHO, // Ghế nhỏ, đèn, decor nhẹ

  // ─── Giấy tờ ────────────────────────────────────────────────
  TAI_LIEU, // Giấy tờ, hợp đồng
  SACH_VO, // Sách, vở, tài liệu học tập

  // ─── Hàng dễ vỡ, nặng, đặc biệt ────────────────────────────
  DE_VO, // Gốm sứ, thuỷ tinh
  HANG_NANG, // Nặng quá mức thông thường (ví dụ >20kg)
  HANG_CUC_LON, // Kích thước lớn (thùng lớn)

  // ─── Hàng nguy hiểm (có thể cấm) ───────────────────────────
  HOA_CHAT, // Chất lỏng dễ tràn, dễ phản ứng
  DE_CHAY_NO, // Pin lithium, bình gas, pháo
  THUOC, // Thuốc tây, dược phẩm

  // ─── Khác ───────────────────────────────────────────────────
  THU_CUNG, // Thú cưng nhỏ (nhiều đơn vị cho phép)
  QUA_TANG, // Quà, gói quà, hộp quà

  // fallback
  KHAC,
  DEFAULT
}
