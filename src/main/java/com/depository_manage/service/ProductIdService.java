package com.depository_manage.service;

import com.depository_manage.entity.ProductId;

import java.util.List;

public interface ProductIdService {
    ProductId getBoxNumberByBoxText(String boxText);
    List<ProductId> getAllBoxNumbers();
    ProductId saveOrUpdateBoxNumber(ProductId boxNumber);

    /**
     * 递增并保存特定 box_number 的 product_id。
     *
     * @param boxText 要递增 product_id 的 box_number。
     * @return 新的 product_id。
     */
    String incrementAndSaveBoxNumber(String boxText);

    String calculateNextBoxNumber(String boxText);
    // 根据需要可以添加更多方法
}
