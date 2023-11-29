package com.depository_manage.controller;

import com.depository_manage.entity.Bearing;
import com.depository_manage.service.BearingService;
import com.depository_manage.service.ProductIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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

    @GetMapping("/{boxText}")
    public ResponseEntity<Bearing> getBearingByBoxText(@PathVariable String boxText) {
        Bearing bearing = bearingService.getBearingByBoxText(boxText);
        if (bearing != null) {
            return ResponseEntity.ok(bearing);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/{boxText}")
    public ResponseEntity<?> createAndReturnNewProductId(@PathVariable String boxText, @RequestBody Map<String, Object> requestData) {
        // 提取 quantity 和其他需要的信息
        int quantity = Integer.parseInt((String) requestData.get("quantity"));
        // 1. 获取与boxNumber相关的Bearing数据
        Bearing bearing = bearingService.getBearingByBoxText(boxText);
        if (bearing == null) {
            return ResponseEntity.notFound().build();
        }
        // 2. 递增product_id并保存到product_ids表中
        String newBoxNumber = productIdService.incrementAndSaveBoxNumber(boxText,quantity);
        if (newBoxNumber == null) {
            // 在发生错误时返回JSON对象
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Unable to generate new Product ID for box number: " + boxText));
        }
        // 3. 创建包含Bearing数据和新product_id的响应
        Map<String, Object> response = getStringObjectMap(boxText, newBoxNumber, bearing, quantity);

        // 4. 返回包含Bearing数据和新product_id的响应
        return ResponseEntity.ok(response);
    }
    @GetMapping("/preGenerate/{boxText}")
    public ResponseEntity<?> preGenerateNewProductId(@PathVariable String boxText) {
        // 获取与boxNumber相关的Bearing数据，但不从数据库中保存或更新
        Bearing bearing = bearingService.getBearingByBoxText(boxText);
        if (bearing == null) {
            return ResponseEntity.notFound().build();
        }
        // 逻辑来模拟新的product_id的生成，但实际上并不保存它
        String mockNewProductId = productIdService.calculateNextBoxNumber(boxText); // 不实际递增数据库中的值
        Integer mockQuantity = bearingService.calculateQuantity(boxText);

        // 构建响应
        Map<String, Object> response = new HashMap<>();
        response.put("boxNumber", mockNewProductId);
        response.put("quantity", mockQuantity);

        // 返回即将生成的数据
        return ResponseEntity.ok(response);
    }

    private static Map<String, Object> getStringObjectMap(String boxText, String newBoxNumber, Bearing bearing) {
        Map<String, Object> response = new HashMap<>();
        response.put("boxText", boxText);
        response.put("boxNumber", newBoxNumber);
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
    private static Map<String, Object> getStringObjectMap(String boxText, String newBoxNumber, Bearing bearing, int quantity) {
        Map<String, Object> response = new HashMap<>();
        response.put("boxText", boxText);
        response.put("boxNumber", newBoxNumber);
        response.put("model", bearing.getModel());
        response.put("customer", bearing.getCustomer());
        response.put("quantity", quantity);  // 使用传递的 quantity 值
        response.put("outerInnerRing", bearing.getOuterInnerRing());   // 外/内轮
//        response.put("productCategory", bearing.getProductCategory()); // 制品分类
//        response.put("steelType", bearing.getSteelType());             // 钢种
//        response.put("steelGrade", bearing.getSteelGrade());           // 钢材等级
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
