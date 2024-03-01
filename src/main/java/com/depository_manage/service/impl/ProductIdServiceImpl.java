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
    public ProductId getLatestZeroBoxNumberByBoxTextAndDepositoryId(String boxText, int depositoryId) {
        return productIdMapper.selectBoxNumberByBoxTextAndDepositoryIdForZero(boxText, depositoryId);
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
            // 使用已有的incrementBoxNumber方法
            newBoxNumber = incrementBoxNumber(current.getBoxNumber());
            newIter = current.getIter();

            // 如果BoxNumber为"999"，则iter增加
            if ("999".equals(current.getBoxNumber())) {
                newIter++;
            }
        } else {
            newBoxNumber = "001"; // 如果不存在当前BoxNumber，从"001"开始
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
    public String incrementAndSaveBoxNumberForZero(String boxText, int depositoryId, int quantity) {
        ProductId current = productIdMapper.selectBoxNumberByBoxTextAndDepositoryIdForZero(boxText, depositoryId);
        String newBoxNumber;
        int newIter;

        if (current != null) {
            newBoxNumber = current.getBoxNumber();
            newIter = current.getIter();

            if ("1999".equals(newBoxNumber)) {
                newBoxNumber = "1001"; // Reset to 1001
                newIter++; // Increment iter
            } else {
                int num = Integer.parseInt(newBoxNumber);
                newBoxNumber = String.format("%04d", num + 1); // Increment within the range
            }
        } else {
            newBoxNumber = "1001"; // Start from 1001 if no current BoxNumber
            newIter = 1; // Initial iter value
        }

        ProductId newProductIdEntry = new ProductId();
        newProductIdEntry.setBoxText(boxText);
        newProductIdEntry.setBoxNumber(newBoxNumber);
        newProductIdEntry.setQuantity(quantity);
        newProductIdEntry.setDepositoryId(depositoryId);
        newProductIdEntry.setIter(newIter); // Set iter value
        productIdMapper.insertOrUpdateBoxNumber(newProductIdEntry);

        return newBoxNumber;
    }

    @Override
    public String calculateNextBoxNumberForZero(String boxText, int depositoryId) {
        ProductId current = productIdMapper.selectBoxNumberByBoxTextAndDepositoryIdForZero(boxText, depositoryId);
        String newBoxNumber;

        if (current != null) {
            newBoxNumber = incrementBoxNumberForZero(current.getBoxNumber());
        } else {
            newBoxNumber = "1001"; // Start from 1001 if no current BoxNumber
        }

        return newBoxNumber;
    }

    private String incrementBoxNumberForZero(String boxNumber) {
        try {
            int num = Integer.parseInt(boxNumber);
            if (num >= 1999) {
                return "1001"; // Reset to 1001
            } else {
                return String.format("%04d", num + 1); // Increment normally
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

    @Override
    public void deleteProductIdsRecord(String boxText, String boxNumber, int depositoryId, int iter) {
        productIdMapper.deleteProductIdsRecord(boxText, boxNumber, depositoryId, iter);
    }

}
