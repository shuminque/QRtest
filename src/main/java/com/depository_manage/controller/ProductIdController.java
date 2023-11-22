package com.depository_manage.controller;

import com.depository_manage.entity.ProductId;
import com.depository_manage.service.ProductIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/productIds")
public class ProductIdController {

    @Autowired
    private ProductIdService productIdService;

    @GetMapping("/{boxNumber}")
    public ResponseEntity<ProductId> getProductIdByBoxNumber(@PathVariable String boxNumber) {
        ProductId productId = productIdService.getProductIdByBoxNumber(boxNumber);
        if (productId != null) {
            return ResponseEntity.ok(productId);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/")
    public ResponseEntity<ProductId> createOrUpdateProductId(@RequestBody ProductId productId) {
        ProductId updatedProductId = productIdService.saveOrUpdateProductId(productId);
        if (updatedProductId != null) {
            return ResponseEntity.ok(updatedProductId);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<ProductId>> getAllProductIds() {
        List<ProductId> productIds = productIdService.getAllProductIds();
        if (productIds != null && !productIds.isEmpty()) {
            return ResponseEntity.ok(productIds);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
