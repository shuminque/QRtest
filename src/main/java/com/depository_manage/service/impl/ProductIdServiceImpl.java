package com.depository_manage.service.impl;

import com.depository_manage.entity.ProductId;
import com.depository_manage.mapper.ProductIdMapper;
import com.depository_manage.service.ProductIdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductIdServiceImpl implements ProductIdService {
    @Autowired
    private ProductIdMapper productIdMapper;

    @Override
    public ProductId getProductIdByBoxNumber(String boxNumber) {
        return productIdMapper.selectProductIdByBoxNumber(boxNumber);
    }

    @Override
    public List<ProductId> getAllProductIds() {
        return productIdMapper.selectAllProductIds();
    }

    @Override
    public ProductId saveOrUpdateProductId(ProductId productId) {
        productIdMapper.insertOrUpdateProductId(productId);
        return productId;
    }

    @Override
    public String incrementAndSaveProductId(String boxNumber) {
        // 获取当前的 product_id
        ProductId current = productIdMapper.selectProductIdByBoxNumber(boxNumber);
        // 计算新的 product_id
        String newProductId;
        if (current != null) {
            newProductId = incrementProductId(current.getProductId());
        } else {
            newProductId = "001"; // 如果不存在当前 product_id，从 "001" 开始
        }

        // 创建新的 ProductId 对象并保存
        ProductId newProductIdEntry = new ProductId();
        newProductIdEntry.setBoxNumber(boxNumber);
        newProductIdEntry.setProductId(newProductId);
        productIdMapper.insertOrUpdateProductId(newProductIdEntry);

        return newProductId;
    }

    private String incrementProductId(String productId) {
        try {
            int num = Integer.parseInt(productId);
            num++; // 递增 product_id
            if (num <= 999) {
                // 如果是三位数或更少，则保持三位数格式
                return String.format("%03d", num);
            } else {
                // 如果超过三位数，则直接返回数字
                return Integer.toString(num);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid productId format: " + productId);
        }
    }
}
