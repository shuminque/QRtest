package com.depository_manage.controller;

import com.depository_manage.entity.ProductId;
import com.depository_manage.service.ProductIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/productIds")
public class ProductIdController {

    @Autowired
    private ProductIdService productIdService;

    @GetMapping("/")
    public ResponseEntity<ProductId> getBoxNumberByBoxTextAndDepositoryId(
            @RequestParam String boxText,
            @RequestParam int depositoryId) {
        ProductId productId = productIdService.getBoxNumberByBoxTextAndDepositoryId(boxText, depositoryId);
        if (productId != null) {
            return ResponseEntity.ok(productId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/Old")
    public ResponseEntity<ProductId> getOldBoxNumberByBoxTextAndDepositoryId(
            @RequestParam String boxText,
            @RequestParam int depositoryId) {
        ProductId productId = productIdService.getOldBoxNumberByBoxTextAndDepositoryId(boxText, depositoryId);
        if (productId != null) {
            return ResponseEntity.ok(productId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/quantity")
    public ResponseEntity<?> getQuantityByBoxTextAndBoxNumber(
            @RequestParam String boxText,
            @RequestParam String boxNumber,
            @RequestParam int depositoryId) {

        int quantity = productIdService.getQuantityByBoxTextAndBoxNumber(boxText, boxNumber, depositoryId);
        if (quantity >= 0) {
            return ResponseEntity.ok(Collections.singletonMap("quantity", quantity));
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @PostMapping("/")
    public ResponseEntity<ProductId> createOrUpdateProductId(@RequestBody ProductId boxNumber) {
        ProductId updatedBoxNumber = productIdService.saveOrUpdateBoxNumber(boxNumber);
        if (updatedBoxNumber != null) {
            return ResponseEntity.ok(updatedBoxNumber);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductId>> getAllBoxNumbers() {
        List<ProductId> boxNumbers = productIdService.getAllBoxNumbers();
        if (boxNumbers != null && !boxNumbers.isEmpty()) {
            return ResponseEntity.ok(boxNumbers);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    @PostMapping("/updateQuantity")
    public ResponseEntity<?> updateQuantity(@RequestBody ProductId productId) {
        System.out.println(productId);
        boolean updateSuccess = productIdService.updateQuantity(productId);

        if (updateSuccess) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新失败");
        }
    }


}
