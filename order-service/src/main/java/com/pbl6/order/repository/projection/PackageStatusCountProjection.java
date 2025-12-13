package com.pbl6.order.repository.projection;

import com.pbl6.order.entity.PackageStatus;

public interface PackageStatusCountProjection {
  PackageStatus getStatus();
  long getCount();
}
