package com.depository_manage.service.impl;

import com.depository_manage.entity.BearingInventory;
import com.depository_manage.entity.InventoryInfo;
import com.depository_manage.entity.ProductId;
import com.depository_manage.exception.OperationAlreadyDoneException;
import com.depository_manage.mapper.BearingInventoryMapper;
import com.depository_manage.mapper.BearingRecordMapper;
import com.depository_manage.service.BearingInventoryService;
import com.depository_manage.service.BearingRecordService;
import com.depository_manage.service.ProductIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class BearingInventoryServiceImpl implements BearingInventoryService {

    @Autowired
    private BearingInventoryMapper bearingInventoryMapper;

    @Autowired
    private ProductIdService productIdService; // 注入ProductIdService
    @Autowired
    private BearingRecordService bearingRecordService; // 注入ProductIdService
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
        // 检查是否已经入库
        boolean isStocked = productIdService.isProductStocked(
                inventory.getBoxText(), inventory.getBoxNumber(), inventory.getDepositoryId(), inventory.getIter()
        );
        if (isStocked) {
            throw new OperationAlreadyDoneException("产品已入库，不能再次入库");
        }
        // 确定目标仓库ID
        int targetDepositoryId = inventory.getDepositoryId() == 1 ? 2 : 1;
        // 如果是转入操作，更新仓库ID
        if ("转入".equals(inventory.getOperationType())) {
            inventory.setDepositoryId(targetDepositoryId);
        }
        // 执行入库操作
        BearingInventory existingInventory = bearingInventoryMapper.selectBearingInventoryByBoxTextAndDepositoryId(
                inventory.getBoxText(), inventory.getDepositoryId());
        // 更新库存信息
        if (existingInventory != null) {
            int newQuantity = existingInventory.getQuantityInStock() + inventory.getQuantityInStock();
            existingInventory.setQuantityInStock(newQuantity);
            // 假设总箱数字段叫做 totalBoxes
            existingInventory.setTotalBoxes(existingInventory.getTotalBoxes() + 1); // 增加总箱数
            bearingInventoryMapper.updateBearingInventory(existingInventory);
        } else {
            // 第一次入库时，总箱数设置为 1
            inventory.setTotalBoxes(1);
            bearingInventoryMapper.insertBearingInventory(inventory);
        }
        // 更新状态为已入库
        productIdService.updateStockedStatus(
                inventory.getBoxText(), inventory.getBoxNumber(),
                inventory.getDepositoryId(),  1, inventory.getIter()
        );
        // 如果是转入操作，还需要更新或新增 product_ids 表中的记录以反映转入状态
        if ("转入".equals(inventory.getOperationType())) {
            ProductId newProductId = new ProductId();
            newProductId.setBoxText(inventory.getBoxText());
            newProductId.setBoxNumber(inventory.getBoxNumber());
            newProductId.setQuantity(inventory.getQuantityInStock()); // 注意这里使用的是入库数量
            newProductId.setDepositoryId(targetDepositoryId);
            newProductId.setIsStocked(1); // 标记为已入库
            newProductId.setIter(inventory.getIter());
            productIdService.saveOrUpdateBoxNumber(newProductId);
        }
    }

    @Override
    public void stockOut(BearingInventory inventory) {
        // 检查是否存在转入记录，根据转入逻辑调整仓库ID
        int adjustedDepositoryId = adjustDepositoryIdBasedOnTransferIn(inventory);
        // 检查是否可以出库
        boolean isStocked = productIdService.isProductStocked(
                inventory.getBoxText(), inventory.getBoxNumber(), adjustedDepositoryId, inventory.getIter());
        if (!isStocked) {
            throw new OperationAlreadyDoneException("产品未入库，不能出库");
        }
        // 执行出库操作
        BearingInventory existingInventory = bearingInventoryMapper.selectBearingInventoryByBoxTextAndDepositoryId(
                inventory.getBoxText(), adjustedDepositoryId);
        if (existingInventory != null && existingInventory.getQuantityInStock() >= inventory.getQuantityInStock()) {
            int newQuantity = existingInventory.getQuantityInStock() - inventory.getQuantityInStock();
            existingInventory.setQuantityInStock(newQuantity);
            existingInventory.setTotalBoxes(existingInventory.getTotalBoxes() - 1);
            bearingInventoryMapper.updateBearingInventory(existingInventory);
        } else {
            throw new IllegalStateException("库存不足或记录不存在，无法执行出库。");
        }
        // 更新状态为已入库
        productIdService.updateStockedStatus(
                inventory.getBoxText(), inventory.getBoxNumber(),
                inventory.getDepositoryId(),  0, inventory.getIter());
    }
    private int adjustDepositoryIdBasedOnTransferIn(BearingInventory inventory) {
        boolean hasTransferIn = bearingRecordService.hasTransferInRecord(
                inventory.getBoxText(), inventory.getBoxNumber(), inventory.getIter());
        if (hasTransferIn) {
            return inventory.getDepositoryId() == 1 ? 2 : 1;
        }
        return inventory.getDepositoryId();
    }
    @Override
    public InventoryInfo getInventoryInfo(String boxText, String boxNumber, int depositoryId) {
        return bearingInventoryMapper.selectInventoryInfo(boxText, boxNumber, depositoryId);
    }
    @Override
    public void adjustStockForDeletion(BearingInventory inventory, boolean increaseStock) {
        BearingInventory existingInventory = bearingInventoryMapper.selectBearingInventoryByBoxTextAndDepositoryId(
                inventory.getBoxText(), inventory.getDepositoryId());

        if (existingInventory != null) {
            int newQuantity = existingInventory.getQuantityInStock() + (increaseStock ? inventory.getQuantityInStock() : -inventory.getQuantityInStock());
            existingInventory.setQuantityInStock(newQuantity);

            // 根据增减库存调整总箱数
            int adjustment = increaseStock ? 1 : -1;
            existingInventory.setTotalBoxes(Math.max(0, existingInventory.getTotalBoxes() + adjustment));

            bearingInventoryMapper.updateBearingInventory(existingInventory);
        }
    }

}
