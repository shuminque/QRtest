package com.depository_manage.service;

import com.depository_manage.entity.Bearing;
import com.depository_manage.entity.BearingRecord;
import com.depository_manage.exception.InventoryOperationException;
import com.depository_manage.mapper.BearingRecordMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BearingRecordWriterServiceTest {

    @Test
    public void createsCompleteRecordWithDefaultState() {
        BearingRecordMapper mapper = mock(BearingRecordMapper.class);
        BearingService bearingService = mock(BearingService.class);
        BearingRecordWriterService writer = new BearingRecordWriterService(mapper, bearingService);

        Bearing bearing = new Bearing();
        bearing.setDepository("ZAB");
        bearing.setCustomer("客户A");
        bearing.setModel("型号A");
        bearing.setPair(null);
        when(bearingService.getBearingByBoxTextAndDepository("ZCOA*XC", "ZAB")).thenReturn(bearing);

        BearingRecord record = new BearingRecord();
        record.setBoxText("ZCOA*XC");
        record.setBoxNumber("592");
        record.setDepository("ZAB");

        writer.createAndInsert(record);

        assertEquals("正常", record.getState());
        assertEquals("ZAB", record.getCurrentDepository());
        assertEquals("客户A", record.getCustomer());
        assertEquals("型号A", record.getModel());
        assertEquals(0, record.getPair());
        verify(mapper).insertBearingRecord(record);
    }

    @Test
    public void refusesToWriteAnIncompleteAuditRecordWhenMasterDataIsMissing() {
        BearingRecordMapper mapper = mock(BearingRecordMapper.class);
        BearingService bearingService = mock(BearingService.class);
        BearingRecordWriterService writer = new BearingRecordWriterService(mapper, bearingService);

        BearingRecord record = new BearingRecord();
        record.setBoxText("ZCOA*XC");
        record.setBoxNumber("592");
        record.setDepository("ZAB");
        when(bearingService.getBearingByBoxTextAndDepository(eq("ZCOA*XC"), eq("ZAB"))).thenReturn(null);

        assertThrows(InventoryOperationException.class, () -> writer.createAndInsert(record));
        verify(mapper, never()).insertBearingRecord(any(BearingRecord.class));
    }
}
