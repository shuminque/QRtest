package com.depository_manage.service.impl;

import com.depository_manage.entity.BearingInventory;
import com.depository_manage.entity.BearingRecord;
import com.depository_manage.entity.InventoryInfo;
import com.depository_manage.entity.ProductId;
import com.depository_manage.exception.OperationAlreadyDoneException;
import com.depository_manage.mapper.BearingInventoryMapper;
import com.depository_manage.service.BearingInventoryService;
import com.depository_manage.service.BearingRecordService;
import com.depository_manage.service.ProductIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
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
        System.out.println(inventory);
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
    public void tranIn(BearingInventory inventory) throws OperationAlreadyDoneException, IllegalStateException {
        String adjustedBoxText = adjustBoxText(inventory.getBoxText());
        // 检查是否已经入库
        boolean isStocked = productIdService.isProductStocked(
                inventory.getBoxText(), inventory.getBoxNumber(), inventory.getDepositoryId(), inventory.getIter()
        );
        if (isStocked) {
            throw new OperationAlreadyDoneException("产品已入库，不能再次入库");
        }
        // 检查是否存在对应的转出记录
        boolean hasTransferIn = bearingRecordService.hasTransferInRecord(
                inventory.getBoxText(),
                inventory.getBoxNumber(),
                inventory.getIter());
        if (hasTransferIn) {
            throw new IllegalStateException("已有对应的转入记录，操作无效");
        }
//        // 检查是否存在对应的转出记录
//        boolean hasTransferOut = bearingRecordService.checkForTransferOut(
//                adjustedBoxText,
//                inventory.getBoxNumber(),
//                inventory.getIter());
//        if (!hasTransferOut) {
//            throw new IllegalStateException("没有找到对应的转出记录，无法进行转入操作");
//        }
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
        System.out.println(inventory);
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
                adjustedDepositoryId,  0, inventory.getIter());
    }
    private int adjustDepositoryIdBasedOnTransferIn(BearingInventory inventory) {
        boolean hasTransferIn = bearingRecordService.hasTransferInRecord(
                inventory.getBoxText(), inventory.getBoxNumber(), inventory.getIter());
        if (hasTransferIn) {
            return inventory.getDepositoryId() == 1 ? 2 : 1;
        }
        return inventory.getDepositoryId();
    }
    private String adjustBoxText(String boxText) {
        if (boxText.startsWith("Z")) {
            // 如果boxText以Z开头，去掉Z
            return boxText.substring(1);
        } else {
            // 如果boxText不以Z开头，加上Z
            return "Z" + boxText;
        }
    }

    @Override
    public void stockOutForPC(BearingInventory inventory) {
        // 检查是否存在转入记录，根据转入逻辑调整仓库ID
        int adjustedDepositoryId = inventory.getDepositoryId();
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
                adjustedDepositoryId,  0, inventory.getIter()
        );
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
    @Override
    public void adjustInventoryBasedOnUpdate(BearingRecord originalRecord, BearingRecord updatedRecord) {
        Integer originalDepositoryId = getDepositoryIdFromString(originalRecord.getDepository());
        Integer updatedDepositoryId = getDepositoryIdFromString(updatedRecord.getDepository());

        if (!Objects.equals(originalDepositoryId, updatedDepositoryId)) {
            // 根据业务需求处理仓库ID不一致的情况
        }

        int quantityChange = updatedRecord.getQuantity() - originalRecord.getQuantity();
        BearingInventory inventory = bearingInventoryMapper.selectBearingInventoryByBoxTextAndDepositoryId(
                updatedRecord.getBoxText(), updatedDepositoryId);
        if (inventory != null) {
            // 根据交易类型和数量变化调整库存
            if (Arrays.asList("入库", "转入").contains(updatedRecord.getTransactionType())) {
                inventory.setQuantityInStock(inventory.getQuantityInStock() + quantityChange);
            } else if (Arrays.asList("出库", "转出", "返库").contains(updatedRecord.getTransactionType())) {
                inventory.setQuantityInStock(inventory.getQuantityInStock() - quantityChange);
            }
            // 更新库存记录
            bearingInventoryMapper.updateBearingInventory(inventory);
        } else {
            // 如果没有找到库存记录，可能需要插入新的库存记录或进行其他处理
        }

        // 根据业务逻辑进一步处理，例如更新 product_ids 表的状态
    }
    private int getDepositoryIdFromString(String depository) {
        switch (depository) {
            case "SAB":
                return 1;
            case "ZAB":
                return 2;
            default:
                return 0; // 或者抛出一个异常，如果没有找到匹配的仓库
        }
    }
}
