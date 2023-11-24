package com.depository_manage.service.impl;

import com.depository_manage.entity.Bearing;
import com.depository_manage.mapper.BearingMapper;
import com.depository_manage.service.BearingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BearingServiceImpl implements BearingService {
    @Autowired
    private BearingMapper bearingMapper;

    @Override
    public Bearing getBearingByBoxText(String boxText) {
        return bearingMapper.selectBearingByBoxText(boxText);
    }

    @Override
    public List<Bearing> getAllBearings() {
        return bearingMapper.selectAllBearings();
    }
    @Override
    public Bearing getLatestBearingByBoxText(String boxText) {
        return bearingMapper.selectLatestBearingByBoxText(boxText);
    }
    @Override
    public void saveBearing(Bearing bearing) {
        bearingMapper.insertBearing(bearing);
    }
    @Override
    public Integer calculateQuantity(String boxText) {
        // 调用 mapper 方法来获取特定 boxNumber 的 quantity 总和
        return bearingMapper.calculateTotalQuantityByBoxText(boxText);
    }
}
