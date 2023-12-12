package com.depository_manage.service.impl;

import com.depository_manage.entity.BearingInventory;
import com.depository_manage.exception.OperationAlreadyDoneException;
import com.depository_manage.mapper.BearingInventoryMapper;
import com.depository_manage.service.BearingInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BearingInventoryServiceImpl implements BearingInventoryService {

    @Autowired
    private BearingInventoryMapper bearingInventoryMapper;

    @Override
    public void addBearingInventory(BearingInventory bearingInventory) {
        bearingInventoryMapper.insertBearingInventory(bearingInventory);
    }

    @Override
    public BearingInventory getBearingInventory(String boxText) {
        return bearingInventoryMapper.selectBearingInventory(boxText);
    }
    @Override
    public void stockIn(BearingInventory inventory) {
        if (isOperationAlreadyDone(inventory.getBoxText(), inventory.getBoxNumber(),"入库")) {
            throw new OperationAlreadyDoneException("重复的操作");
        }
        System.out.println(inventory);
        BearingInventory existingInventory = bearingInventoryMapper.selectBearingInventory(inventory.getBoxText());
        if (existingInventory != null) {
            // 更新库存数量
            int newQuantity = existingInventory.getQuantityInStock() + inventory.getQuantityInStock();
            existingInventory.setQuantityInStock(newQuantity);
            bearingInventoryMapper.updateBearingInventory(existingInventory);
        } else {
            // 插入新的库存记录
            bearingInventoryMapper.insertBearingInventory(inventory);
        }
    }

    @Override
    public void stockOut(BearingInventory inventory) {
        if (isOperationAlreadyDone(inventory.getBoxText(), inventory.getBoxNumber(),"出库")) {
            throw new OperationAlreadyDoneException("重复的操作");
        }
        BearingInventory existingInventory = bearingInventoryMapper.selectBearingInventory(inventory.getBoxText());
        if (existingInventory != null && existingInventory.getQuantityInStock() >= inventory.getQuantityInStock()) {
            // 更新库存数量
            int newQuantity = existingInventory.getQuantityInStock() - inventory.getQuantityInStock();
            existingInventory.setQuantityInStock(newQuantity);
            bearingInventoryMapper.updateBearingInventory(existingInventory);
        } else {
            // 库存不足或记录不存在的处理逻辑
            throw new IllegalStateException("库存不足或记录不存在，无法执行出库。");
        }
    }
    @Override
    public boolean isOperationAlreadyDone(String boxText, String boxNumber,String operationType) {
        int count = bearingInventoryMapper.countOperationRecords(boxText, boxNumber, operationType);
        return count > 0;
    }

}
