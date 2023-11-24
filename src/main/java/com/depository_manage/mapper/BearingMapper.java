package com.depository_manage.mapper;

import com.depository_manage.entity.Bearing;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BearingMapper {
    Bearing selectBearingByBoxText(String boxText);
    List<Bearing> selectAllBearings();
    Bearing selectLatestBearingByBoxText(String boxText);
    void insertBearing(Bearing bearing);

    Integer calculateTotalQuantityByBoxText(String boxText);

}
