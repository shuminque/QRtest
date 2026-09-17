package com.depository_manage.service;

import com.depository_manage.entity.Bearing;
import com.depository_manage.entity.BearingRecord;
import com.depository_manage.exception.InventoryOperationException;
import com.depository_manage.mapper.BearingRecordMapper;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Builds a complete bearing_records row from an operation request and the
 * matching bearing master data.
 */
@Service
public class BearingRecordWriterService {

    private final BearingRecordMapper bearingRecordMapper;
    private final BearingService bearingService;

    public BearingRecordWriterService(BearingRecordMapper bearingRecordMapper,
                                      BearingService bearingService) {
        this.bearingRecordMapper = bearingRecordMapper;
        this.bearingService = bearingService;
    }

    /**
     * Enriches and inserts one operation record.  Throwing here is intentional:
     * callers in a transaction must not commit an inventory change without this
     * audit row.
     */
    public void createAndInsert(BearingRecord record) {
        String adjustedBoxText = record.getBoxText();
        String currentDepository = record.getDepository();
        if ("转入".equals(record.getTransactionType())) {
            boolean isFromZabToSab = "SAB".equals(currentDepository) && adjustedBoxText.startsWith("Z");
            boolean isFromSabToZab = "ZAB".equals(currentDepository) && !adjustedBoxText.startsWith("Z");
            if (isFromZabToSab) {
                adjustedBoxText = adjustedBoxText.substring(1);
                currentDepository = "ZAB";
            } else if (isFromSabToZab) {
                adjustedBoxText = "Z" + adjustedBoxText;
                currentDepository = "SAB";
            }
        }

        String currentState = bearingRecordMapper.getCurrentState(record.getBoxText(), record.getBoxNumber());
        if (currentState == null) {
            currentState = "正常";
        }

        Bearing bearing = bearingService.getBearingByBoxTextAndDepository(
                adjustedBoxText, record.getDepository());
        if (bearing == null) {
            throw new InventoryOperationException("未找到对应轴承基础资料，入库事务已回滚");
        }

        record.setCustomer(bearing.getCustomer());
        record.setModel(bearing.getModel());
        record.setProductCategory(bearing.getProductCategory());
        record.setSteelType(bearing.getSteelType());
        record.setSteelGrade(bearing.getSteelGrade());
        record.setDepository(bearing.getDepository());
        record.setStorageLocation(bearing.getStorageLocation());
        record.setOuterInnerRing(bearing.getOuterInnerRing());
        record.setSize(bearing.getSize());
        // bearing_records.pair is a primitive int while the master-data field
        // is nullable.  A missing pair must not abort a completed stock-in.
        record.setPair(bearing.getPair() == null ? 0 : bearing.getPair());
        record.setState(currentState);
        record.setCurrentDepository(currentDepository);
        record.setSingleEight(bearing.getSingleEight());
        record.setMode(bearing.getMode());
        record.setTime(new Date());

        bearingRecordMapper.insertBearingRecord(record);
    }
}
