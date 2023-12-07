package com.depository_manage.service;

import com.depository_manage.entity.ProductId;

import java.util.List;

public interface ProductIdService {
    ProductId getBoxNumberByBoxTextAndDepositoryId(String boxText, int depositoryId);
    List<ProductId> getAllBoxNumbers();
    ProductId saveOrUpdateBoxNumber(ProductId boxNumber);

    /**
     * 递增并保存特定 box_text 和 depository_id 的 product_id。
     *
     * @param boxText       要递增 product_id 的 box_text。
     * @param depositoryId  厂区 ID。
     * @param quantity      数量。
     * @return 新的 product_id。
     */
    String incrementAndSaveBoxNumber(String boxText, int depositoryId, int quantity);

    /**
     * 计算特定 box_text 和 depository_id 的下一个 product_id。
     *
     * @param boxText       要计算 product_id 的 box_text。
     * @param depositoryId  厂区 ID。
     * @return 预计的下一个 product_id。
     */
    String calculateNextBoxNumber(String boxText, int depositoryId);

    ProductId getProductIdByBoxTextAndDepositoryId(String boxText, String boxNumber, int depositoryId);

    // 根据需要可以添加更多方法

}
