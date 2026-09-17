package com.depository_manage.service;

import com.depository_manage.entity.BearingInventory;
import com.depository_manage.entity.BearingRecord;
import com.depository_manage.exception.InventoryOperationException;
import com.depository_manage.exception.OperationAlreadyDoneException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Coordinates a physical inventory change with its immutable operation record.
 *
 * <p>The two writes must be committed together.  Calling the legacy inventory
 * endpoint and the record endpoint separately can otherwise leave a product
 * marked as stocked without an audit record.</p>
 */
@Service
public class InventoryOperationService {

    private static final Logger log = LoggerFactory.getLogger(InventoryOperationService.class);

    private final BearingInventoryService bearingInventoryService;
    private final BearingRecordWriterService bearingRecordWriterService;

    public InventoryOperationService(BearingInventoryService bearingInventoryService,
                                     BearingRecordWriterService bearingRecordWriterService) {
        this.bearingInventoryService = bearingInventoryService;
        this.bearingRecordWriterService = bearingRecordWriterService;
    }

    /**
     * Performs a normal stock-in and creates its audit record in one database
     * transaction.  Any record creation failure rolls back the inventory and
     * product_ids changes made by {@link BearingInventoryService#stockIn}.
     */
    @Transactional(rollbackFor = Exception.class)
    public void stockInAndCreateRecord(BearingInventory inventory) {
        log.info("stockInAndCreateRecord start, boxText={}, boxNumber={}, iter={}, depositoryId={}",
                inventory.getBoxText(), inventory.getBoxNumber(), inventory.getIter(), inventory.getDepositoryId());
        try {
            bearingInventoryService.stockIn(inventory);

            BearingRecord record = new BearingRecord();
            record.setTransactionType(inventory.getOperationType());
            record.setBoxText(inventory.getBoxText());
            record.setBoxNumber(inventory.getBoxNumber());
            record.setQuantity(inventory.getQuantityInStock());
            record.setDepository(toDepositoryName(inventory.getDepositoryId()));
            record.setIter(inventory.getIter());

            bearingRecordWriterService.createAndInsert(record);

            log.info("stockInAndCreateRecord complete, boxText={}, boxNumber={}, iter={}, depositoryId={}",
                    inventory.getBoxText(), inventory.getBoxNumber(), inventory.getIter(), inventory.getDepositoryId());
        } catch (OperationAlreadyDoneException | InventoryOperationException e) {
            throw e;
        } catch (Exception e) {
            log.error("stockInAndCreateRecord failed, boxText={}, boxNumber={}, iter={}, depositoryId={}",
                    inventory.getBoxText(), inventory.getBoxNumber(), inventory.getIter(), inventory.getDepositoryId(), e);
            throw new InventoryOperationException("入库及记录写入失败，事务已回滚", e);
        }
    }

    private String toDepositoryName(int depositoryId) {
        switch (depositoryId) {
            case 1:
                return "SAB";
            case 2:
                return "ZAB";
            default:
                throw new InventoryOperationException("当前账号未绑定可操作仓库");
        }
    }
}
