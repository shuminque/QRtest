package com.depository_manage.service;

import com.depository_manage.entity.BearingRecord;
import java.util.List;

public interface BearingRecordService {
    void addBearingRecord(BearingRecord record);
    void updateBearingRecord(BearingRecord record);
    void deleteBearingRecordById(int id);
    BearingRecord getBearingRecordById(int id);
    List<BearingRecord> getAllBearingRecords();
}
