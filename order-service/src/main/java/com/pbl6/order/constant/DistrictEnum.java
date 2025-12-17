package com.pbl6.order.constant;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Administrative districts of Da Nang. Keys are UPPER_SNAKE_CASE, codes follow provided mapping.
 */
public enum DistrictEnum {
  QUAN_LIEN_CHIEU(1, "Quận Liên Chiểu"),
  QUAN_THANH_KHE(2, "Quận Thanh Khê"),
  QUAN_HAI_CHAU(3, "Quận Hải Châu"),
  QUAN_SON_TRA(4, "Quận Sơn Trà"),
  QUAN_NGU_HANH_SON(5, "Quận Ngũ Hành Sơn"),
  QUAN_CAM_LE(6, "Quận Cẩm Lệ"),
  HUYEN_HOA_VANG(7, "Huyện Hòa Vang");

  private final int code;
  private final String fullName;

  DistrictEnum(int code, String fullName) {
    this.code = code;
    this.fullName = fullName;
  }

  public int getCode() {
    return code;
  }

  public String getFullName() {
    return fullName;
  }

  private static final Map<Integer, DistrictEnum> LOOKUP =
      Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(DistrictEnum::getCode, e -> e));

  public static Optional<DistrictEnum> fromCode(Integer code) {
    if (code == null) return Optional.empty();
    return Optional.ofNullable(LOOKUP.get(code));
  }
}
