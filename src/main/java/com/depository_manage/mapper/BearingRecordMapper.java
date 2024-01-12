package com.depository_manage.mapper;

import com.depository_manage.entity.BearingRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Mapper
public interface BearingRecordMapper {
    // 插入新的记录
    void insertBearingRecord(BearingRecord record);

    // 更新记录
    void updateBearingRecord(BearingRecord record);

    // 根据 ID 删除记录
    void deleteBearingRecordById(int id);

    // 根据 ID 查询记录
    BearingRecord selectBearingRecordById(int id);

    // 获取所有记录
    List<BearingRecord> selectAllBearingRecords(Map<String,Object> params);

    boolean hasSpecialRecord(@Param("boxText") String boxText,
                             @Param("boxNumber") String boxNumber,
                             @Param("depository") String depository,
                             @Param("quantity") int quantity);
    List<BearingRecord> selectInventoryByCutoffDate(Map<String, Object> params);

}
