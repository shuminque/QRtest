package com.depository_manage.service.impl;

import com.depository_manage.entity.BearingRecord;
import com.depository_manage.mapper.BearingRecordMapper;
import com.depository_manage.service.BearingRecordService;
import com.depository_manage.utils.ObjectFormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

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
    public List<BearingRecord> filterBearingRecords(Map<String, Object> params) {
        Integer size = 8, page = 1;
        if (params.containsKey("size")) {
            size = ObjectFormatUtil.toInteger(params.get("size"));
            params.put("size", size);
        }
        if (params.containsKey("page")) {
            page = ObjectFormatUtil.toInteger(params.get("page"));
            params.put("begin", (page - 1) * size);
        }
        return bearingRecordMapper.selectAllBearingRecords(params);
    }

    @Override
    public boolean hasSpecialRecord(String boxText, String boxNumber, String depository, int quantity, int iter) {
        return bearingRecordMapper.hasSpecialRecord(boxText, boxNumber, depository, quantity, iter);
    }


    @Override
    public List<BearingRecord> selectInventoryByCutoffDate(Map<String, Object> params) {
        Integer size = 8, page = 1;
        if (params.containsKey("size")) {
            size = ObjectFormatUtil.toInteger(params.get("size"));
            params.put("size", size);
        }
        if (params.containsKey("page")) {
            page = ObjectFormatUtil.toInteger(params.get("page"));
            params.put("begin", (page - 1) * size);
        }
        return bearingRecordMapper.selectInventoryByCutoffDate(params);
    }
    @Override
    public List<Map<String, Object>> getEveryPairData(String startDate, String endDate, String depository) {
        return bearingRecordMapper.everyPair(startDate, endDate, depository);
    }
}