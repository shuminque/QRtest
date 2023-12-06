package com.depository_manage.service.impl;

import com.depository_manage.entity.BearingRecord;
import com.depository_manage.mapper.BearingRecordMapper;
import com.depository_manage.service.BearingRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BearingRecordServiceImpl implements BearingRecordService {

    @Autowired
    private BearingRecordMapper bearingRecordMapper;

    @Override
    public void addBearingRecord(BearingRecord record) {
        bearingRecordMapper.insertBearingRecord(record);
    }

    @Override
    public void updateBearingRecord(BearingRecord record) {
        bearingRecordMapper.updateBearingRecord(record);
    }

    @Override
    public void deleteBearingRecordById(int id) {
        bearingRecordMapper.deleteBearingRecordById(id);
    }

    @Override
    public BearingRecord getBearingRecordById(int id) {
        return bearingRecordMapper.selectBearingRecordById(id);
    }

    @Override
    public List<BearingRecord> getAllBearingRecords() {
        return bearingRecordMapper.selectAllBearingRecords();
    }
}
