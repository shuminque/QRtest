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
    public Bearing getBearingByBoxNumber(String boxNumber) {
        return bearingMapper.selectBearingByBoxNumber(boxNumber);
    }

    @Override
    public List<Bearing> getAllBearings() {
        return bearingMapper.selectAllBearings();
    }
}
