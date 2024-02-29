package com.depository_manage.service.impl;

import com.depository_manage.entity.BearingInventory;
import com.depository_manage.entity.BearingRecord;
import com.depository_manage.mapper.BearingRecordMapper;
import com.depository_manage.service.BearingInventoryService;
import com.depository_manage.service.BearingRecordService;
import com.depository_manage.service.ProductIdService;
import com.depository_manage.utils.ObjectFormatUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class BearingRecordServiceImpl implements BearingRecordService {

    @Autowired
    private BearingRecordMapper bearingRecordMapper;
    @Autowired
    private BearingInventoryService bearingInventoryService;
    @Autowired
    private ProductIdService productIdService;
    @Override
    public void addBearingRecord(BearingRecord record) {
        bearingRecordMapper.insertBearingRecord(record);
    }

    @Override
    public void updateBearingRecord(BearingRecord record) {
        bearingRecordMapper.updateBearingRecord(record);
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
    @Transactional
    public void deleteBearingRecordById(int id) {
        // 首先根据ID获取记录详情
        BearingRecord record = bearingRecordMapper.selectBearingRecordById(id);

        if (record != null) {
            int depositoryId = getDepositoryIdFromString(record.getDepository()); // 转换仓库字符为ID
            // 准备库存调整对象
            BearingInventory inventoryAdjustment = new BearingInventory();
            inventoryAdjustment.setBoxText(record.getBoxText());
            inventoryAdjustment.setDepositoryId(depositoryId);
            inventoryAdjustment.setQuantityInStock(record.getQuantity());

            // 确定是否增加库存
            boolean increaseStock = false; // 默认为减少库存

            // 如果是出库、返库或转出操作，删除后需要增加库存
            if ("出库".equals(record.getTransactionType()) || "返库".equals(record.getTransactionType()) || "转出".equals(record.getTransactionType())) {
                increaseStock = true;
            }
            // 使用专门的方法调整库存
            bearingInventoryService.adjustStockForDeletion(inventoryAdjustment, increaseStock);

            // 更新product_ids表的状态，这里的逻辑需要根据您的业务规则调整
            productIdService.updateStockedStatus(record.getBoxText(), record.getBoxNumber(), depositoryId, increaseStock ? 1 : 0, record.getIter());
            // 在更新库存和product_ids状态之后，删除该记录
            bearingRecordMapper.deleteBearingRecordById(id);
        } else {
            throw new EntityNotFoundException("The record with ID " + id + " does not exist.");
        }
    }


    @Override
    public BearingRecord getBearingRecordById(int id) {
        return bearingRecordMapper.selectBearingRecordById(id);
    }
    @Override
    public int countBearingRecords(Map<String, Object> params) {
        return bearingRecordMapper.countBearingRecords(params);
    }
    @Override
    public List<BearingRecord> filterBearingRecords(Map<String, Object> params) {
        Integer size = 8, page = 1;
        if (params.containsKey("size")) {
            size = ObjectFormatUtil.toInteger(params.get("size"));
            params.put("size", size);
        }
        if (params.containsKey("page")) {
            page = ObjectFormatUtil.toInteger(params.get("page"));
            params.put("begin", (page - 1) * size);
        }
        return bearingRecordMapper.selectAllBearingRecords(params);
    }

    @Override
    public boolean hasSpecialRecord(String boxText, String boxNumber, String depository, int quantity, int iter) {
        return bearingRecordMapper.hasSpecialRecord(boxText, boxNumber, depository, quantity, iter);
    }


    @Override
    public List<BearingRecord> selectInventoryByCutoffDate(Map<String, Object> params) {
        Integer size = 8, page = 1;
        if (params.containsKey("size")) {
            size = ObjectFormatUtil.toInteger(params.get("size"));
            params.put("size", size);
        }
        if (params.containsKey("page")) {
            page = ObjectFormatUtil.toInteger(params.get("page"));
            params.put("begin", (page - 1) * size);
        }
        return bearingRecordMapper.selectInventoryByCutoffDate(params);
    }
    @Override
    public List<Map<String, Object>> getEveryPairData(String depository, String startDate, String endDate, String state) {
        return bearingRecordMapper.everyPair(depository, startDate, endDate, state);
    }
    @Override
    public List<Map<String, Object>> getInventoryStatus(Date cutoffDate, String depository, String state) {
        return bearingRecordMapper.selectInventoryStatus(cutoffDate, depository, state);
    }
}