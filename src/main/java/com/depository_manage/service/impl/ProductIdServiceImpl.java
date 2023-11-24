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
    public ProductId getBoxNumberByBoxText(String boxText) {
        return productIdMapper.selectBoxNumberByBoxText(boxText);
    }

    @Override
    public List<ProductId> getAllBoxNumbers() {
        return productIdMapper.selectAllBoxNumbers();
    }

    @Override
    public ProductId saveOrUpdateBoxNumber(ProductId boxNumber) {
        productIdMapper.insertOrUpdateBoxNumber(boxNumber);
        return boxNumber;
    }

    @Override
    public String incrementAndSaveBoxNumber(String boxText, int quantity) {
        // 获取当前的 BoxNumber
        ProductId current = productIdMapper.selectBoxNumberByBoxText(boxText);
        // 计算新的 BoxNumber
        String newBoxNumber;
        if (current != null) {
            newBoxNumber = incrementBoxNumber(current.getBoxNumber());
        } else {
            newBoxNumber = "001"; // 如果不存在当前 BoxNumber，从 "001" 开始
        }
        // 创建新的 ProductId 对象并保存
        ProductId newProductIdEntry = new ProductId();
        newProductIdEntry.setBoxText(boxText);
        newProductIdEntry.setBoxNumber(newBoxNumber);
        newProductIdEntry.setQuantity(quantity);
        productIdMapper.insertOrUpdateBoxNumber(newProductIdEntry);
        return newBoxNumber;
    }
    @Override
    public String calculateNextBoxNumber(String boxText) {
        // 获取当前的 BoxNumber
        ProductId current = productIdMapper.selectBoxNumberByBoxText(boxText);
        // 计算新的 BoxNumber
        String newBoxNumber;
        if (current != null) {
            newBoxNumber = incrementBoxNumber(current.getBoxNumber());
        } else {
            newBoxNumber = "001"; // 如果不存在当前 Box_number，从 "001" 开始
        }
        return newBoxNumber;
    }
    private String incrementBoxNumber(String BoxNumber) {
        try {
            int num = Integer.parseInt(BoxNumber);
            num++; // 递增 product_id
            if (num <= 999) {
                // 如果是三位数或更少，则保持三位数格式
                return String.format("%03d", num);
            } else {
                // 如果超过三位数，则直接返回数字
                return Integer.toString(num);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid productId format: " + BoxNumber);
        }
    }
}
