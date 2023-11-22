package com.depository_manage.service;

import com.depository_manage.entity.Bearing;

import java.util.List;

public interface BearingService {
    Bearing getBearingByBoxNumber(String boxNumber);
    List<Bearing> getAllBearings();
    Bearing getLatestBearingByBoxNumber(String boxNumber);
    void saveBearing(Bearing bearing);

}
