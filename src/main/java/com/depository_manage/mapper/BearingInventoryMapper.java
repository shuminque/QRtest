package com.depository_manage.mapper;

import com.depository_manage.entity.BearingInventory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BearingInventoryMapper {
    void insertBearingInventory(BearingInventory bearingInventory);

    BearingInventory selectBearingInventory(String boxText);

    // 新添加的更新库存方法
    void updateBearingInventory(BearingInventory bearingInventory);

    // 如果需要，添加删除库存方法
    void deleteBearingInventory(String boxText);
}
