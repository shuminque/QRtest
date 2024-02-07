package com.depository_manage.controller;

import com.depository_manage.entity.Bearing;
import com.depository_manage.entity.BearingRecord;
import com.depository_manage.service.BearingRecordService;
import com.depository_manage.service.BearingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bearingRecords")
public class BearingRecordController {

    @Autowired
    private BearingRecordService bearingRecordService;
    @Autowired
    private BearingService bearingService;
    @PostMapping
    public ResponseEntity<?> addBearingRecord(@RequestBody BearingRecord record) {
        // 获取与boxText相关的Bearing数据
        System.out.println(record);
        Bearing bearing = bearingService.getBearingByBoxTextAndDepository(record.getBoxText(), record.getDepository());
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

    @DeleteMapping("/{id}")
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
    public ResponseEntity<?> filterBearingRecords(@RequestParam Map<String, Object> params) {
        String dateRange = (String) params.get("time");
        if (dateRange !=null && dateRange.contains(" - ")){
            String[] dates = dateRange.split(" - ");
            params.put("startDate", dates[0] + " 00:00:00");
            params.put("endDate", dates[1] + " 23:59:59");
        }
        List<BearingRecord> records = bearingRecordService.filterBearingRecords(params);
        return ResponseEntity.ok(records);
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
            @RequestParam("depository") String depository) {
        List<Map<String, Object>> data = bearingRecordService.getEveryPairData(startDate, endDate, depository);
        return ResponseEntity.ok(data);
    }

}
