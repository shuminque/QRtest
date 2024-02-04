package com.depository_manage.service;

import com.depository_manage.entity.BearingRecord;

import java.util.List;
import java.util.Map;

public interface BearingRecordService {
    void addBearingRecord(BearingRecord record);
    void updateBearingRecord(BearingRecord record);
    void deleteBearingRecordById(int id);
    BearingRecord getBearingRecordById(int id);
    public List<BearingRecord> filterBearingRecords(Map<String, Object> params);
    public boolean hasSpecialRecord(String boxText, String boxNumber, String depository, int quantity, int iter);

    List<BearingRecord> selectInventoryByCutoffDate(Map<String, Object> params);
    List<Map<String, Object>> getEveryPairData(String startDate, String endDate, String depository);

}
