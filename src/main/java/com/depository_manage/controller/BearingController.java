package com.depository_manage.controller;

import com.depository_manage.entity.Bearing;
import com.depository_manage.entity.ProductId;
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

    @GetMapping("/search")
    public ResponseEntity<?> searchBoxText(@RequestParam String query, @RequestParam(required = false) String depository) {
        List<String> boxTexts = bearingService.searchBoxText(query,depository);
        if (boxTexts != null && !boxTexts.isEmpty()) {
            return ResponseEntity.ok(boxTexts);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{boxText}/{depositoryId}")
    public ResponseEntity<?> createAndReturnNewProductId(@PathVariable String boxText,
                                                         @PathVariable int depositoryId,
                                                         @RequestBody Map<String, Object> requestData) {
        // 提取 quantity 和其他需要的信息
        int quantity = Integer.parseInt((String) requestData.get("quantity"));
        String depositoryText = convertDepositoryIdToText(depositoryId);
        // 1. 获取与boxNumber相关的Bearing数据
        Bearing bearing = bearingService.getBearingByBoxTextAndDepository(boxText,depositoryText);
        if (bearing == null) {
            return ResponseEntity.notFound().build();
        }
        // 2. 递增product_id并保存到product_ids表中
        String newBoxNumber = productIdService.incrementAndSaveBoxNumber(boxText, depositoryId, quantity);
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
    @GetMapping("/{boxText}/{boxNumber}/{depositoryId}")
    public ResponseEntity<?> getExistingProductInfo(@PathVariable String boxText, @PathVariable String boxNumber, @PathVariable int depositoryId) {
        // 查询 product_ids 表获取数量
        ProductId productId = productIdService.getProductIdByBoxTextAndDepositoryId(boxText, boxNumber, depositoryId);
        if (productId == null) {
            return ResponseEntity.notFound().build();
        }
        String depositoryText = convertDepositoryIdToText(depositoryId);

        // 查询 bearings 表获取其他信息
        Bearing bearing = bearingService.getBearingByBoxTextAndDepository(boxText,depositoryText);
        if (bearing == null) {
            return ResponseEntity.notFound().build();
        }

        // 构建响应数据
        Map<String, Object> response = new HashMap<>();
        response.put("boxText", boxText);
        response.put("boxNumber", boxNumber);
        response.put("quantity", productId.getQuantity());
        // 添加其他必要的数据从 bearing 对象
        response.put("model", bearing.getModel());
        response.put("customer", bearing.getCustomer());
        response.put("outerInnerRing", bearing.getOuterInnerRing());
        response.put("depository", bearing.getDepository());
        response.put("storageLocation", bearing.getStorageLocation());
        // ...

        return ResponseEntity.ok(response);
    }


    @GetMapping("/preGenerate/{boxText}/{depositoryId}")
    public ResponseEntity<?> preGenerateNewProductId(@PathVariable String boxText, @PathVariable int depositoryId) {
        // 获取与boxNumber相关的Bearing数据，但不从数据库中保存或更新
        String depositoryText = convertDepositoryIdToText(depositoryId);
        Bearing bearing = bearingService.getBearingByBoxTextAndDepository(boxText,depositoryText);
        if (bearing == null) {
            return ResponseEntity.notFound().build();
        }
        // 逻辑来模拟新的product_id的生成，但实际上并不保存它
        String mockNewProductId = productIdService.calculateNextBoxNumber(boxText, depositoryId);
        Integer mockQuantity = bearingService.calculateQuantity(boxText,depositoryText);

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
    public String convertDepositoryIdToText(int depositoryId) {
        switch (depositoryId) {
            case 1: return "SAB";
            case 2: return "ZAB";
            case 0: return "ALL";
            default: return "Unknown";
        }
    }

}
