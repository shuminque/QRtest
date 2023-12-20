package com.depository_manage.mapper;

import com.depository_manage.entity.Bearing;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BearingMapper {
    Bearing selectBearingByBoxTextAndDepository(@Param("boxText") String boxText,
                                                @Param("depository") String depository);
    List<Bearing> selectAllBearings();
    Bearing selectLatestBearingByBoxText(String boxText);
    void insertBearing(Bearing bearing);

    Integer calculateTotalQuantityByBoxText(@Param("boxText") String boxText,
                                            @Param("depository") String depository);

}
