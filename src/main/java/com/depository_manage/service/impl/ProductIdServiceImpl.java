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
    public ProductId getBoxNumberByBoxTextAndDepositoryId(String boxText, int depositoryId) {
        return productIdMapper.selectBoxNumberByBoxTextAndDepositoryId(boxText, depositoryId);
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
    public String incrementAndSaveBoxNumber(String boxText, int depositoryId, int quantity) {
        // 获取当前的 BoxNumber
        ProductId current = productIdMapper.selectBoxNumberByBoxTextAndDepositoryId(boxText, depositoryId);
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
        newProductIdEntry.setDepositoryId(depositoryId);
        productIdMapper.insertOrUpdateBoxNumber(newProductIdEntry);
        return newBoxNumber;
    }

    @Override
    public String calculateNextBoxNumber(String boxText, int depositoryId) {
        // 获取当前的 BoxNumber
        ProductId current = productIdMapper.selectBoxNumberByBoxTextAndDepositoryId(boxText, depositoryId);
        // 计算新的 BoxNumber
        String newBoxNumber;
        if (current != null) {
            newBoxNumber = incrementBoxNumber(current.getBoxNumber());
        } else {
            newBoxNumber = "001"; // 如果不存在当前 Box_number，从 "001" 开始
        }
        return newBoxNumber;
    }

    @Override
    public int getQuantityByBoxTextAndBoxNumber(String boxText, String boxNumber, int depositoryId) {
        return productIdMapper.selectQuantityByBoxTextAndBoxNumber(boxText, boxNumber, depositoryId);
    }

    private String incrementBoxNumber(String boxNumber) {
        try {
            int num = Integer.parseInt(boxNumber);
            num++; // 递增 product_id
            if (num <= 999) {
                // 如果是三位数或更少，则保持三位数格式
                return String.format("%03d", num);
            } else {
                // 如果超过三位数，则直接返回数字
                return Integer.toString(num);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid boxNumber format: " + boxNumber);
        }
    }
    @Override
    public ProductId getProductIdByBoxTextAndDepositoryId(String boxText, String boxNumber, int depositoryId) {
        return productIdMapper.selectProductIdByBoxTextAndDepositoryId(boxText, boxNumber, depositoryId);
    }

    @Override
    public void updateStockedStatus(String boxText, String boxNumber, int depositoryId, int isStocked) {
        productIdMapper.updateStockedStatus(boxText, boxNumber, depositoryId, isStocked);
    }
    public boolean isProductStocked(String boxText, String boxNumber, int depositoryId) {
        Integer stockedStatus = productIdMapper.checkIfStocked(boxText, boxNumber, depositoryId);
        // 考虑到null的情况，null也视为未入库
        return stockedStatus != null && stockedStatus == 1;
    }
}
