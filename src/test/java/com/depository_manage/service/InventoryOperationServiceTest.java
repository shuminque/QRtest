package com.depository_manage.service;

import com.depository_manage.entity.BearingInventory;
import com.depository_manage.exception.InventoryOperationException;
import org.junit.Test;

import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class InventoryOperationServiceTest {

    @Test
    public void propagatesRecordWriteFailureSoTheTransactionCanRollBack() {
        BearingInventoryService inventoryService = mock(BearingInventoryService.class);
        BearingRecordWriterService recordWriter = mock(BearingRecordWriterService.class);
        InventoryOperationService service = new InventoryOperationService(inventoryService, recordWriter);
        BearingInventory inventory = stockInInventory();
        doThrow(new InventoryOperationException("记录失败")).when(recordWriter).createAndInsert(org.mockito.ArgumentMatchers.any());

        assertThrows(InventoryOperationException.class, () -> service.stockInAndCreateRecord(inventory));
        verify(inventoryService).stockIn(inventory);
        verify(recordWriter).createAndInsert(org.mockito.ArgumentMatchers.any());
    }

    private BearingInventory stockInInventory() {
        BearingInventory inventory = new BearingInventory();
        inventory.setBoxText("ZCOA*XC");
        inventory.setBoxNumber("592");
        inventory.setQuantityInStock(15000);
        inventory.setDepositoryId(2);
        inventory.setIter(1);
        inventory.setOperationType("入库");
        return inventory;
    }
}
