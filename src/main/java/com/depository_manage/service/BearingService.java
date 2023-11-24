package com.depository_manage.service;

import com.depository_manage.entity.Bearing;

import java.util.List;

public interface BearingService {
    Bearing getBearingByBoxText(String boxText);
    List<Bearing> getAllBearings();
    Bearing getLatestBearingByBoxText(String boxText);
    void saveBearing(Bearing bearing);

    Integer calculateQuantity(String boxText);
}
