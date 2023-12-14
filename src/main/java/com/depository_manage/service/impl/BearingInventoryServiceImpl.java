package com.depository_manage.service.impl;

import com.depository_manage.entity.BearingInventory;
import com.depository_manage.exception.OperationAlreadyDoneException;
import com.depository_manage.mapper.BearingInventoryMapper;
import com.depository_manage.service.BearingInventoryService;
import com.depository_manage.service.ProductIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BearingInventoryServiceImpl implements BearingInventoryService {

    @Autowired
    private BearingInventoryMapper bearingInventoryMapper;

    @Autowired
    private ProductIdService productIdService; // 注入ProductIdService

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
        System.out.println(inventory);
        // 检查是否已经入库
        boolean isStocked = productIdService.isProductStocked(
                inventory.getBoxText(), inventory.getBoxNumber(), inventory.getDepositoryId()
        );
        if (isStocked) {
            throw new OperationAlreadyDoneException("产品已入库，不能再次入库");
        }
        // 执行入库操作...
        BearingInventory existingInventory = bearingInventoryMapper.selectBearingInventory(inventory.getBoxText());
        if (existingInventory != null) {
            int newQuantity = existingInventory.getQuantityInStock() + inventory.getQuantityInStock();
            existingInventory.setQuantityInStock(newQuantity);
            bearingInventoryMapper.updateBearingInventory(existingInventory);
        } else {
            bearingInventoryMapper.insertBearingInventory(inventory);
        }

        // 更新状态为已入库
        productIdService.updateStockedStatus(
                inventory.getBoxText(), inventory.getBoxNumber(), inventory.getDepositoryId(), 1);
    }

    @Override
    public void stockOut(BearingInventory inventory) {
        System.out.println(inventory);
        // 检查是否可以出库
        boolean isStocked = productIdService.isProductStocked(
                inventory.getBoxText(), inventory.getBoxNumber(), inventory.getDepositoryId());

        if (!isStocked) {
            throw new OperationAlreadyDoneException("产品未入库，不能出库");
        }

        // 执行出库操作...
        BearingInventory existingInventory = bearingInventoryMapper.selectBearingInventory(inventory.getBoxText());
        if (existingInventory != null && existingInventory.getQuantityInStock() >= inventory.getQuantityInStock()) {
            int newQuantity = existingInventory.getQuantityInStock() - inventory.getQuantityInStock();
            existingInventory.setQuantityInStock(newQuantity);
            bearingInventoryMapper.updateBearingInventory(existingInventory);
        } else {
            throw new IllegalStateException("库存不足或记录不存在，无法执行出库。");
        }

        // 更新状态为未入库
        productIdService.updateStockedStatus(
                inventory.getBoxText(), inventory.getBoxNumber(), inventory.getDepositoryId(), 0);
    }
}
