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

    @GetMapping("/{boxText}")
    public ResponseEntity<ProductId> getBoxNumberByBoxText(@PathVariable String boxText) {
        ProductId productId = productIdService.getBoxNumberByBoxText(boxText);
        if (productId != null) {
            return ResponseEntity.ok(productId);
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
}
