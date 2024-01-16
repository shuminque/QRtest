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
    public ProductId getOldBoxNumberByBoxTextAndDepositoryId(String boxText, int depositoryId) {
        return productIdMapper.selectOldBoxNumberByBoxTextAndDepositoryId(boxText, depositoryId);
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
        ProductId current = productIdMapper.selectBoxNumberByBoxTextAndDepositoryId(boxText, depositoryId);
        String newBoxNumber;
        int newIter;

        if (current != null) {
            newBoxNumber = current.getBoxNumber();
            newIter = current.getIter();

            if ("999".equals(newBoxNumber)) {
                newBoxNumber = "001"; // 重置为001
                newIter++; // 增加iter
            } else {
                int num = Integer.parseInt(newBoxNumber);
                newBoxNumber = String.format("%03d", num + 1); // 正常递增
            }
        } else {
            newBoxNumber = "001"; // 如果不存在当前 BoxNumber，从 "001" 开始
            newIter = 1; // 初始iter值设为1
        }

        ProductId newProductIdEntry = new ProductId();
        newProductIdEntry.setBoxText(boxText);
        newProductIdEntry.setBoxNumber(newBoxNumber);
        newProductIdEntry.setQuantity(quantity);
        newProductIdEntry.setDepositoryId(depositoryId);
        newProductIdEntry.setIter(newIter); // 设置iter值
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
    private String incrementBoxNumber(String boxNumber) {
        try {
//            int num = Integer.parseInt(boxNumber);
//            num++; // 递增 product_id
//            if (num <= 999) {
//                // 如果是三位数或更少，则保持三位数格式
//                return String.format("%03d", num);
//            } else {
//                // 如果超过三位数，则直接返回数字
//                return Integer.toString(num);
//            }
            int num = Integer.parseInt(boxNumber);
            if (num >= 999) {
                // Reset to "001" and indicate that iter needs to be incremented
                return "001";
            } else {
                // Increment box number normally
                return String.format("%03d", num + 1);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid boxNumber format: " + boxNumber);
        }
    }
    @Override
    public int getQuantityByBoxTextAndBoxNumber(String boxText, String boxNumber, int depositoryId) {
        return productIdMapper.selectQuantityByBoxTextAndBoxNumber(boxText, boxNumber, depositoryId);
    }

    @Override
    public ProductId getProductIdByBoxTextAndDepositoryId(String boxText, String boxNumber, int depositoryId) {
        return productIdMapper.selectProductIdByBoxTextAndDepositoryId(boxText, boxNumber, depositoryId);
    }

    @Override
    public void updateStockedStatus(String boxText, String boxNumber, int depositoryId, int isStocked, int iter) {
        productIdMapper.updateStockedStatus(boxText, boxNumber, depositoryId, isStocked, iter);
    }
    public boolean isProductStocked(String boxText, String boxNumber, int depositoryId, int iter) {
        Integer stockedStatus = productIdMapper.checkIfStocked(boxText, boxNumber, depositoryId, iter);
        // 考虑到null的情况，null也视为未入库
        return stockedStatus != null && stockedStatus == 1;
    }
    public boolean updateQuantity(ProductId productId) {
        return productIdMapper.updateQuantity(productId) > 0;
    }

}
