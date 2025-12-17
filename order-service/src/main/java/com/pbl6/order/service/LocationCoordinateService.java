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
        // Tọa độ trung tâm các quận (theo Wikipedia Đà Nẵng)
        map.put(DistrictEnum.QUAN_LIEN_CHIEU.getCode(), new Coordinate(16.12389, 108.11778)); // Liên Chiểu:contentReference[oaicite:12]{index=12}
        map.put(DistrictEnum.QUAN_THANH_KHE.getCode(), new Coordinate(16.05778, 108.18333)); // Thanh Khê:contentReference[oaicite:13]{index=13}
        map.put(DistrictEnum.QUAN_HAI_CHAU.getCode(), new Coordinate(16.06056, 108.19611));   // Hải Châu:contentReference[oaicite:14]{index=14}
        map.put(DistrictEnum.QUAN_SON_TRA.getCode(), new Coordinate(16.11600, 108.27700));  // Sơn Trà:contentReference[oaicite:15]{index=15}
        map.put(DistrictEnum.QUAN_NGU_HANH_SON.getCode(), new Coordinate(16.00833, 108.25250)); // Ngũ Hành Sơn:contentReference[oaicite:16]{index=16}
        map.put(DistrictEnum.QUAN_CAM_LE.getCode(), new Coordinate(16.01667, 108.20278));  // Cẩm Lệ:contentReference[oaicite:17]{index=17}
        map.put(DistrictEnum.HUYEN_HOA_VANG.getCode(), new Coordinate(16.06639, 108.02417));  // Hòa Vang:contentReference[oaicite:18]{index=18}
        return map;
    }

    private static Map<Integer, Coordinate> buildWardCoordinates() {
        Map<Integer, Coordinate> map = new HashMap<>();

        // Liên Chiểu (cũ): Hòa Hiệp Bắc/Nam được nhập vào phường Hải Vân (16.1315, 107.9720):contentReference[oaicite:19]{index=19}
        map.put(WardEnum.PHUONG_HOA_HIEP_BAC.getCode(), new Coordinate(16.125, 108.140)); // gần trung tâm Hòa Hiệp
        map.put(WardEnum.PHUONG_HOA_HIEP_NAM.getCode(), new Coordinate(16.110, 108.142)); // gần trung tâm Hòa Hiệp
        // Hòa Khánh Bắc/Nam và Hòa Minh nhập vào phường Hòa Khánh (16.0471,108.1310):contentReference[oaicite:20]{index=20}
        map.put(WardEnum.PHUONG_HOA_KHANH_BAC.getCode(), new Coordinate(16.04710, 108.13100));
        map.put(WardEnum.PHUONG_HOA_KHANH_NAM.getCode(), new Coordinate(16.04710, 108.13100));
        map.put(WardEnum.PHUONG_HOA_MINH.getCode(), new Coordinate(16.04710, 108.13100));

        // Thanh Khê (mới): toàn bộ Thanh Khê Tây, Đông, Xuân Hà, Chánh Gián, Thạc Gián, An Khê nhập vào phường Thanh Khê (16.0711,108.1918):contentReference[oaicite:21]{index=21}
        map.put(WardEnum.PHUONG_THANH_KHE_TAY.getCode(), new Coordinate(16.07109, 108.19180));
        map.put(WardEnum.PHUONG_THANH_KHE_DONG.getCode(), new Coordinate(16.07109, 108.19180));
        map.put(WardEnum.PHUONG_XUAN_HA.getCode(),    new Coordinate(16.07109, 108.19180));
        map.put(WardEnum.PHUONG_CHINH_GIAN.getCode(),  new Coordinate(16.07109, 108.19180));
        map.put(WardEnum.PHUONG_THAC_GIAN.getCode(),   new Coordinate(16.07109, 108.19180));
        map.put(WardEnum.PHUONG_AN_KHE.getCode(),      new Coordinate(16.03670, 108.17900)); // An Khê (cũ):contentReference[oaicite:22]{index=22}

        // Hải Châu (cũ): giữ các phường truyền thống trong quận Hải Châu (không thay đổi lớn)
        map.put(WardEnum.PHUONG_THANH_BINH.getCode(),  new Coordinate(16.073, 108.210)); // tham khảo vị trí trung tâm phường
        map.put(WardEnum.PHUONG_THUAN_PHUOC.getCode(), new Coordinate(16.080, 108.216));
        map.put(WardEnum.PHUONG_THACH_THANG.getCode(), new Coordinate(16.068, 108.215));
        map.put(WardEnum.PHUONG_HAI_CHAU.getCode(),     new Coordinate(16.065, 108.218));
        map.put(WardEnum.PHUONG_PHUOC_NINH.getCode(),   new Coordinate(16.058, 108.217));
        map.put(WardEnum.PHUONG_HOA_THUAN_TAY.getCode(),new Coordinate(16.054, 108.208));
        map.put(WardEnum.PHUONG_BINH_THUAN.getCode(),   new Coordinate(16.053, 108.220));
        map.put(WardEnum.PHUONG_HOA_CUONG_BAC.getCode(),new Coordinate(16.050, 108.211));
        map.put(WardEnum.PHUONG_HOA_CUONG_NAM.getCode(),new Coordinate(16.044, 108.212));

        // Sơn Trà: các phường truyền thống
        map.put(WardEnum.PHUONG_THO_QUANG.getCode(),   new Coordinate(16.104, 108.247));
        map.put(WardEnum.PHUONG_NAI_HIEN_DONG.getCode(),new Coordinate(16.095, 108.240));
        map.put(WardEnum.PHUONG_MAN_THAI.getCode(),     new Coordinate(16.089, 108.251));
        map.put(WardEnum.PHUONG_AN_HAI_BAC.getCode(),   new Coordinate(16.083, 108.244));
        map.put(WardEnum.PHUONG_PHUOC_MY.getCode(),     new Coordinate(16.079, 108.246));
        map.put(WardEnum.PHUONG_AN_HAI_NAM.getCode(),   new Coordinate(16.075, 108.238));

        // Ngũ Hành Sơn (cũ): Mỹ An, Khuê Mỹ, Hòa Quý, Hòa Hải nhập vào phường Ngũ Hành Sơn (15.9980,108.2490):contentReference[oaicite:23]{index=23}
        map.put(WardEnum.PHUONG_MY_AN.getCode(),       new Coordinate(16.00833, 108.25250));
        map.put(WardEnum.PHUONG_KHUE_MY.getCode(),     new Coordinate(16.00833, 108.25250));
        map.put(WardEnum.PHUONG_HOA_QUY.getCode(),     new Coordinate(16.00833, 108.25250));
        map.put(WardEnum.PHUONG_HOA_HAI.getCode(),     new Coordinate(16.00833, 108.25250));

        // Cẩm Lệ: các phường truyền thống
        map.put(WardEnum.PHUONG_KHUE_TRUNG.getCode(), new Coordinate(16.030, 108.185));
        map.put(WardEnum.PHUONG_HOA_PHAT.getCode(),   new Coordinate(16.032, 108.195));
        map.put(WardEnum.PHUONG_HOA_AN.getCode(),     new Coordinate(16.025, 108.188));
        map.put(WardEnum.PHUONG_HOA_THO_TAY.getCode(),new Coordinate(16.028, 108.204));
        map.put(WardEnum.PHUONG_HOA_THO_DONG.getCode(),new Coordinate(16.024, 108.206));
        map.put(WardEnum.PHUONG_HOA_XUAN.getCode(),   new Coordinate(16.018, 108.198));

        // Hòa Vang: các xã vùng nội thành (vẫn trên đất liền)
        map.put(WardEnum.XA_HOA_BAC.getCode(),   new Coordinate(16.090, 108.030));
        map.put(WardEnum.XA_HOA_LIEN.getCode(),  new Coordinate(16.070, 108.060));
        map.put(WardEnum.XA_HOA_NINH.getCode(),  new Coordinate(16.030, 108.050));
        map.put(WardEnum.XA_HOA_SON.getCode(),   new Coordinate(16.020, 108.070));
        map.put(WardEnum.XA_HOA_NHON.getCode(),  new Coordinate(16.010, 108.090));
        map.put(WardEnum.XA_HOA_PHU.getCode(),   new Coordinate(16.000, 108.050));
        map.put(WardEnum.XA_HOA_PHONG.getCode(), new Coordinate(15.990, 108.080));
        map.put(WardEnum.XA_HOA_CHAU.getCode(),  new Coordinate(15.980, 108.060));
        map.put(WardEnum.XA_HOA_TIEN.getCode(),  new Coordinate(15.970, 108.090));
        map.put(WardEnum.XA_HOA_PHUOC.getCode(), new Coordinate(15.960, 108.070));
        map.put(WardEnum.XA_HOA_KHUONG.getCode(),new Coordinate(15.955, 108.100));

        return map;
    }
}

