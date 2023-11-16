package com.depository_manage.mapper;

import com.depository_manage.entity.Bearing;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface BearingMapper {
    Bearing selectBearingByBoxNumber(String boxNumber);
    List<Bearing> selectAllBearings();

}
