package com.depository_manage.controller;

import com.depository_manage.entity.Bearing;
import com.depository_manage.entity.BearingRecord;
import com.depository_manage.service.BearingRecordService;
import com.depository_manage.service.BearingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/bearingRecords")
public class BearingRecordController {

    @Autowired
    private BearingRecordService bearingRecordService;
    @Autowired
    private BearingService bearingService;
    @PostMapping
    public ResponseEntity<?> addBearingRecord(@RequestBody BearingRecord record) {
        String adjustedBoxText = record.getBoxText(); // 初始化调整后的箱号
        String currentDepository = record.getDepository(); // 使用记录中的原始仓库信息
        // 检查是否存在转入记录
        boolean hasTransferIn = bearingRecordService.hasTransferInRecord(record.getBoxText(), record.getBoxNumber(), record.getIter());
        if ("转入".equals(record.getTransactionType()) || ("出库".equals(record.getTransactionType()) && hasTransferIn)) {
            boolean isFromZABToSAB = "SAB".equals(currentDepository) && adjustedBoxText.startsWith("Z");
            boolean isFromSABToZAB = "ZAB".equals(currentDepository) && !adjustedBoxText.startsWith("Z");
            if (isFromZABToSAB) {
                adjustedBoxText = adjustedBoxText.substring(1);
                currentDepository = "ZAB";
            } else if (isFromSABToZAB) {
                adjustedBoxText = "Z" + adjustedBoxText;
                currentDepository = "SAB";
            }
        }
        System.out.println(record);
        Bearing bearing = bearingService.getBearingByBoxTextAndDepository(adjustedBoxText, record.getDepository());
        if (bearing != null) {
            // 使用Bearing数据填充BearingRecord
            record.setCustomer(bearing.getCustomer());
            record.setModel(bearing.getModel());
            record.setProductCategory(bearing.getProductCategory());
            record.setSteelType(bearing.getSteelType());
            record.setSteelGrade(bearing.getSteelGrade());
            record.setDepository(bearing.getDepository());
            record.setStorageLocation(bearing.getStorageLocation());
            record.setOuterInnerRing(bearing.getOuterInnerRing());
            record.setSize(bearing.getSize());
            record.setPair(bearing.getPair());
            record.setState(bearing.getState());
            record.setCurrentDepository(currentDepository); // 设置当前仓库为bearing的当前仓库
            // ...其他需要的字段...
            // 设置记录的时间
            record.setTime(new Date());
            // 添加记录
            bearingRecordService.addBearingRecord(record);
            return ResponseEntity.ok(Collections.singletonMap("message", record));
        } else {
            // 如果找不到对应的Bearing信息，返回错误响应
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No bearing found for boxText: " + record.getBoxText());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBearingRecord(@PathVariable int id, @RequestBody BearingRecord record) {
        record.setId(id);
        bearingRecordService.updateBearingRecord(record);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteBearingRecord(@PathVariable int id) {
        bearingRecordService.deleteBearingRecordById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<BearingRecord> getBearingRecord(@PathVariable int id) {
        BearingRecord record = bearingRecordService.getBearingRecordById(id);
        if (record != null) {
            return ResponseEntity.ok(record);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterBearingRecords(
            @RequestParam Map<String, Object> params,
            @RequestParam(defaultValue = "1") int page, // 页码通常是从1开始
            @RequestParam(defaultValue = "20") int size) {
        String dateRange = (String) params.get("time");
        if (dateRange != null && dateRange.contains(" - ")) {
            String[] dates = dateRange.split(" - ");
            params.put("startDate", dates[0] + " 00:00:00");
            params.put("endDate", dates[1] + " 23:59:59");
        }

        // 计算开始的记录索引，MyBatis分页是从0开始计算的
        int begin = (page - 1) * size;
        params.put("begin", begin);
        params.put("size", size);

        List<BearingRecord> records = bearingRecordService.filterBearingRecords(params);
        int count = bearingRecordService.countBearingRecords(params); // 获取满足条件的记录总数

        Map<String, Object> response = new HashMap<>();
        response.put("code", 0);
        response.put("msg", "");
        response.put("count", count);
        response.put("data", records);
        return ResponseEntity.ok(response);
    }


    @GetMapping("/checkSpecialRecord/{boxText}/{boxNumber}/{depositoryName}/{quantity}/{iter}")
    public ResponseEntity<?> checkSpecialRecord(@PathVariable String boxText,
                                                @PathVariable String boxNumber,
                                                @PathVariable String depositoryName,
                                                @PathVariable int quantity,
                                                @PathVariable int iter) {
        boolean hasSpecial = bearingRecordService.hasSpecialRecord(boxText, boxNumber, depositoryName, quantity, iter);
        return ResponseEntity.ok(Collections.singletonMap("hasSpecial", hasSpecial));
    }


    @GetMapping("/inventory")
    public ResponseEntity<?> getInventoryByCutoffDate(@RequestParam Map<String, Object> params) {
        List<BearingRecord> records = bearingRecordService.selectInventoryByCutoffDate(params);
        return ResponseEntity.ok(records);
    }
    @GetMapping("/everyPair")
    public ResponseEntity<?> getEveryPairData(
            @RequestParam("startDate") @DateTimeFormat(pattern = "yyyy-MM-dd") String startDate,
            @RequestParam("endDate") @DateTimeFormat(pattern = "yyyy-MM-dd") String endDate,
            @RequestParam("depository") String depository,
            @RequestParam(required = false) String state) {
        List<Map<String, Object>> data = bearingRecordService.getEveryPairData(depository, startDate, endDate, state);
        return ResponseEntity.ok(data);
    }
    // 新端点获取库存状态信息
    @GetMapping("/customer-status")
    public ResponseEntity<List<Map<String, Object>>> getInventoryStatus(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date cutoffDate,
            @RequestParam(required = false, defaultValue = "ALL") String depository,
            @RequestParam(required = false, defaultValue = "ALL") String state) {
        List<Map<String, Object>> inventoryStatus = bearingRecordService.getInventoryStatus(cutoffDate, depository, state);
        return ResponseEntity.ok(inventoryStatus);
    }
}
