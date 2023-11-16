package com.depository_manage.controller;

import com.depository_manage.entity.BearingInventory;
import com.depository_manage.service.BearingInventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bearingInventory")
public class BearingInventoryController {

    @Autowired
    private BearingInventoryService bearingInventoryService;

    // API端点，例如获取库存、更新库存等
    @PostMapping("/add")
    public ResponseEntity<?> addBearingInventory(@RequestBody BearingInventory bearingInventory) {
        bearingInventoryService.addBearingInventory(bearingInventory);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{boxNumber}")
    public ResponseEntity<BearingInventory> getBearingInventory(@PathVariable String boxNumber) {
        BearingInventory bearingInventory = bearingInventoryService.getBearingInventory(boxNumber);
        if (bearingInventory != null) {
            return ResponseEntity.ok(bearingInventory);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/stockIn")
    public ResponseEntity<?> stockIn(@RequestBody BearingInventory inventory) {
        bearingInventoryService.stockIn(inventory);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/stockOut")
    public ResponseEntity<?> stockOut(@RequestBody BearingInventory inventory) {
        bearingInventoryService.stockOut(inventory);
        return ResponseEntity.ok().build();
    }


}
