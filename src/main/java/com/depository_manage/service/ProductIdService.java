package com.depository_manage.service;

import com.depository_manage.entity.ProductId;

import java.util.List;

public interface ProductIdService {
    ProductId getProductIdByBoxNumber(String boxNumber);
    List<ProductId> getAllProductIds();
    ProductId saveOrUpdateProductId(ProductId productId);

    /**
     * 递增并保存特定 box_number 的 product_id。
     *
     * @param boxNumber 要递增 product_id 的 box_number。
     * @return 新的 product_id。
     */
    String incrementAndSaveProductId(String boxNumber);
    // 根据需要可以添加更多方法
}
