package com.depository_manage.service;

import com.depository_manage.entity.BearingInventory;
import com.depository_manage.entity.BearingRecord;
import com.depository_manage.entity.InventoryInfo;

public interface BearingInventoryService {
    void addBearingInventory(BearingInventory bearingInventory);
    BearingInventory getBearingInventory(String boxText);
    void stockIn(BearingInventory bearingInventory);
    void stockOut(BearingInventory bearingInventory);
    void stockOutForPC(BearingInventory bearingInventory);

    //    boolean isOperationAlreadyDone(String boxText, String boxNumber,String operationType);
    InventoryInfo getInventoryInfo(String boxText, String boxNumber, int depositoryId);

    void adjustStockForDeletion(BearingInventory inventory, boolean increaseStock);
    void adjustInventoryBasedOnUpdate(BearingRecord originalRecord, BearingRecord updatedRecord);

}
