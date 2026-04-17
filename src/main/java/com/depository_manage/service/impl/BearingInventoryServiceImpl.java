package com.depository_manage.service.impl;

import com.depository_manage.entity.BearingInventory;
import com.depository_manage.entity.BearingRecord;
import com.depository_manage.entity.InventoryInfo;
import com.depository_manage.entity.ProductId;
import com.depository_manage.exception.InventoryOperationException;
import com.depository_manage.exception.OperationAlreadyDoneException;
import com.depository_manage.mapper.BearingInventoryMapper;
import com.depository_manage.service.BearingInventoryService;
import com.depository_manage.service.BearingRecordService;
import com.depository_manage.service.ProductIdService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Objects;

@Service
public class BearingInventoryServiceImpl implements BearingInventoryService {
    private static final Logger log = LoggerFactory.getLogger(BearingInventoryServiceImpl.class);

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
    @Transactional(rollbackFor = Exception.class)
    public void stockIn(BearingInventory inventory) {
        log.info("stockIn start, boxText={}, boxNumber={}, iter={}, depositoryId={}, operationType={}",
                inventory.getBoxText(), inventory.getBoxNumber(), inventory.getIter(),
                inventory.getDepositoryId(), inventory.getOperationType());
        try {
            boolean isStocked = productIdService.isProductStocked(
                    inventory.getBoxText(), inventory.getBoxNumber(), inventory.getDepositoryId(), inventory.getIter()
            );
            if (isStocked) {
                throw new OperationAlreadyDoneException("产品已入库，不能再次入库");
            }
            int targetDepositoryId = inventory.getDepositoryId() == 1 ? 2 : 1;
            if ("转入".equals(inventory.getOperationType())) {
                inventory.setDepositoryId(targetDepositoryId);
            }
            BearingInventory existingInventory = bearingInventoryMapper.selectBearingInventoryByBoxTextAndDepositoryId(
                    inventory.getBoxText(), inventory.getDepositoryId());
            if (existingInventory != null) {
                int newQuantity = existingInventory.getQuantityInStock() + inventory.getQuantityInStock();
                existingInventory.setQuantityInStock(newQuantity);
                existingInventory.setTotalBoxes(existingInventory.getTotalBoxes() + 1);
                bearingInventoryMapper.updateBearingInventory(existingInventory);
            } else {
                inventory.setTotalBoxes(1);
                bearingInventoryMapper.insertBearingInventory(inventory);
            }
            int stockedRows = productIdService.updateStockedStatus(
                    inventory.getBoxText(), inventory.getBoxNumber(),
                    inventory.getDepositoryId(), 1, inventory.getIter()
            );
            if (stockedRows <= 0) {
                throw new InventoryOperationException("更新入库状态失败，事务将回滚");
            }
            if ("转入".equals(inventory.getOperationType())) {
                ProductId newProductId = new ProductId();
                newProductId.setBoxText(inventory.getBoxText());
                newProductId.setBoxNumber(inventory.getBoxNumber());
                newProductId.setQuantity(inventory.getQuantityInStock());
                newProductId.setDepositoryId(targetDepositoryId);
                newProductId.setIsStocked(1);
                newProductId.setIter(inventory.getIter());
                ProductId savedProductId = productIdService.saveOrUpdateBoxNumber(newProductId);
                if (savedProductId == null) {
                    throw new InventoryOperationException("保存转入箱号失败，事务将回滚");
                }
            }
        } catch (OperationAlreadyDoneException e) {
            throw e;
        } catch (Exception e) {
            log.error("stockIn failed, boxText={}, boxNumber={}, iter={}, depositoryId={}, operationType={}",
                    inventory.getBoxText(), inventory.getBoxNumber(), inventory.getIter(),
                    inventory.getDepositoryId(), inventory.getOperationType(), e);
            throw new InventoryOperationException("入库失败，事务已回滚", e);
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void tranIn(BearingInventory inventory) throws OperationAlreadyDoneException, IllegalStateException {
        log.info("tranIn start, boxText={}, boxNumber={}, iter={}, depositoryId={}, operationType={}",
                inventory.getBoxText(), inventory.getBoxNumber(), inventory.getIter(),
                inventory.getDepositoryId(), inventory.getOperationType());
        try {
            String adjustedBoxText = adjustBoxText(inventory.getBoxText());
            int targetDepositoryId = inventory.getDepositoryId() == 1 ? 2 : 1;
            boolean isStocked = productIdService.isProductStocked(
                    inventory.getBoxText(), inventory.getBoxNumber(), targetDepositoryId, inventory.getIter()
            );
            if (isStocked) {
                throw new OperationAlreadyDoneException("产品已入库，不能再次入库");
            }
            boolean hasTransferOut = bearingRecordService.checkForTransferOut(
                    adjustedBoxText,
                    inventory.getBoxNumber(),
                    inventory.getIter());
            if (!hasTransferOut) {
                throw new IllegalStateException("没有找到对应的转出记录，无法进行转入操作");
            }
            if ("转入".equals(inventory.getOperationType())) {
                inventory.setDepositoryId(targetDepositoryId);
            }
            BearingInventory existingInventory = bearingInventoryMapper.selectBearingInventoryByBoxTextAndDepositoryId(
                    inventory.getBoxText(), inventory.getDepositoryId());
            if (existingInventory != null) {
                int newQuantity = existingInventory.getQuantityInStock() + inventory.getQuantityInStock();
                existingInventory.setQuantityInStock(newQuantity);
                existingInventory.setTotalBoxes(existingInventory.getTotalBoxes() + 1);
                bearingInventoryMapper.updateBearingInventory(existingInventory);
            } else {
                inventory.setTotalBoxes(1);
                bearingInventoryMapper.insertBearingInventory(inventory);
            }
            int stockedRows = productIdService.updateStockedStatus(
                    inventory.getBoxText(), inventory.getBoxNumber(),
                    inventory.getDepositoryId(), 1, inventory.getIter()
            );
            if (stockedRows <= 0) {
                throw new InventoryOperationException("更新转入状态失败，事务将回滚");
            }
            if ("转入".equals(inventory.getOperationType())) {
                ProductId existingProductId = productIdService.findProductId(
                        inventory.getBoxText(),
                        inventory.getBoxNumber(),
                        targetDepositoryId,
                        inventory.getIter()
                );
                ProductId savedProductId;
                if (existingProductId == null) {
                    ProductId newProductId = new ProductId();
                    newProductId.setBoxText(inventory.getBoxText());
                    newProductId.setBoxNumber(inventory.getBoxNumber());
                    newProductId.setQuantity(inventory.getQuantityInStock());
                    newProductId.setDepositoryId(targetDepositoryId);
                    newProductId.setIsStocked(1);
                    newProductId.setIter(inventory.getIter());
                    savedProductId = productIdService.saveOrUpdateBoxNumber(newProductId);
                } else {
                    existingProductId.setIsStocked(1);
                    existingProductId.setQuantity(inventory.getQuantityInStock());
                    savedProductId = productIdService.saveOrUpdateBoxNumber(existingProductId);
                }
                if (savedProductId == null) {
                    throw new InventoryOperationException("保存转入箱号失败，事务将回滚");
                }
            }
        } catch (OperationAlreadyDoneException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("tranIn failed, boxText={}, boxNumber={}, iter={}, depositoryId={}, operationType={}",
                    inventory.getBoxText(), inventory.getBoxNumber(), inventory.getIter(),
                    inventory.getDepositoryId(), inventory.getOperationType(), e);
            throw new InventoryOperationException("转入失败，事务已回滚", e);
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void stockOut(BearingInventory inventory) {
        log.info("stockOut start, boxText={}, boxNumber={}, iter={}, depositoryId={}, operationType={}",
                inventory.getBoxText(), inventory.getBoxNumber(), inventory.getIter(),
                inventory.getDepositoryId(), inventory.getOperationType());
        try {
            int adjustedDepositoryId = adjustDepositoryIdBasedOnTransferIn(inventory);
            boolean isStocked = productIdService.isProductStocked(
                    inventory.getBoxText(), inventory.getBoxNumber(), adjustedDepositoryId, inventory.getIter());
            if (!isStocked) {
                throw new OperationAlreadyDoneException("产品未入库，不能出库");
            }
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
            int stockedRows = productIdService.updateStockedStatus(
                    inventory.getBoxText(), inventory.getBoxNumber(),
                    adjustedDepositoryId, 0, inventory.getIter());
            if (stockedRows <= 0) {
                throw new InventoryOperationException("更新出库状态失败，事务将回滚");
            }
        } catch (OperationAlreadyDoneException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("stockOut failed, boxText={}, boxNumber={}, iter={}, depositoryId={}, operationType={}",
                    inventory.getBoxText(), inventory.getBoxNumber(), inventory.getIter(),
                    inventory.getDepositoryId(), inventory.getOperationType(), e);
            throw new InventoryOperationException("出库失败，事务已回滚", e);
        }
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
    @Transactional(rollbackFor = Exception.class)
    public void stockOutForPC(BearingInventory inventory) {
        log.info("stockOutForPC start, boxText={}, boxNumber={}, iter={}, depositoryId={}, operationType={}",
                inventory.getBoxText(), inventory.getBoxNumber(), inventory.getIter(),
                inventory.getDepositoryId(), inventory.getOperationType());
        try {
            int adjustedDepositoryId = inventory.getDepositoryId();
            boolean isStocked = productIdService.isProductStocked(
                    inventory.getBoxText(), inventory.getBoxNumber(), adjustedDepositoryId, inventory.getIter());
            if (!isStocked) {
                throw new OperationAlreadyDoneException("产品未入库，不能出库");
            }
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
            int stockedRows = productIdService.updateStockedStatus(
                    inventory.getBoxText(), inventory.getBoxNumber(),
                    adjustedDepositoryId, 0, inventory.getIter()
            );
            if (stockedRows <= 0) {
                throw new InventoryOperationException("更新出库状态失败，事务将回滚");
            }
        } catch (OperationAlreadyDoneException | IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            log.error("stockOutForPC failed, boxText={}, boxNumber={}, iter={}, depositoryId={}, operationType={}",
                    inventory.getBoxText(), inventory.getBoxNumber(), inventory.getIter(),
                    inventory.getDepositoryId(), inventory.getOperationType(), e);
            throw new InventoryOperationException("PC出库失败，事务已回滚", e);
        }
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
        Integer originalDepositoryId = getDepositoryIdFromString(originalRecord.getCurrentDepository());
        Integer updatedDepositoryId = getDepositoryIdFromString(updatedRecord.getCurrentDepository());

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
