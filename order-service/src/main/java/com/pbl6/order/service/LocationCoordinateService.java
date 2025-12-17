package com.pbl6.order.service;

import com.pbl6.order.constant.DistrictEnum;
import com.pbl6.order.constant.WardEnum;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class LocationCoordinateService {

  private static final Map<Integer, Coordinate> DISTRICT_COORDINATES = buildDistrictCoordinates();
  private static final Map<Integer, Coordinate> WARD_COORDINATES = buildWardCoordinates();

  public Optional<Coordinate> findDistrictCoordinate(Integer districtCode) {
    if (districtCode == null) return Optional.empty();
    return Optional.ofNullable(DISTRICT_COORDINATES.get(districtCode));
  }

  public Optional<Coordinate> findWardCoordinate(Integer wardCode) {
    if (wardCode == null) return Optional.empty();
    return Optional.ofNullable(WARD_COORDINATES.get(wardCode));
  }

  public record Coordinate(double lat, double lng) {}

  private static Map<Integer, Coordinate> buildDistrictCoordinates() {
    Map<Integer, Coordinate> map = new HashMap<>();
    map.put(DistrictEnum.QUAN_LIEN_CHIEU.getCode(), new Coordinate(16.087, 108.151));
    map.put(DistrictEnum.QUAN_THANH_KHE.getCode(), new Coordinate(16.067, 108.196));
    map.put(DistrictEnum.QUAN_HAI_CHAU.getCode(), new Coordinate(16.061, 108.220));
    map.put(DistrictEnum.QUAN_SON_TRA.getCode(), new Coordinate(16.085, 108.246));
    map.put(DistrictEnum.QUAN_NGU_HANH_SON.getCode(), new Coordinate(16.000, 108.252));
    map.put(DistrictEnum.QUAN_CAM_LE.getCode(), new Coordinate(16.032, 108.200));
    map.put(DistrictEnum.HUYEN_HOA_VANG.getCode(), new Coordinate(15.995, 108.100));
    return map;
  }

  private static Map<Integer, Coordinate> buildWardCoordinates() {
    Map<Integer, Coordinate> map = new HashMap<>();

    // Liên Chiểu (16.09x, 108.14x)
    map.put(WardEnum.PHUONG_HOA_HIEP_BAC.getCode(), new Coordinate(16.125, 108.140));
    map.put(WardEnum.PHUONG_HOA_HIEP_NAM.getCode(), new Coordinate(16.110, 108.142));
    map.put(WardEnum.PHUONG_HOA_KHANH_BAC.getCode(), new Coordinate(16.080, 108.150));
    map.put(WardEnum.PHUONG_HOA_KHANH_NAM.getCode(), new Coordinate(16.072, 108.152));
    map.put(WardEnum.PHUONG_HOA_MINH.getCode(), new Coordinate(16.060, 108.160));

    // Thanh Khê (16.06x, 108.17x-108.19x)
    map.put(WardEnum.PHUONG_THANH_KHE_TAY.getCode(), new Coordinate(16.070, 108.180));
    map.put(WardEnum.PHUONG_THANH_KHE_DONG.getCode(), new Coordinate(16.067, 108.186));
    map.put(WardEnum.PHUONG_XUAN_HA.getCode(), new Coordinate(16.069, 108.175));
    map.put(WardEnum.PHUONG_CHINH_GIAN.getCode(), new Coordinate(16.060, 108.182));
    map.put(WardEnum.PHUONG_THAC_GIAN.getCode(), new Coordinate(16.055, 108.185));
    map.put(WardEnum.PHUONG_AN_KHE.getCode(), new Coordinate(16.056, 108.172));

    // Hải Châu (16.04x-16.06x, 108.20x-108.22x)
    map.put(WardEnum.PHUONG_THANH_BINH.getCode(), new Coordinate(16.073, 108.210));
    map.put(WardEnum.PHUONG_THUAN_PHUOC.getCode(), new Coordinate(16.080, 108.216));
    map.put(WardEnum.PHUONG_THACH_THANG.getCode(), new Coordinate(16.068, 108.215));
    map.put(WardEnum.PHUONG_HAI_CHAU.getCode(), new Coordinate(16.065, 108.218));
    map.put(WardEnum.PHUONG_PHUOC_NINH.getCode(), new Coordinate(16.058, 108.217));
    map.put(WardEnum.PHUONG_HOA_THUAN_TAY.getCode(), new Coordinate(16.054, 108.208));
    map.put(WardEnum.PHUONG_BINH_THUAN.getCode(), new Coordinate(16.053, 108.220));
    map.put(WardEnum.PHUONG_HOA_CUONG_BAC.getCode(), new Coordinate(16.050, 108.211));
    map.put(WardEnum.PHUONG_HOA_CUONG_NAM.getCode(), new Coordinate(16.044, 108.212));

    // Sơn Trà (16.07x-16.10x, 108.23x-108.27x)
    map.put(WardEnum.PHUONG_THO_QUANG.getCode(), new Coordinate(16.104, 108.247));
    map.put(WardEnum.PHUONG_NAI_HIEN_DONG.getCode(), new Coordinate(16.095, 108.240));
    map.put(WardEnum.PHUONG_MAN_THAI.getCode(), new Coordinate(16.089, 108.251));
    map.put(WardEnum.PHUONG_AN_HAI_BAC.getCode(), new Coordinate(16.083, 108.244));
    map.put(WardEnum.PHUONG_PHUOC_MY.getCode(), new Coordinate(16.079, 108.246));
    map.put(WardEnum.PHUONG_AN_HAI_NAM.getCode(), new Coordinate(16.075, 108.238));

    // Ngũ Hành Sơn (15.99x-16.02x, 108.24x-108.26x)
    map.put(WardEnum.PHUONG_MY_AN.getCode(), new Coordinate(16.033, 108.243));
    map.put(WardEnum.PHUONG_KHUE_MY.getCode(), new Coordinate(16.020, 108.250));
    map.put(WardEnum.PHUONG_HOA_QUY.getCode(), new Coordinate(16.005, 108.252));
    map.put(WardEnum.PHUONG_HOA_HAI.getCode(), new Coordinate(15.995, 108.255));

    // Cẩm Lệ (16.02x-16.04x, 108.18x-108.21x)
    map.put(WardEnum.PHUONG_KHUE_TRUNG.getCode(), new Coordinate(16.030, 108.185));
    map.put(WardEnum.PHUONG_HOA_PHAT.getCode(), new Coordinate(16.032, 108.195));
    map.put(WardEnum.PHUONG_HOA_AN.getCode(), new Coordinate(16.025, 108.188));
    map.put(WardEnum.PHUONG_HOA_THO_TAY.getCode(), new Coordinate(16.028, 108.204));
    map.put(WardEnum.PHUONG_HOA_THO_DONG.getCode(), new Coordinate(16.024, 108.206));
    map.put(WardEnum.PHUONG_HOA_XUAN.getCode(), new Coordinate(16.018, 108.198));

    // Hòa Vang (15.95x-16.02x, 108.00x-108.14x)
    map.put(WardEnum.XA_HOA_BAC.getCode(), new Coordinate(16.090, 108.030));
    map.put(WardEnum.XA_HOA_LIEN.getCode(), new Coordinate(16.070, 108.060));
    map.put(WardEnum.XA_HOA_NINH.getCode(), new Coordinate(16.030, 108.050));
    map.put(WardEnum.XA_HOA_SON.getCode(), new Coordinate(16.020, 108.070));
    map.put(WardEnum.XA_HOA_NHON.getCode(), new Coordinate(16.010, 108.090));
    map.put(WardEnum.XA_HOA_PHU.getCode(), new Coordinate(16.000, 108.050));
    map.put(WardEnum.XA_HOA_PHONG.getCode(), new Coordinate(15.990, 108.080));
    map.put(WardEnum.XA_HOA_CHAU.getCode(), new Coordinate(15.980, 108.060));
    map.put(WardEnum.XA_HOA_TIEN.getCode(), new Coordinate(15.970, 108.090));
    map.put(WardEnum.XA_HOA_PHUOC.getCode(), new Coordinate(15.960, 108.070));
    map.put(WardEnum.XA_HOA_KHUONG.getCode(), new Coordinate(15.955, 108.100));

    return map;
  }
}
