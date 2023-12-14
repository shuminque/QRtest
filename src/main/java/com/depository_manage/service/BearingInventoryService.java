package com.depository_manage.service;

import com.depository_manage.entity.BearingInventory;

public interface BearingInventoryService {
    void addBearingInventory(BearingInventory bearingInventory);
    BearingInventory getBearingInventory(String boxText);
    void stockIn(BearingInventory bearingInventory);
    void stockOut(BearingInventory bearingInventory);
//    boolean isOperationAlreadyDone(String boxText, String boxNumber,String operationType);

}
