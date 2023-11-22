package com.depository_manage.controller;

import com.depository_manage.entity.Bearing;
import com.depository_manage.service.BearingService;
import com.depository_manage.service.ProductIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bearings")
public class BearingController {

    @Autowired
    private BearingService bearingService;
    @Autowired
    private ProductIdService productIdService;

    @GetMapping("/{boxNumber}")
    public ResponseEntity<Bearing> getBearingByBoxNumber(@PathVariable String boxNumber) {
        Bearing bearing = bearingService.getBearingByBoxNumber(boxNumber);
        if (bearing != null) {
            return ResponseEntity.ok(bearing);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/{boxNumber}")
    public ResponseEntity<?> createAndReturnNewProductId(@PathVariable String boxNumber) {
        // 1. 获取与boxNumber相关的Bearing数据
        Bearing bearing = bearingService.getBearingByBoxNumber(boxNumber);
        if (bearing == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. 递增product_id并保存到product_ids表中
        String newProductId = productIdService.incrementAndSaveProductId(boxNumber);
        if (newProductId == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to generate new Product ID for box number: " + boxNumber);
        }

// 3. 创建包含Bearing数据和新product_id的响应
        Map<String, Object> response = getStringObjectMap(boxNumber, newProductId, bearing);

// 4. 返回包含Bearing数据和新product_id的响应
        return ResponseEntity.ok(response);

    }

    private static Map<String, Object> getStringObjectMap(String boxNumber, String newProductId, Bearing bearing) {
        Map<String, Object> response = new HashMap<>();
        response.put("boxNumber", boxNumber);
        response.put("productId", newProductId);
        response.put("model", bearing.getModel());
        response.put("customer", bearing.getCustomer());
        response.put("quantity", bearing.getQuantity());
        response.put("outerInnerRing", bearing.getOuterInnerRing());   // 外/内轮
        response.put("productCategory", bearing.getProductCategory()); // 制品分类
        response.put("steelType", bearing.getSteelType());             // 钢种
        response.put("steelGrade", bearing.getSteelGrade());           // 钢材等级
        response.put("depository", bearing.getDepository());           // 厂区
        response.put("storageLocation", bearing.getStorageLocation()); // 库位
        return response;
    }


    @GetMapping("/all")
    public ResponseEntity<List<Bearing>> getAllBearings() {
        List<Bearing> bearings = bearingService.getAllBearings();
        if (bearings != null && !bearings.isEmpty()) {
            return ResponseEntity.ok(bearings);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
