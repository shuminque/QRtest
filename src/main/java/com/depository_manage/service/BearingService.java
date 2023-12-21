package com.depository_manage.service;

import com.depository_manage.entity.Bearing;

import java.util.List;

public interface BearingService {
    Bearing getBearingByBoxTextAndDepository(String boxText, String depository);
    List<Bearing> getAllBearings();
    Bearing getLatestBearingByBoxText(String boxText);
    void saveBearing(Bearing bearing);

    Integer calculateQuantity(String boxText, String depository);
    List<String> searchBoxText(String query, String depository);

}
