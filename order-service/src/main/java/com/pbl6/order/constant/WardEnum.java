package com.pbl6.order.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Administrative wards/communes of Da Nang. Keys are UPPER_SNAKE_CASE, codes follow provided mapping.
 */
public enum WardEnum {
  // Quận Liên Chiểu
  PHUONG_HOA_HIEP_BAC(101, "Phường Hòa Hiệp Bắc", DistrictEnum.QUAN_LIEN_CHIEU),
  PHUONG_HOA_HIEP_NAM(102, "Phường Hòa Hiệp Nam", DistrictEnum.QUAN_LIEN_CHIEU),
  PHUONG_HOA_KHANH_BAC(103, "Phường Hòa Khánh Bắc", DistrictEnum.QUAN_LIEN_CHIEU),
  PHUONG_HOA_KHANH_NAM(104, "Phường Hòa Khánh Nam", DistrictEnum.QUAN_LIEN_CHIEU),
  PHUONG_HOA_MINH(105, "Phường Hòa Minh", DistrictEnum.QUAN_LIEN_CHIEU),

  // Quận Thanh Khê
  PHUONG_THANH_KHE_TAY(201, "Phường Thanh Khê Tây", DistrictEnum.QUAN_THANH_KHE),
  PHUONG_THANH_KHE_DONG(202, "Phường Thanh Khê Đông", DistrictEnum.QUAN_THANH_KHE),
  PHUONG_XUAN_HA(203, "Phường Xuân Hà", DistrictEnum.QUAN_THANH_KHE),
  PHUONG_CHINH_GIAN(204, "Phường Chính Gián", DistrictEnum.QUAN_THANH_KHE),
  PHUONG_THAC_GIAN(205, "Phường Thạc Gián", DistrictEnum.QUAN_THANH_KHE),
  PHUONG_AN_KHE(206, "Phường An Khê", DistrictEnum.QUAN_THANH_KHE),

  // Quận Hải Châu
  PHUONG_THANH_BINH(301, "Phường Thanh Bình", DistrictEnum.QUAN_HAI_CHAU),
  PHUONG_THUAN_PHUOC(302, "Phường Thuận Phước", DistrictEnum.QUAN_HAI_CHAU),
  PHUONG_THACH_THANG(303, "Phường Thạch Thang", DistrictEnum.QUAN_HAI_CHAU),
  PHUONG_HAI_CHAU(304, "Phường Hải Châu", DistrictEnum.QUAN_HAI_CHAU),
  PHUONG_PHUOC_NINH(305, "Phường Phước Ninh", DistrictEnum.QUAN_HAI_CHAU),
  PHUONG_HOA_THUAN_TAY(306, "Phường Hòa Thuận Tây", DistrictEnum.QUAN_HAI_CHAU),
  PHUONG_BINH_THUAN(307, "Phường Bình Thuận", DistrictEnum.QUAN_HAI_CHAU),
  PHUONG_HOA_CUONG_BAC(308, "Phường Hòa Cường Bắc", DistrictEnum.QUAN_HAI_CHAU),
  PHUONG_HOA_CUONG_NAM(309, "Phường Hòa Cường Nam", DistrictEnum.QUAN_HAI_CHAU),

  // Quận Sơn Trà
  PHUONG_THO_QUANG(401, "Phường Thọ Quang", DistrictEnum.QUAN_SON_TRA),
  PHUONG_NAI_HIEN_DONG(402, "Phường Nại Hiên Đông", DistrictEnum.QUAN_SON_TRA),
  PHUONG_MAN_THAI(403, "Phường Mân Thái", DistrictEnum.QUAN_SON_TRA),
  PHUONG_AN_HAI_BAC(404, "Phường An Hải Bắc", DistrictEnum.QUAN_SON_TRA),
  PHUONG_PHUOC_MY(405, "Phường Phước Mỹ", DistrictEnum.QUAN_SON_TRA),
  PHUONG_AN_HAI_NAM(406, "Phường An Hải Nam", DistrictEnum.QUAN_SON_TRA),

  // Quận Ngũ Hành Sơn
  PHUONG_MY_AN(501, "Phường Mỹ An", DistrictEnum.QUAN_NGU_HANH_SON),
  PHUONG_KHUE_MY(502, "Phường Khuê Mỹ", DistrictEnum.QUAN_NGU_HANH_SON),
  PHUONG_HOA_QUY(503, "Phường Hòa Quý", DistrictEnum.QUAN_NGU_HANH_SON),
  PHUONG_HOA_HAI(504, "Phường Hòa Hải", DistrictEnum.QUAN_NGU_HANH_SON),

  // Quận Cẩm Lệ
  PHUONG_KHUE_TRUNG(601, "Phường Khuê Trung", DistrictEnum.QUAN_CAM_LE),
  PHUONG_HOA_PHAT(602, "Phường Hòa Phát", DistrictEnum.QUAN_CAM_LE),
  PHUONG_HOA_AN(603, "Phường Hòa An", DistrictEnum.QUAN_CAM_LE),
  PHUONG_HOA_THO_TAY(604, "Phường Hòa Thọ Tây", DistrictEnum.QUAN_CAM_LE),
  PHUONG_HOA_THO_DONG(605, "Phường Hòa Thọ Đông", DistrictEnum.QUAN_CAM_LE),
  PHUONG_HOA_XUAN(606, "Phường Hòa Xuân", DistrictEnum.QUAN_CAM_LE),

  // Huyện Hòa Vang
  XA_HOA_BAC(701, "Xã Hòa Bắc", DistrictEnum.HUYEN_HOA_VANG),
  XA_HOA_LIEN(702, "Xã Hòa Liên", DistrictEnum.HUYEN_HOA_VANG),
  XA_HOA_NINH(703, "Xã Hòa Ninh", DistrictEnum.HUYEN_HOA_VANG),
  XA_HOA_SON(704, "Xã Hòa Sơn", DistrictEnum.HUYEN_HOA_VANG),
  XA_HOA_NHON(705, "Xã Hòa Nhơn", DistrictEnum.HUYEN_HOA_VANG),
  XA_HOA_PHU(706, "Xã Hòa Phú", DistrictEnum.HUYEN_HOA_VANG),
  XA_HOA_PHONG(707, "Xã Hòa Phong", DistrictEnum.HUYEN_HOA_VANG),
  XA_HOA_CHAU(708, "Xã Hòa Châu", DistrictEnum.HUYEN_HOA_VANG),
  XA_HOA_TIEN(709, "Xã Hòa Tiến", DistrictEnum.HUYEN_HOA_VANG),
  XA_HOA_PHUOC(710, "Xã Hòa Phước", DistrictEnum.HUYEN_HOA_VANG),
  XA_HOA_KHUONG(711, "Xã Hòa Khương", DistrictEnum.HUYEN_HOA_VANG);

  private final int code;
  private final String fullName;
  private final DistrictEnum district;

  WardEnum(int code, String fullName, DistrictEnum district) {
    this.code = code;
    this.fullName = fullName;
    this.district = district;
  }

  public int getCode() {
    return code;
  }

  public String getFullName() {
    return fullName;
  }

  public DistrictEnum getDistrict() {
    return district;
  }

  private static final Map<Integer, WardEnum> LOOKUP =
      Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(WardEnum::getCode, e -> e));

  public static Optional<WardEnum> fromCode(Integer code) {
    if (code == null) return Optional.empty();
    return Optional.ofNullable(LOOKUP.get(code));
  }
}
